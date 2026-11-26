package cl.duoc.miapi.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

// Todas las respuestas de error salen con el mismo formato JSON, para que el
// frontend pueda mostrar el mensaje:
// { "status": 409, "message": "Ya existe una cuenta con ese correo", "errors": {...} }
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
        return body(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), null);
    }

    // Falla de @Valid: devuelve el mensaje de cada campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return body(HttpStatus.BAD_REQUEST, "Hay datos inválidos", errors);
    }

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<Map<String, Object>> handleFieldValidation(FieldValidationException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrors());
    }

    // Parámetro de la URL con formato incorrecto (ej. ?from=30-11-2026 o /receipts/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return body(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' no es válido. Las fechas van como yyyy-MM-dd.", null);
    }

    // JSON mal formado o con tipos incorrectos (ej. una fecha que no es yyyy-MM-dd)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return body(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es válido", null);
    }

    // Cualquier otro error: se registra en el log con el detalle y al cliente se le
    // responde un mensaje genérico (sin mostrar SQL ni trazas internas)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) throws Exception {
        // Deja pasar los errores de seguridad para que respondan 401/403 como corresponde
        if (ex instanceof org.springframework.security.access.AccessDeniedException
                || ex instanceof org.springframework.security.core.AuthenticationException) {
            throw ex;
        }
        log.error("Error no controlado", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intenta nuevamente.", null);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message,
                                                     Map<String, String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("message", message != null ? message : status.getReasonPhrase());
        if (errors != null) {
            body.put("errors", errors);
        }
        return ResponseEntity.status(status).body(body);
    }
}
