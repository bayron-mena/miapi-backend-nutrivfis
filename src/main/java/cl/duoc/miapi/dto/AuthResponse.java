package cl.duoc.miapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Respuesta de register, login y refresh. Trae lo mismo que la "Session" del frontend,
// para no tener que pedir el perfil aparte.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    // JWT corto: va en el header "Authorization: Bearer <token>"
    private String token;
    // Siempre "Bearer"
    private String type;
    // Segundos que dura el token
    private long expiresIn;
    // Token largo para pedir un JWT nuevo en /api/v1/auth/refresh (se usa una sola vez)
    private String refreshToken;
    // Segundos que dura el refresh token
    private long refreshExpiresIn;
    private String name;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private String phone;
    // ADMIN | SELLER | CLIENT
    private String role;
}
