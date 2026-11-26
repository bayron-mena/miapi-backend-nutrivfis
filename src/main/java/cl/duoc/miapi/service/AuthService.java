package cl.duoc.miapi.service;

import cl.duoc.miapi.config.ChileRegions;
import cl.duoc.miapi.config.FieldValidationException;
import cl.duoc.miapi.dto.AuthResponse;
import cl.duoc.miapi.dto.LoginRequest;
import cl.duoc.miapi.dto.RegisterRequest;
import cl.duoc.miapi.model.Role;
import cl.duoc.miapi.model.User;
import cl.duoc.miapi.repository.UserRepository;
import cl.duoc.miapi.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    // Igual que ANIO_MAXIMO_NACIMIENTO del frontend: nacido este año o antes = 14+ años
    public static final int MAX_BIRTH_YEAR = 2012;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    // Registro público: siempre crea un CLIENT. Los ADMIN y SELLER los crea un administrador.
    public AuthResponse register(RegisterRequest request) {
        validateRegister(request);

        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);
        // BCrypt: en la base queda solo el hash, nunca la contraseña
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBirthDate(request.getBirthDate());
        user.setPhone(normalizePhone(request.getPhone()));
        user.setAddress(request.getAddress().trim());
        user.setRegion(request.getRegion());
        user.setComuna(request.getComuna());
        user.setGender(request.getGender());
        user.setRole(Role.CLIENT);
        // @AssertTrue ya garantizó que acceptTerms es true
        user.setTermsAcceptedAt(LocalDateTime.now());
        userRepository.save(user);

        return buildResponse(user);
    }

    // Sin @Transactional a propósito: el contador de intentos fallidos debe
    // quedar guardado aunque después se lance la excepción.
    public AuthResponse login(LoginRequest request) {
        // Mismo mensaje si el correo no existe o la contraseña es incorrecta,
        // para no revelar qué correos están registrados
        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(this::invalidCredentials);

        checkAccountEnabled(user);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            user.setLocked(attempts >= User.MAX_FAILED_ATTEMPTS);
            userRepository.save(user);
            if (user.isLocked()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Cuenta bloqueada por " + User.MAX_FAILED_ATTEMPTS + " intentos fallidos.");
            }
            throw invalidCredentials();
        }

        if (user.getFailedAttempts() > 0) {
            user.setFailedAttempts(0);
            userRepository.save(user);
        }
        return buildResponse(user);
    }

    // Entrega un JWT nuevo y un refresh token nuevo; el que llegó queda inválido.
    // Si la cuenta se bloqueó o desactivó después del login, ya no puede renovar.
    public AuthResponse refresh(String refreshToken) {
        User user = refreshTokenService.consume(refreshToken);
        checkAccountEnabled(user);
        return buildResponse(user);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private void checkAccountEnabled(User user) {
        if (user.isLocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cuenta bloqueada por " + User.MAX_FAILED_ATTEMPTS
                            + " intentos fallidos. Contacta a soporte.");
        }
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está desactivada");
        }
    }

    // Reglas que no caben en una anotación del DTO
    private void validateRegister(RegisterRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (request.getBirthDate().getYear() > MAX_BIRTH_YEAR) {
            errors.put("birthDate", "Debes haber nacido el año " + MAX_BIRTH_YEAR
                    + " o antes (mínimo 14 años).");
        }
        if (!ChileRegions.isValidRegion(request.getRegion())) {
            errors.put("region", "Selecciona una opción de la lista.");
        } else if (!ChileRegions.isValidComuna(request.getRegion(), request.getComuna())) {
            errors.put("comuna", "Selecciona una opción de la lista.");
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
    }

    private AuthResponse buildResponse(User user) {
        return new AuthResponse(
                jwtService.generarToken(user),
                "Bearer",
                jwtService.expirationSeconds(),
                refreshTokenService.create(user),
                refreshTokenService.validitySeconds(),
                user.getName(),
                user.getLastName(),
                user.getEmail(),
                user.getBirthDate(),
                user.getPhone(),
                user.getRole().name());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    // "+" o vacío = sin teléfono
    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank() || phone.equals("+")) {
            return null;
        }
        return phone;
    }
}
