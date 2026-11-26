package cl.duoc.miapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Cuerpo de POST /api/v1/auth/refresh y /api/v1/auth/logout
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshRequest {
    @NotBlank(message = "Falta el refresh token.")
    private String refreshToken;
}
