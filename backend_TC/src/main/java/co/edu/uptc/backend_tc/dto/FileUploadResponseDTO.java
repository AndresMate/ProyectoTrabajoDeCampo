package co.edu.uptc.backend_tc.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponseDTO {
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String uploadedAt;
}