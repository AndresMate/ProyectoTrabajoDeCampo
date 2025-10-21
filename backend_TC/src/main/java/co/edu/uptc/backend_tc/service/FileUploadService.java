package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.config.GoogleDriveProperties; // Importa la config
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent; // ¡Cambio importante!
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct; // ¡Importante!
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {

    private final GoogleDriveProperties driveProperties;
    private Drive driveService; // ¡El servicio ahora es un campo de la clase!

    // Inyecta las propiedades por constructor (buena práctica)
    public FileUploadService(GoogleDriveProperties driveProperties) {
        this.driveProperties = driveProperties;
    }

    /**
     * Este método se ejecuta una sola vez después de que el servicio es creado.
     * Se encarga de inicializar el cliente de Google Drive de forma segura.
     */
    @PostConstruct
    public void init() {
        log.info("Inicializando servicio de Google Drive...");
        try (InputStream credentialsStream = driveProperties.getCredentials().getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(credentialsStream)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/drive.file"));

            // El servicio se crea una vez y se reutiliza
            this.driveService = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("UPTC-Tournaments")
                    .build();
            log.info("Servicio de Google Drive inicializado exitosamente.");

        } catch (IOException | GeneralSecurityException e) {
            log.error("Error al inicializar el servicio de Google Drive: {}", e.getMessage(), e);
            // Si el servicio no puede iniciar, la aplicación debería fallar.
            throw new RuntimeException("No se pudo inicializar Google Drive", e);
        }
    }

    /**
     * Sube el archivo usando el 'driveService' ya inicializado
     * y sin crear archivos temporales.
     */
    public String uploadIdCard(MultipartFile file) throws IOException {
        log.info("Iniciando upload de archivo: {}", file.getOriginalFilename());

        try {
            // 1. Metadata del archivo
            String uniqueFileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            File fileMetadata = new File();
            fileMetadata.setName(uniqueFileName);
            fileMetadata.setParents(Collections.singletonList(driveProperties.getFolderId()));

            // 2. ¡CAMBIO CLAVE! Usar InputStreamContent
            // Se pasa el InputStream del MultipartFile directamente.
            InputStreamContent mediaContent = new InputStreamContent(
                    file.getContentType(),
                    file.getInputStream() // No se crea archivo temporal
            );
            // Opcional: setLength mejora el rendimiento si se conoce el tamaño
            // mediaContent.setLength(file.getSize());

            // 3. Subir archivo (usando el servicio reutilizado)
            log.info("Subiendo archivo a Google Drive...");
            File uploadedFile = this.driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id") // Solo pedimos el ID, es lo único que usamos
                    .execute();

            log.info("Archivo subido a Drive. ID: {}", uploadedFile.getId());

            // 4. Hacer el archivo público
            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");

            this.driveService.permissions()
                    .create(uploadedFile.getId(), permission)
                    .execute();

            log.info("Permisos públicos configurados correctamente");

            // 5. Construir URL directa (sin cambios)
            String fileUrl = "https://drive.google.com/uc?export=view&id=" + uploadedFile.getId();
            log.info("Upload completado. URL del archivo: {}", fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("Error de IO durante el upload: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar el archivo: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado durante el upload: {}", e.getMessage(), e);
            throw new RuntimeException("Error al subir archivo a Google Drive: " + e.getMessage(), e);
        }
        // ¡Ya no es necesario el bloque 'finally' para borrar el archivo temporal!
    }
}