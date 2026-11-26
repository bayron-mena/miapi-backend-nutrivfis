package cl.duoc.miapi.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Por defecto Spring Security responde 401/403 sin cuerpo. Así el frontend
// recibe { "status": ..., "message": "..." } igual que en los demás errores.
public final class JsonSecurityHandlers {

    private JsonSecurityHandlers() {
    }

    // 401: no mandó token, o el token no sirve (vencido, mal formado, firma inválida)
    public static AuthenticationEntryPoint unauthorized() {
        return (request, response, ex) -> {
            boolean badToken = ex instanceof InvalidBearerTokenException;
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE,
                    badToken ? "Bearer error=\"invalid_token\"" : "Bearer");
            write(response, HttpServletResponse.SC_UNAUTHORIZED,
                    badToken
                            ? "La sesión expiró o el token no es válido. Inicia sesión nuevamente."
                            : "Debes iniciar sesión para acceder a este recurso.");
        };
    }

    // 403: tiene sesión, pero su rol no permite la acción
    public static AccessDeniedHandler forbidden() {
        return (request, response, ex) -> write(response, HttpServletResponse.SC_FORBIDDEN,
                "No tienes permisos para realizar esta acción.");
    }

    private static void write(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Los mensajes son fijos (sin comillas), así que se puede armar el JSON a mano
        response.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
