package cl.duoc.miapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Crear o editar un usuario desde el panel (Admin) o editar el perfil propio (/users/me).
// Mismas reglas que RegisterRequest. Diferencias:
// - password es opcional al editar (vacío = no se cambia); al crear es obligatoria.
// - role y locked solo los usa el Admin; en /users/me se ignoran.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
    private String name;

    // Puede quedar vacío (el admin inicial del frontend no tenía apellido)
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres.")
    private String lastName;

    @NotBlank(message = "Completa este campo.")
    @Pattern(regexp = "^[A-Za-z0-9]{1,24}@(gmail\\.com|profesor\\.duoc\\.cl|duoc\\.cl)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Correo inválido. Máx. 24 letras/números antes de @, y solo @gmail.com, @duoc.cl o @profesor.duoc.cl.")
    private String email;

    @Pattern(regexp = "^$|^(?=.*[A-Z]).{4,13}$",
            message = "La contraseña debe tener entre 4 y 13 caracteres y al menos una mayúscula.")
    private String password;

    @NotNull(message = "Completa este campo.")
    private LocalDate birthDate;

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

    // ADMIN | SELLER | CLIENT (solo Admin)
    @Pattern(regexp = "^(ADMIN|SELLER|CLIENT)?$", message = "Rol inválido.")
    private String role;

    // false = desbloquear la cuenta (solo Admin)
    private Boolean locked;
}
