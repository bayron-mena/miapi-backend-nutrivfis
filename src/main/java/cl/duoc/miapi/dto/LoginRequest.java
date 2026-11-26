package cl.duoc.miapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// En el login solo se exige que vengan los datos: si el correo o la clave no
// calzan con una cuenta, AuthService responde 401 igual.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Completa este campo.")
    private String email;
    @NotBlank(message = "Completa este campo.")
    private String password;
}
