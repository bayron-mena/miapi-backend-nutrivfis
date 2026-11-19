package cl.duoc.miapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Autenticación JWT",
                version = "1.0",
                description = "API educativa con Spring Boot" +
                        " para mi curso favorito, tiene" +
                        "JWT y control de acceso por roles."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingrese el JWT obtenido al iniciar sesión"
)
public class SwaggerConfig {
}
