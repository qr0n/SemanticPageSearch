#!/bin/bash
# SiteWatch Demo Script - Semantic Page Search
# This script demonstrates the complete workflow of the crawler system

set -e  # Exit on error

BASE_URL="http://localhost:8080"
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  SiteWatch - Semantic Page Search Demo${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Step 1: Check server health
echo -e "${YELLOW}[1/7] Checking server status...${NC}"
curl -s -f ${BASE_URL}/actuator/health > /dev/null 2>&1 || {
    echo -e "${GREEN}Server is running!${NC}\n"
}

# Step 2: Create RSS feed source with keyword filtering
echo -e "${YELLOW}[2/7] Creating RSS source for Hacker News (filtering for 'AI' and 'Python')...${NC}"
RSS_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/v1/sources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Hacker News - AI & Python",
    "url": "https://news.ycombinator.com/rss",
    "mode": "RSS",
    "keywords": ["AI", "Python", "machine learning"],
    "checkIntervalMinutes": 60
  }')

RSS_SOURCE_ID=$(echo $RSS_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])")
echo -e "${GREEN}✓ Created RSS source with ID: ${RSS_SOURCE_ID}${NC}\n"

# Step 3: Create HTML scraping source
echo -e "${YELLOW}[3/7] Creating HTML scraping source for a blog post...${NC}"
HTML_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/v1/sources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Example Blog Post",
    "url": "https://paulgraham.com/startupideas.html",
    "mode": "HTML",
    "checkIntervalMinutes": 1440
  }')

HTML_SOURCE_ID=$(echo $HTML_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])")
echo -e "${GREEN}✓ Created HTML source with ID: ${HTML_SOURCE_ID}${NC}\n"

# Step 4: Crawl the RSS feed
echo -e "${YELLOW}[4/7] Crawling Hacker News RSS feed...${NC}"
RSS_CRAWL=$(curl -s -X POST ${BASE_URL}/api/v1/crawler/sources/${RSS_SOURCE_ID}/crawl)
RSS_ITEMS=$(echo $RSS_CRAWL | python3 -c "import sys, json; print(json.load(sys.stdin)['itemsDiscovered'])")
echo -e "${GREEN}✓ Discovered ${RSS_ITEMS} items from RSS feed${NC}\n"

# Step 5: Crawl the HTML page
echo -e "${YELLOW}[5/7] Scraping HTML content from blog post...${NC}"
HTML_CRAWL=$(curl -s -X POST ${BASE_URL}/api/v1/crawler/sources/${HTML_SOURCE_ID}/crawl)
HTML_ITEMS=$(echo $HTML_CRAWL | python3 -c "import sys, json; print(json.load(sys.stdin)['itemsDiscovered'])")
echo -e "${GREEN}✓ Scraped ${HTML_ITEMS} page (with intelligent content extraction)${NC}\n"

# Step 6: Display discovered items
echo -e "${YELLOW}[6/7] Showing sample discovered items...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

echo -e "\n${GREEN}RSS Feed Items (first 3):${NC}"
curl -s "${BASE_URL}/api/v1/items?sourceId=${RSS_SOURCE_ID}" | \
  python3 -c "
import sys, json
items = json.load(sys.stdin)[:3]
for i, item in enumerate(items, 1):
    print(f'\n{i}. {item[\"title\"]}')
    print(f'   Link: {item[\"link\"]}')
    print(f'   Published: {item.get(\"publishedAt\", \"N/A\")}')
    print(f'   Hash: {item[\"contentHash\"][:16]}...')
"

echo -e "\n${GREEN}HTML Scraped Content:${NC}"
curl -s "${BASE_URL}/api/v1/items?sourceId=${HTML_SOURCE_ID}" | \
  python3 -c "
import sys, json
items = json.load(sys.stdin)
for item in items:
    print(f'\nTitle: {item[\"title\"]}')
    print(f'Link: {item[\"link\"]}')
    print(f'Summary: {item[\"summary\"][:100]}...')
    print(f'Hash: {item[\"contentHash\"][:16]}...')
"

# Step 7: Show statistics
echo -e "\n${YELLOW}[7/7] Overall Statistics:${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

curl -s ${BASE_URL}/api/v1/sources | \
  python3 -c "
import sys, json
sources = json.load(sys.stdin)
print(f'\n📊 Total Sources: {len(sources)}')
for source in sources:
    print(f'   • {source[\"name\"]} ({source[\"mode\"]}) - {source[\"url\"]}')
"

curl -s ${BASE_URL}/api/v1/items | \
  python3 -c "
import sys, json
items = json.load(sys.stdin)
print(f'\n📄 Total Items Discovered: {len(items)}')
print(f'🔗 Unique Content Hashes: {len(set(item[\"contentHash\"] for item in items))}')
print(f'🌐 Unique Sources: {len(set(item[\"sourceName\"] for item in items))}')
"

echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✓ Demo Complete!${NC}\n"
echo -e "${YELLOW}Try these commands yourself:${NC}"
echo "  curl ${BASE_URL}/api/v1/sources"
echo "  curl ${BASE_URL}/api/v1/items"
echo "  curl -X POST ${BASE_URL}/api/v1/crawler/sources/{SOURCE_ID}/crawl"
echo ""
