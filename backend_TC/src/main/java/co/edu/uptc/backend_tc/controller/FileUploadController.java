package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*") // Ajusta según tu frontend
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping(value = "/upload/id-card", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadIdCard(@RequestPart("file") MultipartFile file) {
        log.info("=== PETICIÓN RECIBIDA ===");
        log.info("📨 Endpoint: /api/files/upload/id-card");
        log.info("📄 Archivo: {}", file != null ? file.getOriginalFilename() : "NULL");

        Map<String, Object> response = new HashMap<>();

        try {
            // Validación básica
            if (file == null || file.isEmpty()) {
                log.warn("⚠️  Archivo vacío o nulo");
                response.put("success", false);
                response.put("error", "El archivo es requerido");
                return ResponseEntity.badRequest().body(response);
            }

            // Subir archivo
            String url = fileUploadService.uploadIdCard(file);

            // Respuesta exitosa
            response.put("success", true);
            response.put("url", url);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("message", "Archivo subido exitosamente");

            log.info("✅ RESPUESTA EXITOSA");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️  Validación fallida: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("❌ ERROR EN CONTROLLER: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Error al procesar el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "File Upload Service");
        return ResponseEntity.ok(response);
    }
}