package cl.duoc.miapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Mismos datos que pide el formulario de registro del frontend (Profile)
@Setter
@Getter
@Entity
// "user" es palabra reservada en MySQL, por eso la tabla se llama "users"
@Table(name = "users")
public class User {

    // Intentos fallidos de contraseña antes de bloquear la cuenta (igual que el frontend)
    public static final int MAX_FAILED_ATTEMPTS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Aquí se guarda el hash BCrypt, nunca la contraseña en texto plano
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDate birthDate;

    // Opcional, formato +56912345678
    @Column(length = 12)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String comuna;

    // femenino | masculino | otro | prefiero-no-decir
    @Column(nullable = false, length = 20)
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Permite desactivar usuarios sin borrarlos
    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private int failedAttempts = 0;

    // Se activa al llegar a MAX_FAILED_ATTEMPTS; solo un administrador la desbloquea
    @Column(nullable = false)
    private boolean locked = false;

    // Constancia de cuándo aceptó las condiciones de registro.
    // Es null en los usuarios que no se registraron solos (ej. los de prueba).
    private LocalDateTime termsAcceptedAt;

    public User() {
    }
}
