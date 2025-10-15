package co.edu.uptc.backend_tc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API - Sistema de Gestión de Torneos Deportivos")
                        .version("1.0.0")
                        .description("Documentación del backend del sistema de torneos desarrollado por la Escuela de Ingeniería de Sistemas UPTC.")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo UPTC")
                                .email("blanco")));
    }
}
