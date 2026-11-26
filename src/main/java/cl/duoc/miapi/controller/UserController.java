package cl.duoc.miapi.controller;

import cl.duoc.miapi.dto.UserRequest;
import cl.duoc.miapi.dto.UserResponse;
import cl.duoc.miapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Orígenes permitidos en application.properties (app.cors.allowed-origins)
@CrossOrigin(originPatterns = "${app.cors.allowed-origins}")
@RequestMapping("/api/v1/users")
@Tag(name = "Usuarios", description = "Gestión de usuarios (Admin) y perfil propio")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ---- Perfil propio (cualquier usuario con sesión) ----

    @Operation(summary = "Datos del usuario conectado")
    @GetMapping("/me")
    public UserResponse me(Authentication auth) {
        return userService.findMe(auth.getName());
    }

    @Operation(summary = "Edita los datos personales del usuario conectado (no cambia correo, rol ni clave)")
    @PutMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UserRequest request, Authentication auth) {
        return userService.updateMe(auth.getName(), request);
    }

    // ---- Gestión (solo Admin, ver SecurityConfig) ----

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @Operation(summary = "Crea un usuario con cualquier rol")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @Operation(summary = "Edita un usuario; locked=false lo desbloquea y password vacía no la cambia")
    @PutMapping("/{email}")
    public UserResponse update(@PathVariable String email, @Valid @RequestBody UserRequest request) {
        return userService.update(email, request);
    }

    @Operation(summary = "Elimina un usuario sin compras")
    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String email) {
        userService.delete(email);
    }
}
