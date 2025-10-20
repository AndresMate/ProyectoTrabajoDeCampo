// backend_TC/src/main/java/co/edu/uptc/backend_tc/service/GoogleDriveService.java
package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.exception.BadRequestException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.UUID;

@Service
@Slf4j
public class GoogleDriveService {

    @Value("${google.drive.folder-id}")
    private String folderId;

    @Value("${google.credentials.path}")
    private String credentialsPath;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String uploadFile(MultipartFile file) throws IOException, GeneralSecurityException {
        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (!isValidImageType(contentType)) {
            throw new BadRequestException("Invalid file type. Only JPG, JPEG, and PNG are allowed");
        }

        // Crear archivo temporal
        Path tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        try {
            // Configurar Google Drive
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new FileInputStream(credentialsPath)
            ).createScoped(Collections.singleton("https://www.googleapis.com/auth/drive.file"));

            Drive driveService = new Drive.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName("Tournament System").build();

            // Crear metadata del archivo
            File fileMetadata = new File();
            fileMetadata.setName(generateUniqueFileName(file.getOriginalFilename()));
            fileMetadata.setParents(Collections.singletonList(folderId));

            // Subir archivo
            FileContent mediaContent = new FileContent(contentType, tempFile.toFile());
            File uploadedFile = driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink, webContentLink")
                    .execute();

            log.info("File uploaded successfully: {}", uploadedFile.getId());

            // Retornar URL pública del archivo
            return String.format("https://drive.google.com/uc?id=%s", uploadedFile.getId());

        } finally {
            // Limpiar archivo temporal
            Files.deleteIfExists(tempFile);
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png")
        );
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalFilename.substring(lastDot);
        }
        return UUID.randomUUID().toString() + extension;
    }
}