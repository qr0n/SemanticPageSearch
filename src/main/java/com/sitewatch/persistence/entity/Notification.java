package com.sitewatch.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Notification
 *
 * Represents a notification sent for a discovered item.
 * Tracks delivery status and payload for auditing and retry logic.
 *
 * Key responsibilities:
 * - Record notification attempts for each item
 * - Store channel type (email, webhook, Slack, etc.)
 * - Persist payload and delivery status
 * - Enable retry logic for failed notifications
 *
 * Persistence: Mapped to the 'notifications' table with indexes on
 * item_id and sent_at for querying notification history.
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_item_id", columnList = "item_id"),
    @Index(name = "idx_notifications_sent_at", columnList = "sent_at"),
    @Index(name = "idx_notifications_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    /**
     * Unique identifier for the notification.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Reference to the item this notification is about.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /**
     * Notification channel type (email, webhook, slack, discord).
     */
    @Column(nullable = false)
    private String channel;

    /**
     * JSON payload sent in the notification.
     */
    @Column(columnDefinition = "jsonb")
    private String payload;

    /**
     * Timestamp when notification was sent.
     */
    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    /**
     * Delivery status (pending, sent, failed, retrying).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    /**
     * Error message if delivery failed.
     */
    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    /**
     * Number of retry attempts made.
     */
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * NotificationStatus
     *
     * Enumeration of notification delivery statuses.
     */
    public enum NotificationStatus {
        /**
         * Notification queued but not yet sent.
         */
        PENDING,

        /**
         * Notification successfully delivered.
         */
        SENT,

        /**
         * Delivery failed after all retries.
         */
        FAILED,

        /**
         * Notification is being retried.
         */
        RETRYING
    }
}
