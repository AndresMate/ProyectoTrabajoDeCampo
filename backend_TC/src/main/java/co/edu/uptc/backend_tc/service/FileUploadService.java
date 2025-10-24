package co.edu.uptc.backend_tc.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {

    private final Cloudinary cloudinary;

    public FileUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadIdCard(MultipartFile file) throws IOException {
        log.info("=== INICIANDO UPLOAD A CLOUDINARY ===");
        log.info("📄 Archivo: {}", file.getOriginalFilename());
        log.info("📊 Tamaño: {} bytes", file.getSize());
        log.info("🎨 Content-Type: {}", file.getContentType());

        try {
            // Validaciones
            if (file.isEmpty()) {
                throw new IllegalArgumentException("El archivo está vacío");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("El archivo supera los 5MB");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Solo se permiten imágenes");
            }

            // Generar ID único
            String publicId = "uptc/carnets/" + UUID.randomUUID().toString();
            log.info("🔤 Public ID: {}", publicId);

            // Subir archivo a Cloudinary
            log.info("⬆️  Subiendo a Cloudinary...");
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "uptc-carnets",
                            "resource_type", "image",
                            "overwrite", false,
                            "use_filename", false
                    ));

            // Obtener URL segura
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("✅ Archivo subido exitosamente");
            log.info("🔗 URL: {}", secureUrl);
            log.info("=== UPLOAD COMPLETADO ===");

            return secureUrl;

        } catch (IllegalArgumentException e) {
            log.error("⚠️  Validación fallida: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("❌ Error de IO al subir archivo: {}", e.getMessage(), e);
            throw new IOException("Error al subir archivo: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Error inesperado: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado al subir archivo: " + e.getMessage(), e);
        }
    }

    // Método opcional para eliminar archivos
    public void deleteIdCard(String publicId) throws IOException {
        try {
            log.info("🗑️  Eliminando archivo: {}", publicId);
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("✅ Archivo eliminado: {}", result.get("result"));
        } catch (IOException e) {
            log.error("❌ Error al eliminar archivo: {}", e.getMessage(), e);
            throw new IOException("Error al eliminar archivo: " + e.getMessage(), e);
        }
    }
}