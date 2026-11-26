package cl.duoc.miapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Única configuración de Swagger. El esquema "bearerAuth" agrega el botón
// "Authorize" en /swagger-ui.html para probar los endpoints con el JWT.
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Clínica NutriVifs",
                version = "v1",
                description = "API REST para la gestión de la clínica veterinaria, con JWT y control de acceso por roles."
        ),
        // Envía el token en todas las peticiones de Swagger (los endpoints públicos lo ignoran)
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingrese el JWT obtenido al iniciar sesión"
)
public class OpenApiConfig {
}
