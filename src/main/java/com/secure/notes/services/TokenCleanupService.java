package com.secure.notes.services;

import com.secure.notes.repositories.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service responsible for cleaning up expired and used password reset tokens
 * from the database on a scheduled basis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Scheduled task that runs every hour to clean up expired and used tokens.
     * 
     * Cron expression breakdown:
     * - 0: Second (0th second)
     * - 0: Minute (0th minute)
     * - *: Hour (every hour)
     * - *: Day of month (every day)
     * - *: Month (every month)
     * - *: Day of week (every day of week)
     */
//    @Scheduled(cron = "0 0 * * * *") // Runs every hour at minute 0
    @Scheduled(cron = "0 0/30 * * * *") // Runs every 30 minutes at 0th second
    public void cleanupExpiredAndUsedTokens() {
        log.info("Starting scheduled cleanup of password reset tokens...");
        
        try {
            Instant now = Instant.now();
            
            // Get counts before deletion (for logging)
            long expiredCount = passwordResetTokenRepository.countExpiredTokens(now);
            long usedCount = passwordResetTokenRepository.countUsedTokens();
            
            log.info("Found {} expired tokens and {} used tokens", expiredCount, usedCount);
            
            // Delete all expired or used tokens in a single query
            int deletedCount = passwordResetTokenRepository.deleteExpiredOrUsedTokens(now);
            
            log.info("Successfully cleaned up {} password reset tokens", deletedCount);
            
        } catch (Exception e) {
            log.error("Error occurred during token cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Optional: Separate cleanup for only expired tokens
     * This method is not scheduled by default but can be called manually if needed
     */
    public int cleanupExpiredTokens() {
        log.info("Manual cleanup of expired tokens requested");
        Instant now = Instant.now();
        int deletedCount = passwordResetTokenRepository.deleteExpiredTokens(now);
        log.info("Deleted {} expired tokens", deletedCount);
        return deletedCount;
    }
    
    /**
     * Optional: Separate cleanup for only used tokens
     * This method is not scheduled by default but can be called manually if needed
     */
    public int cleanupUsedTokens() {
        log.info("Manual cleanup of used tokens requested");
        int deletedCount = passwordResetTokenRepository.deleteUsedTokens();
        log.info("Deleted {} used tokens", deletedCount);
        return deletedCount;
    }
    
    /**
     * Get statistics about tokens in the database
     */
    public TokenCleanupStats getCleanupStats() {
        Instant now = Instant.now();
        long totalTokens = passwordResetTokenRepository.count();
        long expiredTokens = passwordResetTokenRepository.countExpiredTokens(now);
        long usedTokens = passwordResetTokenRepository.countUsedTokens();
        
        return new TokenCleanupStats(totalTokens, expiredTokens, usedTokens);
    }
    
    /**
     * Inner class to hold cleanup statistics
     */
    public record TokenCleanupStats(
            long totalTokens,
            long expiredTokens,
            long usedTokens
    ) {
        public long getValidTokens() {
            return totalTokens - expiredTokens - usedTokens;
        }
    }
}