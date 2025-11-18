-- Create sources table
CREATE TABLE sources (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    url TEXT NOT NULL,
    mode VARCHAR(10) NOT NULL CHECK (mode IN ('RSS', 'HTML', 'AUTO')),
    filter_keywords TEXT[],
    filter_regex TEXT[],
    interval_minutes INTEGER NOT NULL DEFAULT 60,
    last_checked TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

-- Create indexes for sources
CREATE INDEX idx_sources_url ON sources(url);
CREATE INDEX idx_sources_last_checked ON sources(last_checked);

-- Create items table
CREATE TABLE items (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL REFERENCES sources(id) ON DELETE CASCADE,
    title TEXT,
    link TEXT NOT NULL,
    summary TEXT,
    published_at TIMESTAMP,
    discovered_at TIMESTAMP NOT NULL DEFAULT now(),
    content_hash TEXT
);

-- Create unique constraint and indexes for items
ALTER TABLE items ADD CONSTRAINT uk_items_source_link UNIQUE (source_id, link);
CREATE INDEX idx_items_content_hash ON items(content_hash);
CREATE INDEX idx_items_published_at ON items(published_at);
CREATE INDEX idx_items_discovered_at ON items(discovered_at);

-- Create notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    channel VARCHAR(50) NOT NULL,
    payload JSONB,
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'RETRYING')),
    error_message TEXT,
    retry_count INTEGER DEFAULT 0
);

-- Create indexes for notifications
CREATE INDEX idx_notifications_item_id ON notifications(item_id);
CREATE INDEX idx_notifications_sent_at ON notifications(sent_at);
CREATE INDEX idx_notifications_status ON notifications(status);
