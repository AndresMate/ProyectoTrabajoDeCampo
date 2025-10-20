// backend_TC/src/main/java/co/edu/uptc/backend_tc/controller/FileUploadController.java
package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.FileUploadResponseDTO;
import co.edu.uptc.backend_tc.service.GoogleDriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Archivos", description = "Operaciones para subir archivos (carnets)")
public class FileUploadController {

    private final GoogleDriveService googleDriveService;

    @Operation(summary = "Subir foto de carnet",
            description = "Sube una imagen del carnet de estudiante a Google Drive. Formatos permitidos: JPG, JPEG, PNG. Tamaño máximo: 5MB")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archivo subido exitosamente"),
            @ApiResponse(responseCode = "400", description = "Archivo inválido (formato o tamaño)"),
            @ApiResponse(responseCode = "500", description = "Error al subir el archivo")
    })
    @PostMapping(value = "/upload/id-card", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDTO> uploadIdCard(
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = googleDriveService.uploadFile(file);

            FileUploadResponseDTO response = FileUploadResponseDTO.builder()
                    .fileUrl(fileUrl)
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .uploadedAt(OffsetDateTime.now().toString())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}