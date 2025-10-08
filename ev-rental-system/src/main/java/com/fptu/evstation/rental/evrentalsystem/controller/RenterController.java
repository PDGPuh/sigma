package com.fptu.evstation.rental.evrentalsystem.controller;

import com.fptu.evstation.rental.evrentalsystem.dto.UploadVerificationRequest;
import com.fptu.evstation.rental.evrentalsystem.entity.User;
import com.fptu.evstation.rental.evrentalsystem.service.auth.AuthService;
import com.fptu.evstation.rental.evrentalsystem.service.verification.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/renter")
@RequiredArgsConstructor
public class RenterController {
    private final AuthService authService;
    private final VerificationService verificationService;
    /** Upload giấy tờ sau login (có thể re-upload nếu rejected) */
    @PostMapping("/upload-verification")
    public ResponseEntity<?> uploadVerification(
            @RequestHeader("Authorization") String authHeader,
            @ModelAttribute UploadVerificationRequest req) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Thiếu token");
        }
        String token = authHeader.substring(7);
        User user = authService.validateTokenAndGetUser(token);
        // Check role EV_RENTER
        if (!user.getRole().getRoleName().equals("EV_RENTER")) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Không có quyền");
        }
        String message = verificationService.uploadVerification(user, req);
        return ResponseEntity.ok(Map.of("message", message));
    }
    /** THÊM: Check trạng thái verification + reason nếu rejected */
    @GetMapping("/verification-status")
    public ResponseEntity<?> getVerificationStatus(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Thiếu token");
        }
        String token = authHeader.substring(7);
        User user = authService.validateTokenAndGetUser(token);
        if (!user.getRole().getRoleName().equals("EV_RENTER")) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Không có quyền");
        }
        Map<String, Object> status = verificationService.getVerificationStatus(user);
        return ResponseEntity.ok(status);
    }
}

