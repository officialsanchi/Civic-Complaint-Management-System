package com.civic.complaint.repository;

import com.civic.complaint.model.Complaint;
import com.civic.complaint.enums.ComplaintStatus;
import com.civic.complaint.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Page<Complaint> findByReporter(User reporter, Pageable pageable);
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);
    Page<Complaint> findByCategory(String category, Pageable pageable);

    @Query("SELECT c FROM Complaint c WHERE " +
            "LOWER(c.title) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Complaint> search(@Param("q") String query, Pageable pageable);

    long countByStatus(ComplaintStatus status);

    @Query("SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category")
    List<Object[]> countGroupedByCategory();
}
