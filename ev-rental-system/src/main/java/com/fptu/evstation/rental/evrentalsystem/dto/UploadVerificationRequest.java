package com.fptu.evstation.rental.evrentalsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadVerificationRequest {
    private MultipartFile cccdFile;
    private MultipartFile gplxFile;
    private MultipartFile selfieFile;
}
