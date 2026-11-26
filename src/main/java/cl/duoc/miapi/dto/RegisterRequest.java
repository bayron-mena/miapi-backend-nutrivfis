package cl.duoc.miapi.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Mismas reglas y mensajes que src/utils/registro.ts y src/utils/validaciones.ts del frontend.
// La fecha de nacimiento (año <= 2012) y la comuna de la región se revisan en AuthService.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
    private String name;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres.")
    private String lastName;

    // CORREO_AGENDAMIENTO_REGEX del frontend (se compara en minúsculas)
    @NotBlank(message = "Completa este campo.")
    @Pattern(regexp = "^[A-Za-z0-9]{1,24}@(gmail\\.com|profesor\\.duoc\\.cl|duoc\\.cl)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Correo inválido. Máx. 24 letras/números antes de @, y solo @gmail.com, @duoc.cl o @profesor.duoc.cl.")
    private String email;

    // isValidPassword del frontend: 4 a 13 caracteres y al menos una mayúscula
    @NotBlank(message = "Completa este campo.")
    @Size(min = 4, max = 13,
            message = "La contraseña debe tener entre 4 y 13 caracteres y al menos una mayúscula.")
    @Pattern(regexp = ".*[A-Z].*",
            message = "La contraseña debe tener entre 4 y 13 caracteres y al menos una mayúscula.")
    private String password;

    // Formato yyyy-MM-dd, como lo entrega <input type="date">
    @NotNull(message = "Completa este campo.")
    private LocalDate birthDate;

    // Opcional. El frontend deja "+" cuando el campo está vacío; se acepta y se guarda como null.
    @Pattern(regexp = "^(\\+|\\+56\\d{9})?$",
            message = "Teléfono inválido. Solo números, con el formato +56934020512.")
    private String phone;

    @NotBlank(message = "Completa este campo.")
    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres.")
    private String address;

    @NotBlank(message = "Selecciona una opción de la lista.")
    private String region;

    @NotBlank(message = "Selecciona una opción de la lista.")
    private String comuna;

    @NotBlank(message = "Selecciona una opción de la lista.")
    @Pattern(regexp = "^(femenino|masculino|otro|prefiero-no-decir)$",
            message = "Selecciona una opción de la lista.")
    private String gender;

    // Checkbox "aceptaTerminos" del frontend. Se valida aquí también porque la API
    // se puede llamar sin pasar por el formulario. (confirmPassword no se envía:
    // es solo una ayuda del formulario.)
    @NotNull(message = "Debes aceptar las condiciones de registro.")
    @AssertTrue(message = "Debes aceptar las condiciones de registro.")
    private Boolean acceptTerms;
}
