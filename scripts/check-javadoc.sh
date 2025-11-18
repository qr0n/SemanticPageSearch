#!/usr/bin/env bash
# Basic check for missing Javadoc on public classes and methods
# Note: Checkstyle provides more robust validation; this is a quick pre-commit check

set -e

echo "🔍 Scanning for missing Javadoc..."

MISSING_JAVADOC=0

# Find Java files (excluding test files and generated code)
FILES=$(git ls-files '*.java' | grep -v '/test/' | grep -v '/target/')

for file in $FILES; do
    # Check if file exists (in case of deleted files in staging)
    [ -f "$file" ] || continue
    
    # Extract public class/interface/enum declarations and check for preceding Javadoc
    while IFS= read -r line_num; do
        # Get the line content
        line=$(sed -n "${line_num}p" "$file")
        
        # Skip if it's just an annotation
        if [[ "$line" =~ ^[[:space:]]*@ ]]; then
            continue
        fi
        
        # Check previous non-empty line for Javadoc start
        prev_line_num=$((line_num - 1))
        while [ $prev_line_num -gt 0 ]; do
            prev_line=$(sed -n "${prev_line_num}p" "$file")
            # Skip empty lines and annotations
            if [[ -n "$prev_line" && ! "$prev_line" =~ ^[[:space:]]*$ && ! "$prev_line" =~ ^[[:space:]]*@ ]]; then
                break
            fi
            prev_line_num=$((prev_line_num - 1))
        done
        
        # Check if the previous non-empty line contains Javadoc
        if [[ ! "$prev_line" =~ /\*\* && ! "$prev_line" =~ \*/ ]]; then
            echo "⚠️  Missing Javadoc: $file:$line_num"
            echo "   $line"
            MISSING_JAVADOC=1
        fi
    done < <(grep -n "^[[:space:]]*public[[:space:]]\+\(class\|interface\|enum\)" "$file" | cut -d: -f1)
done

if [ $MISSING_JAVADOC -eq 1 ]; then
    echo ""
    echo "❌ Missing Javadoc found. Please add documentation."
    echo "   See JAVADOC_TEMPLATE.md for examples."
    exit 1
fi

echo "✅ Javadoc check passed!"
exit 0
