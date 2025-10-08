package com.fptu.evstation.rental.evrentalsystem.controller;

import com.fptu.evstation.rental.evrentalsystem.dto.VerifyRequest;
import com.fptu.evstation.rental.evrentalsystem.entity.User;
import com.fptu.evstation.rental.evrentalsystem.service.auth.AuthService;
import com.fptu.evstation.rental.evrentalsystem.service.verification.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {
    private final AuthService authService;
    private final VerificationService verificationService;
    /** Lấy list pending verifications */
    @GetMapping("/pending-verifications")
    public ResponseEntity<?> getPendingVerifications(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Thiếu token");
        }
        String token = authHeader.substring(7);
        User staff = authService.validateTokenAndGetUser(token);
// Check role STATION_STAFF
        if (!staff.getRole().getRoleName().equals("STATION_STAFF")) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Không có quyền");
        }
        List<User> pendings = verificationService.getPendingVerifications();
        return ResponseEntity.ok(pendings);  // Trả full info (ảnh paths cho frontend)
    }
    /** Xác nhận/ từ chối verification (reason bắt buộc khi reject) */
    @PostMapping("/verify/{userId}")
    public ResponseEntity<?> verifyUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable int userId,
            @RequestBody VerifyRequest req) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Thiếu token");
        }
        String token = authHeader.substring(7);
        User staff = authService.validateTokenAndGetUser(token);
        if (!staff.getRole().getRoleName().equals("STATION_STAFF")) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Không có quyền");
        }
        String message = verificationService.verifyUser(userId, req);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
