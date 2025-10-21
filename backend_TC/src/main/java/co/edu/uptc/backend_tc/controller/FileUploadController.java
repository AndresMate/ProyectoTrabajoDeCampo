package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload/id-card")
    public ResponseEntity<?> uploadIdCard(@RequestParam("file") MultipartFile file) {
        try {
            // Validaciones básicas
            if (file.isEmpty()) {
                log.error("Archivo vacío recibido");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El archivo está vacío"));
            }

            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.equals("image/jpeg") &&
                            !contentType.equals("image/jpg") &&
                            !contentType.equals("image/png"))) {
                log.error("Tipo de archivo no permitido: {}", contentType);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Solo se permiten archivos JPG, JPEG o PNG"));
            }

            // Validar tamaño (5MB máximo)
            if (file.getSize() > 5 * 1024 * 1024) {
                log.error("Archivo muy grande: {} bytes", file.getSize());
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El archivo no debe superar 5MB"));
            }

            log.info("Subiendo archivo: {} - Tamaño: {} bytes - Tipo: {}",
                    file.getOriginalFilename(), file.getSize(), contentType);

            // Subir archivo
            String fileUrl = fileUploadService.uploadIdCard(file);

            log.info("Archivo subido exitosamente: {}", fileUrl);

            // Retornar respuesta
            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            response.put("message", "Archivo subido exitosamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al subir archivo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el archivo: " + e.getMessage()));
        }
    }
}