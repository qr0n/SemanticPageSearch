#!/usr/bin/env bash
# Install Git hooks for the project

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
HOOKS_DIR="$PROJECT_ROOT/.githooks"
GIT_HOOKS_DIR="$PROJECT_ROOT/.git/hooks"

echo "📦 Installing Git hooks..."

# Check if .git directory exists
if [ ! -d "$GIT_HOOKS_DIR" ]; then
    echo "❌ Error: .git/hooks directory not found. Are you in a Git repository?"
    exit 1
fi

# Copy pre-commit hook
if [ -f "$HOOKS_DIR/pre-commit" ]; then
    cp "$HOOKS_DIR/pre-commit" "$GIT_HOOKS_DIR/pre-commit"
    chmod +x "$GIT_HOOKS_DIR/pre-commit"
    echo "✅ Installed pre-commit hook"
else
    echo "⚠️  Warning: pre-commit hook not found in $HOOKS_DIR"
fi

echo "🎉 Git hooks installed successfully!"
echo ""
echo "To run checks manually:"
echo "  mvn checkstyle:check"
echo "  mvn verify"
