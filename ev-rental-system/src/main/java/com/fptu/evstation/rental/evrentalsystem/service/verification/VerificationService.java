package com.fptu.evstation.rental.evrentalsystem.service.verification;

import com.fptu.evstation.rental.evrentalsystem.dto.UploadVerificationRequest;
import com.fptu.evstation.rental.evrentalsystem.dto.VerifyRequest;
import com.fptu.evstation.rental.evrentalsystem.entity.User;
import com.fptu.evstation.rental.evrentalsystem.entity.VerificationStatus;
import com.fptu.evstation.rental.evrentalsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {
    private final UserRepository userRepo;
    // Folder lưu file (base)
    private final Path uploadBaseDir = Paths.get("uploads/verification");
    @Transactional
    public String uploadVerification(User user, UploadVerificationRequest req) {
        // Cho phép upload nếu PENDING hoặc REJECTED (re-upload)
        if (user.getVerificationStatus() == VerificationStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tài khoản đã được xác thực, không cần upload lại");
        }
        // Tạo folder riêng cho user: uploads/verification/user_{userId}
        Path userDir = uploadBaseDir.resolve("user_" + user.getUserId());
        try {
            if (!Files.exists(userDir)) {
                Files.createDirectories(userDir);
            } else {
                // FIXED: Clear folder cũ nếu re-upload (xóa files cũ)
                File[] oldFiles = userDir.toFile().listFiles();
                if (oldFiles != null) {
                    for (File oldFile : oldFiles) {
                        if (!oldFile.delete()) {
                            log.warn("Không thể xóa file cũ: " + oldFile.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Lỗi tạo/xóa folder user: " + userDir, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống khi xử lý folder");
        }
        // Nếu REJECTED, reset status
        if (user.getVerificationStatus() == VerificationStatus.REJECTED) {
            user.setRejectionReason(null);
            log.info("User {} re-upload sau reject, reset status", user.getUserId());
        }
        user.setVerificationStatus(VerificationStatus.PENDING);
        // Validate files không null
        if (req.getCccdFile() == null || req.getGplxFile() == null || req.getSelfieFile() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng upload đầy đủ CCCD, GPLX và selfie");
        }
        // Validate type (chỉ image)
        validateFileType(req.getCccdFile(), "CCCD");
        validateFileType(req.getGplxFile(), "GPLX");
        validateFileType(req.getSelfieFile(), "Selfie");
        // Lưu files với tên đơn giản (overwrite)
        String cccdPath = saveFile(req.getCccdFile(), userDir, "cccd");
        String gplxPath = saveFile(req.getGplxFile(), userDir, "gplx");
        String selfiePath = saveFile(req.getSelfieFile(), userDir, "selfie");
        // Gán đường dẫn trả về từ saveFile() (đã là relative, có đuôi đúng)
        user.setCccdPath(cccdPath);
        user.setGplxPath(gplxPath);
        user.setSelfiePath(selfiePath);

        userRepo.save(user);
        return "Yêu cầu đã gửi. Vui lòng chờ nhân viên xác nhận trong vòng 24h. Bạn có thể xem xe nhưng chưa thể đặt thuê.";
    }
    private void validateFileType(MultipartFile file, String type) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, type + " phải là file ảnh (jpg/png)");
        }
        if (file.getSize() > 5 * 1024 * 1024) {  // 5MB max
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, type + " không vượt quá 5MB");
        }
    }


    private String saveFile(MultipartFile file, Path userDir, String fileName) {
        try {
            Files.createDirectories(userDir);

            String extension = ".jpg";
            String orig = file.getOriginalFilename();
            if (orig != null && orig.lastIndexOf('.') >= 0) {
                extension = orig.substring(orig.lastIndexOf('.')).toLowerCase();
            } else {
                extension = "." + detectExtension(file);
            }

            String finalName = fileName + extension;
            Path filePath = userDir.resolve(finalName);
            file.transferTo(filePath);

            // Trả relative path (cho DB và frontend)
            return "/uploads/verification/" + userDir.getFileName().toString() + "/" + finalName;

        } catch (IOException e) {
            log.error("Lỗi lưu file", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi lưu file");
        }
    }
    public List<User> getPendingVerifications() {
        return userRepo.findByVerificationStatus(VerificationStatus.PENDING);
    }
    @Transactional
    public String verifyUser(int userId, VerifyRequest req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));
        if (user.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ (chỉ xử lý PENDING)");
        }
        if (!req.isApproved()) {
            if (req.getReason() == null || req.getReason().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phải nhập lý do khi từ chối (ví dụ: ảnh mờ, không khớp thông tin)");
            }
            user.setVerificationStatus(VerificationStatus.REJECTED);
            user.setRejectionReason(req.getReason());
            userRepo.save(user);
            return "Từ chối thành công. Lý do: " + req.getReason() + ". User có thể upload lại.";
        } else {
            user.setVerificationStatus(VerificationStatus.APPROVED);
            user.setRejectionReason(null);
            userRepo.save(user);
            return "Xác nhận thành công. User giờ có thể thuê xe đầy đủ.";
        }
    }
    public Map<String, Object> getVerificationStatus(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", user.getVerificationStatus().name());
        if (user.getVerificationStatus() == VerificationStatus.REJECTED && user.getRejectionReason() != null) {
            response.put("reason", user.getRejectionReason());
        }
        return response;
    }

    // helper: determine extension safely
    private String detectExtension(MultipartFile file) {
        if (file == null) return "jpg";
        String orig = file.getOriginalFilename();
        if (orig != null) {
            int idx = orig.lastIndexOf('.');
            if (idx >= 0 && idx < orig.length() - 1) {
                return orig.substring(idx + 1).toLowerCase();
            }
        }
        String ct = file.getContentType();
        if (ct != null) {
            if (ct.contains("png")) return "png";
            if (ct.contains("jpeg") || ct.contains("jpg")) return "jpg";
            if (ct.contains("gif")) return "gif";
            if (ct.contains("webp")) return "webp";
        }
        return "jpg"; // safe fallback
    }

    // helper: store file and return relative url path (starts with /uploads/...)
    private String storeVerificationFile(MultipartFile file, Long userId, String fieldName) throws IOException {
        if (file == null || file.isEmpty()) throw new IOException("File null or empty");

        String ext = detectExtension(file);
        String fileName = fieldName + "." + ext; // e.g. cccd.png

        Path userDir = Paths.get(System.getProperty("user.dir"), "uploads", "verification", "user_" + userId).toAbsolutePath();
        Files.createDirectories(userDir);

        Path dest = userDir.resolve(fileName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/verification/user_" + userId + "/" + fileName;
    }
}