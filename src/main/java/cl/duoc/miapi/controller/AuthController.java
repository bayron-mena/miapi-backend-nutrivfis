package cl.duoc.miapi.controller;

import cl.duoc.miapi.dto.AuthResponse;
import cl.duoc.miapi.dto.LoginRequest;
import cl.duoc.miapi.dto.RefreshRequest;
import cl.duoc.miapi.dto.RegisterRequest;
import cl.duoc.miapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
// Orígenes permitidos en application.properties (app.cors.allowed-origins)
@CrossOrigin(originPatterns = "${app.cors.allowed-origins}")
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro e inicio de sesión con JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Crea una cuenta de cliente y devuelve su JWT")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Valida correo y contraseña y devuelve un JWT")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Entrega un JWT nuevo a partir del refresh token",
            description = "El refresh token sirve una sola vez: la respuesta trae uno nuevo que "
                    + "reemplaza al anterior. No necesita el JWT, porque se usa cuando ya venció.")
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.getRefreshToken());
    }

    @Operation(summary = "Cierra la sesión invalidando el refresh token")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
    }
}
