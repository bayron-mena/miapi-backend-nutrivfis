package cl.duoc.miapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse{
    String token;
    String tipo;
    String nombre;
    String email;
    String rol;
}