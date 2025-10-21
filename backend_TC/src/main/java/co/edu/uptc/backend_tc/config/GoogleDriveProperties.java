package co.edu.uptc.backend_tc.config; // (o donde pongas tus configs)

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

// Esta anotación le dice a Spring que cargue propiedades
// que empiecen con "google.drive" en esta clase.
@ConfigurationProperties(prefix = "google.drive")
public class GoogleDriveProperties {

    private String folderId;
    private Resource credentials; // El .properties debe ser google.drive.credentials

    // --- Getters y Setters son necesarios ---

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public Resource getCredentials() {
        return credentials;
    }

    public void setCredentials(Resource credentials) {
        this.credentials = credentials;
    }
}