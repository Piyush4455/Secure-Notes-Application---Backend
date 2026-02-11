package com.secure.notes.repositories;

import com.secure.notes.models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Delete all tokens that have expired (expiryDate is before the current time)
     * @param now Current timestamp
     * @return Number of deleted records
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Delete all tokens that have been used
     * @return Number of deleted records
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.used = true")
    int deleteUsedTokens();

    /**
     * Delete all tokens that are either expired OR used
     * This is more efficient than calling two separate delete methods
     * @param now Current timestamp
     * @return Number of deleted records
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :now OR p.used = true")
    int deleteExpiredOrUsedTokens(@Param("now") Instant now);

    /**
     * Count expired tokens (for monitoring/logging purposes)
     * @param now Current timestamp
     * @return Count of expired tokens
     */
    @Query("SELECT COUNT(p) FROM PasswordResetToken p WHERE p.expiryDate < :now")
    long countExpiredTokens(@Param("now") Instant now);

    /**
     * Count used tokens (for monitoring/logging purposes)
     * @return Count of used tokens
     */
    @Query("SELECT COUNT(p) FROM PasswordResetToken p WHERE p.used = true")
    long countUsedTokens();
}