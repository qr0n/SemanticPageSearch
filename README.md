# SiteWatch

Monitor configured websites for new or changed content that matches user-provided rules, persist discoveries, and notify via webhooks/email/UX.

## Overview

**SiteWatch** is a Spring Boot application designed to track changes in websites and RSS feeds based on configurable filters (keywords, regex). When new content matching your rules is discovered, you're notified through webhooks, email, or an in-app feed.

### Key Features

- **Multi-source monitoring**: Track RSS/Atom feeds and HTML pages
- **Flexible filtering**: Keyword matching, regex patterns, boolean logic
- **Smart deduplication**: By URL and content hash
- **Multiple notification channels**: Email, Slack, Discord, HTTP webhooks
- **Rate limiting & politeness**: Respects robots.txt, configurable throttling
- **Observability**: Prometheus metrics, structured logging, OpenTelemetry support
- **Production-ready**: Containerized, Helm charts, secure credential management

## Technology Stack

- **Java 17+** / **Spring Boot 3.2**
- **PostgreSQL** for persistence
- **Jsoup** for HTML parsing
- **Rome** for RSS/Atom feed parsing
- **Flyway** for database migrations
- **Micrometer** for metrics & monitoring

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+
- Docker (optional, for containerized deployment)

### Local Development

1. **Clone and navigate to the project**:
   ```bash
   cd SemanticPageSearch
   ```

2. **Set up PostgreSQL**:
   ```bash
   docker run -d \
     --name sitewatch-postgres \
     -e POSTGRES_DB=sitewatch \
     -e POSTGRES_USER=sitewatch \
     -e POSTGRES_PASSWORD=sitewatch \
     -p 5432:5432 \
     postgres:15-alpine
   ```

3. **Configure environment** (optional):
   Create `src/main/resources/application-local.yml` with your overrides.

4. **Build and run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access the application**:
   - API: `http://localhost:8080/api/v1`
   - Actuator health: `http://localhost:8080/actuator/health`
   - Prometheus metrics: `http://localhost:8080/actuator/prometheus`

## Project Structure

```
src/main/java/com/sitewatch/
├── api/                    # REST controllers, DTOs, request/response models
├── service/                # Business logic, orchestration
├── crawler/                # RSS fetcher, HTML scraper, content parsing
├── persistence/            # JPA entities, repositories
├── notifier/               # Notification channels (email, webhook, Slack)
├── config/                 # Spring configuration, security, scheduling
└── SiteWatchApplication.java
```

## API Usage

### Add a Source

```bash
curl -X POST http://localhost:8080/api/v1/sources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tech Blog",
    "url": "https://example.com/blog",
    "mode": "AUTO",
    "filterKeywords": ["kubernetes", "cloud"],
    "intervalMinutes": 30
  }'
```

### List Sources

```bash
curl http://localhost:8080/api/v1/sources
```

### Check Source Now

```bash
curl -X POST http://localhost:8080/api/v1/sources/{id}/check-now
```

### Get Recent Items

```bash
curl http://localhost:8080/api/v1/sources/{id}/items?limit=20
```

## Code Quality & Documentation

This project enforces strict code quality standards:

- **Checkstyle**: All public classes and methods require Javadoc
- **PMD**: Code complexity and best practice checks
- **SpotBugs**: Static analysis for potential bugs
- **Pre-commit hooks**: Automatic validation before commits

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed documentation requirements.

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn verify

# Integration tests (requires Docker for Testcontainers)
mvn verify -P integration-tests
```

## Deployment

### Docker

```bash
docker build -t sitewatch:latest .
docker run -p 8080:8080 \
  -e DB_HOST=postgres \
  -e DB_PASSWORD=secret \
  sitewatch:latest
```

### Kubernetes (Helm)

```bash
helm install sitewatch ./helm/sitewatch \
  --set postgresql.auth.password=secret \
  --set ingress.enabled=true
```

## Configuration

Key configuration properties (set via environment variables):

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `sitewatch` |
| `DB_USER` | Database user | `sitewatch` |
| `DB_PASSWORD` | Database password | `sitewatch` |
| `JWT_SECRET` | JWT signing secret | ⚠️ **Must change in production** |
| `MAIL_HOST` | SMTP host | `smtp.gmail.com` |
| `MAIL_USERNAME` | SMTP username | - |
| `MAIL_PASSWORD` | SMTP password | - |

## Observability

- **Health**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

Custom metrics:
- `sitewatch_checks_total`: Total checks performed
- `sitewatch_checks_success`: Successful checks
- `sitewatch_items_found_total`: New items discovered
- `sitewatch_notifications_sent`: Notifications dispatched

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and development process.

## Contact

issues@ir0n.xyz
