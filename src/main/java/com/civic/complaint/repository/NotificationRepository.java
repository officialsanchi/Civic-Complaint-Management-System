package com.civic.complaint.repository;

import com.civic.complaint.model.Notification;
import com.civic.complaint.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {


    Page<Notification> findByUserOrderBySentAtDesc(User user, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    void markAllReadForUser(@Param("user") User user);

    long countByUserAndIsRead(User user, boolean isRead);
}
