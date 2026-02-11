package com.secure.notes.controllers;

import com.secure.notes.dtos.UserDTO;
import com.secure.notes.models.Role;
import com.secure.notes.models.User;
import com.secure.notes.services.TokenCleanupService;
import com.secure.notes.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    @Autowired
    UserService userService;

    private final TokenCleanupService tokenCleanupService;

    @GetMapping("/getusers")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(),
                HttpStatus.OK);
    }

    @PutMapping("/update-role")
    public ResponseEntity<String> updateUserRole(@RequestParam Long userId,
                                                 @RequestParam String roleName) {
        userService.updateUserRole(userId, roleName);
        return ResponseEntity.ok("User role updated");
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return new ResponseEntity<>(userService.getUserById(id),
                HttpStatus.OK);
    }

    @PutMapping("/update-lock-status")
    public ResponseEntity<String> updateAccountLockStatus(@RequestParam Long userId,
                                                          @RequestParam boolean lock) {
        userService.updateAccountLockStatus(userId, lock);
        return ResponseEntity.ok("Account lock status updated");
    }

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return userService.getAllRoles();
    }

    @PutMapping("/update-expiry-status")
    public ResponseEntity<String> updateAccountExpiryStatus(@RequestParam Long userId,
                                                            @RequestParam boolean expire) {
        userService.updateAccountExpiryStatus(userId, expire);
        return ResponseEntity.ok("Account expiry status updated");
    }

    @PutMapping("/update-enabled-status")
    public ResponseEntity<String> updateAccountEnabledStatus(@RequestParam Long userId,
                                                             @RequestParam boolean enabled) {
        userService.updateAccountEnabledStatus(userId, enabled);
        return ResponseEntity.ok("Account enabled status updated");
    }

    @PutMapping("/update-credentials-expiry-status")
    public ResponseEntity<String> updateCredentialsExpiryStatus(@RequestParam Long userId, @RequestParam boolean expire) {
        userService.updateCredentialsExpiryStatus(userId, expire);
        return ResponseEntity.ok("Credentials expiry status updated");
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam Long userId,
                                                 @RequestParam String password) {
        try {
            userService.updatePassword(userId, password);
            return ResponseEntity.ok("Password updated");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Manually trigger token cleanup
     * GET http://localhost:8080/api/admin/tokens/cleanup
     */
    @PostMapping("/tokens/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> manualCleanup() {
        // Trigger cleanup manually
        tokenCleanupService.cleanupExpiredAndUsedTokens();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Token cleanup triggered successfully");
        response.put("timestamp", java.time.Instant.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Get token statistics
     * GET http://localhost:8080/api/admin/tokens/stats
     */
    @GetMapping("/tokens/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTokenStats() {
        TokenCleanupService.TokenCleanupStats stats = tokenCleanupService.getCleanupStats();

        Map<String, Object> response = new HashMap<>();
        response.put("totalTokens", stats.totalTokens());
        response.put("expiredTokens", stats.expiredTokens());
        response.put("usedTokens", stats.usedTokens());
        response.put("validTokens", stats.getValidTokens());

        return ResponseEntity.ok(response);
    }

    /**
     * Cleanup only expired tokens
     * POST http://localhost:8080/api/admin/tokens/cleanup-expired
     */
    @PostMapping("/tokens/cleanup-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupExpiredOnly() {
        int deleted = tokenCleanupService.cleanupExpiredTokens();

        Map<String, Object> response = new HashMap<>();
        response.put("deletedCount", deleted);
        response.put("type", "expired");
        response.put("message", "Deleted " + deleted + " expired tokens");

        return ResponseEntity.ok(response);
    }

    /**
     * Cleanup only used tokens
     * POST http://localhost:8080/api/admin/tokens/cleanup-used
     */
    @PostMapping("/tokens/cleanup-used")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupUsedOnly() {
        int deleted = tokenCleanupService.cleanupUsedTokens();

        Map<String, Object> response = new HashMap<>();
        response.put("deletedCount", deleted);
        response.put("type", "used");
        response.put("message", "Deleted " + deleted + " used tokens");

        return ResponseEntity.ok(response);
    }

}

//package com.secure.notes.controllers;
//
//import com.secure.notes.dtos.UserDTO;
//import com.secure.notes.exceptions.UnauthorizedAdminOperationException;
//import com.secure.notes.models.Role;
//import com.secure.notes.models.User;
//import com.secure.notes.services.TokenCleanupService;
//import com.secure.notes.services.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/admin")
//@RequiredArgsConstructor
//@PreAuthorize("hasRole('ROLE_ADMIN')")
//public class AdminController {
//
//    @Autowired
//    UserService userService;
//
//    private final TokenCleanupService tokenCleanupService;
//
//    @GetMapping("/getusers")
//    public ResponseEntity<List<User>> getAllUsers() {
//        return new ResponseEntity<>(userService.getAllUsers(),
//                HttpStatus.OK);
//    }
//
//    @PutMapping("/update-role")
//    public ResponseEntity<String> updateUserRole(@RequestParam Long userId,
//                                                 @RequestParam String roleName) {
//        try {
//            userService.updateUserRole(userId, roleName);
//            return ResponseEntity.ok("User role updated successfully");
//        } catch (UnauthorizedAdminOperationException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }
//
//    @GetMapping("/user/{id}")
//    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
//        return new ResponseEntity<>(userService.getUserById(id),
//                HttpStatus.OK);
//    }
//
//    @PutMapping("/update-lock-status")
//    public ResponseEntity<String> updateAccountLockStatus(@RequestParam Long userId,
//                                                          @RequestParam boolean lock) {
//        try {
//            userService.updateAccountLockStatus(userId, lock);
//            return ResponseEntity.ok("Account lock status updated successfully");
//        } catch (UnauthorizedAdminOperationException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }
//
//    @GetMapping("/roles")
//    public List<Role> getAllRoles() {
//        return userService.getAllRoles();
//    }
//
//    @PutMapping("/update-expiry-status")
//    public ResponseEntity<String> updateAccountExpiryStatus(@RequestParam Long userId,
//                                                            @RequestParam boolean expire) {
//        try {
//            userService.updateAccountExpiryStatus(userId, expire);
//            return ResponseEntity.ok("Account expiry status updated successfully");
//        } catch (UnauthorizedAdminOperationException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }
//
//    @PutMapping("/update-enabled-status")
//    public ResponseEntity<String> updateAccountEnabledStatus(@RequestParam Long userId,
//                                                             @RequestParam boolean enabled) {
//        try {
//            userService.updateAccountEnabledStatus(userId, enabled);
//            return ResponseEntity.ok("Account enabled status updated successfully");
//        } catch (UnauthorizedAdminOperationException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }
//
//    @PutMapping("/update-credentials-expiry-status")
//    public ResponseEntity<String> updateCredentialsExpiryStatus(@RequestParam Long userId,
//                                                                @RequestParam boolean expire) {
//        try {
//            userService.updateCredentialsExpiryStatus(userId, expire);
//            return ResponseEntity.ok("Credentials expiry status updated successfully");
//        } catch (UnauthorizedAdminOperationException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }
//
//    @PutMapping("/update-password")
//    public ResponseEntity<String> updatePassword(@RequestParam Long userId,
//                                                 @RequestParam String password) {
//        try {
//            userService.updatePassword(userId, password);
//            return ResponseEntity.ok("Password updated successfully");
//        } catch (UnauthorizedAdminOperationException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }
//
//    /**
//     * Manually trigger token cleanup
//     * POST http://localhost:8080/api/admin/tokens/cleanup
//     */
//    @PostMapping("/tokens/cleanup")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Map<String, Object>> manualCleanup() {
//        tokenCleanupService.cleanupExpiredAndUsedTokens();
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Token cleanup triggered successfully");
//        response.put("timestamp", java.time.Instant.now());
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Get token statistics
//     * GET http://localhost:8080/api/admin/tokens/stats
//     */
//    @GetMapping("/tokens/stats")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Map<String, Object>> getTokenStats() {
//        TokenCleanupService.TokenCleanupStats stats = tokenCleanupService.getCleanupStats();
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("totalTokens", stats.totalTokens());
//        response.put("expiredTokens", stats.expiredTokens());
//        response.put("usedTokens", stats.usedTokens());
//        response.put("validTokens", stats.getValidTokens());
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Cleanup only expired tokens
//     * POST http://localhost:8080/api/admin/tokens/cleanup-expired
//     */
//    @PostMapping("/tokens/cleanup-expired")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Map<String, Object>> cleanupExpiredOnly() {
//        int deleted = tokenCleanupService.cleanupExpiredTokens();
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("deletedCount", deleted);
//        response.put("type", "expired");
//        response.put("message", "Deleted " + deleted + " expired tokens");
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Cleanup only used tokens
//     * POST http://localhost:8080/api/admin/tokens/cleanup-used
//     */
//    @PostMapping("/tokens/cleanup-used")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Map<String, Object>> cleanupUsedOnly() {
//        int deleted = tokenCleanupService.cleanupUsedTokens();
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("deletedCount", deleted);
//        response.put("type", "used");
//        response.put("message", "Deleted " + deleted + " used tokens");
//
//        return ResponseEntity.ok(response);
//    }
//}