package com.sitewatch.persistence.repository;

import com.sitewatch.persistence.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * NotificationRepository
 *
 * Spring Data JPA repository for {@link Notification} entities.
 * Provides query methods for managing notification delivery records.
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find all notifications for a specific item.
     *
     * @param itemId the UUID of the item
     * @param pageable pagination and sorting parameters
     * @return page of notifications
     */
    Page<Notification> findByItemIdOrderBySentAtDesc(UUID itemId, Pageable pageable);

    /**
     * Find notifications by status.
     *
     * @param status the notification status
     * @return list of notifications with matching status
     */
    List<Notification> findByStatus(Notification.NotificationStatus status);

    /**
     * Find failed notifications that need retry (status FAILED or RETRYING, retry count below max).
     *
     * @param status the notification status
     * @param maxRetries maximum retry attempts
     * @return list of notifications eligible for retry
     */
    List<Notification> findByStatusAndRetryCountLessThan(
        Notification.NotificationStatus status,
        Integer maxRetries
    );

    /**
     * Find notifications sent within a time range.
     *
     * @param start start of time range
     * @param end end of time range
     * @param pageable pagination parameters
     * @return page of notifications
     */
    Page<Notification> findBySentAtBetweenOrderBySentAtDesc(
        Instant start,
        Instant end,
        Pageable pageable
    );

    /**
     * Count notifications by status.
     *
     * @param status the notification status
     * @return count of notifications
     */
    long countByStatus(Notification.NotificationStatus status);
}
