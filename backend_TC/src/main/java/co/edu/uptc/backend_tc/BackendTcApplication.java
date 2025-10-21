package co.edu.uptc.backend_tc;

import co.edu.uptc.backend_tc.config.GoogleDriveProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GoogleDriveProperties.class)
public class BackendTcApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendTcApplication.class, args);
    }

}
