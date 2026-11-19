package cl.duoc.miapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank
    String nombre;
    @NotBlank
    @Email
    String email;
    @NotBlank
    @Size(min = 8, max = 72)
    String password;
}

