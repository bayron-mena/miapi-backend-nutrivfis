package cl.duoc.miapi.config;

import java.util.Map;

// Para validaciones que no caben en una anotación (ej. la comuna debe ser de la región).
// GlobalExceptionHandler la responde como 400 con el mismo formato que @Valid.
public class FieldValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public FieldValidationException(Map<String, String> errors) {
        super("Hay datos inválidos");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
