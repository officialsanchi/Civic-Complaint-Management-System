package com.civic.complaint.repository;

import com.civic.complaint.model.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistedTokenRepository  extends JpaRepository<BlacklistedToken, Long> {

    boolean existsByToken(String token);
}
