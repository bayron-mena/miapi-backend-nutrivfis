package cl.duoc.miapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

// Token de larga duración para pedir un JWT nuevo sin volver a escribir la contraseña.
// Se guarda solo su hash SHA-256: si alguien lee la base, no puede usar los tokens.
@Setter
@Getter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    // EAGER: AuthService usa el usuario después de que termina la transacción
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    // Se marca al usarlo (rotación) o al cerrar sesión. Un token revocado no sirve más.
    private Instant revokedAt;

    // true = se revocó porque se cambió por uno nuevo en /refresh. Si un token rotado
    // vuelve a usarse, es señal de robo. Uno cerrado con logout no cuenta como robo.
    @Column(nullable = false)
    private boolean rotated = false;

    public boolean isActive() {
        return revokedAt == null && expiresAt.isAfter(Instant.now());
    }
}
