package cl.duoc.miapi.service;

import cl.duoc.miapi.config.ChileRegions;
import cl.duoc.miapi.config.FieldValidationException;
import cl.duoc.miapi.dto.UserRequest;
import cl.duoc.miapi.dto.UserResponse;
import cl.duoc.miapi.model.Role;
import cl.duoc.miapi.model.User;
import cl.duoc.miapi.repository.ReceiptRepository;
import cl.duoc.miapi.repository.RefreshTokenRepository;
import cl.duoc.miapi.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Gestión de usuarios (Admin) y perfil propio (/users/me)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReceiptRepository receiptRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ReceiptRepository receiptRepository,
                       RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.receiptRepository = receiptRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findMe(String email) {
        return UserResponse.from(byEmail(email));
    }

    // Admin crea un usuario con cualquier rol (el registro público solo crea CLIENT)
    @Transactional
    public UserResponse create(UserRequest request) {
        validate(request);
        String email = normalize(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado.");
        }
        if (isBlank(request.getPassword())) {
            throw new FieldValidationException(Map.of("password",
                    "La contraseña debe tener entre 4 y 13 caracteres y al menos una mayúscula."));
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(isBlank(request.getRole()) ? Role.CLIENT : Role.valueOf(request.getRole()));
        copyPersonalData(request, user);
        return UserResponse.from(userRepository.save(user));
    }

    // Admin edita un usuario: datos, rol, clave (opcional) y desbloqueo
    @Transactional
    public UserResponse update(String email, UserRequest request) {
        validate(request);
        User user = byEmail(email);
        String newEmail = normalize(request.getEmail());
        if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado.");
        }
        user.setEmail(newEmail);
        if (!isBlank(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (!isBlank(request.getRole())) {
            user.setRole(Role.valueOf(request.getRole()));
        }
        if (Boolean.FALSE.equals(request.getLocked())) {
            user.setLocked(false);
            user.setFailedAttempts(0);
        }
        copyPersonalData(request, user);
        return UserResponse.from(userRepository.save(user));
    }

    // El usuario edita sus datos personales. El correo no cambia aquí: identifica
    // la sesión (es el "sub" del JWT). Rol, clave y bloqueo tampoco.
    @Transactional
    public UserResponse updateMe(String email, UserRequest request) {
        request.setEmail(email);
        validate(request);
        User user = byEmail(email);
        copyPersonalData(request, user);
        return UserResponse.from(userRepository.save(user));
    }

    // Un usuario con compras no se borra: sus boletas deben seguir existiendo
    @Transactional
    public void delete(String email) {
        User user = byEmail(email);
        if (receiptRepository.existsByUserId(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el usuario tiene compras registradas.");
        }
        refreshTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    private void copyPersonalData(UserRequest r, User u) {
        u.setName(r.getName().trim());
        u.setLastName(r.getLastName() == null ? "" : r.getLastName().trim());
        u.setBirthDate(r.getBirthDate());
        u.setPhone(isBlank(r.getPhone()) || "+".equals(r.getPhone()) ? null : r.getPhone());
        u.setAddress(r.getAddress().trim());
        u.setRegion(r.getRegion());
        u.setComuna(r.getComuna());
        u.setGender(r.getGender());
    }

    // Mismas reglas extra que el registro (AuthService)
    private void validate(UserRequest r) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (r.getBirthDate() != null && r.getBirthDate().getYear() > AuthService.MAX_BIRTH_YEAR) {
            errors.put("birthDate", "Debes haber nacido el año " + AuthService.MAX_BIRTH_YEAR
                    + " o antes (mínimo 14 años).");
        }
        if (!ChileRegions.isValidRegion(r.getRegion())) {
            errors.put("region", "Selecciona una opción de la lista.");
        } else if (!ChileRegions.isValidComuna(r.getRegion(), r.getComuna())) {
            errors.put("comuna", "Selecciona una opción de la lista.");
        }
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }

    private User byEmail(String email) {
        return userRepository.findByEmail(normalize(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
