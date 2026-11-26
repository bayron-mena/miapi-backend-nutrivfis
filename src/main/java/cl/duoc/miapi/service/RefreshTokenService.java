package cl.duoc.miapi.service;

import cl.duoc.miapi.model.RefreshToken;
import cl.duoc.miapi.model.User;
import cl.duoc.miapi.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration validity;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               @Value("${app.jwt.refresh-expiration-days}") long expirationDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.validity = Duration.ofDays(expirationDays);
    }

    // Crea un refresh token nuevo y devuelve el valor en texto (solo se entrega una vez)
    @Transactional
    public String create(User user) {
        Instant now = Instant.now();
        refreshTokenRepository.deleteExpired(now);

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = new RefreshToken();
        token.setTokenHash(hash(raw));
        token.setUser(user);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(validity));
        refreshTokenRepository.save(token);
        return raw;
    }

    // Valida el token y lo marca como usado (rotación: cada refresh token sirve una sola vez).
    // noRollbackFor: si se detecta un token reutilizado, el cierre de sesiones debe quedar
    // guardado aunque después se lance el error.
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public User consume(String raw) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(raw))
                .orElseThrow(() -> unauthorized("La sesión no es válida. Inicia sesión nuevamente."));
        User user = token.getUser();

        if (token.isRotated()) {
            // Un token que ya se cambió por otro vuelve a aparecer: puede que lo hayan robado.
            // Por seguridad se cierran todas las sesiones de ese usuario.
            refreshTokenRepository.revokeAllByUser(user, Instant.now());
            throw unauthorized("La sesión fue cerrada por seguridad. Inicia sesión nuevamente.");
        }
        if (token.getRevokedAt() != null) {
            throw unauthorized("La sesión fue cerrada. Inicia sesión nuevamente.");
        }
        if (!token.isActive()) {
            throw unauthorized("La sesión expiró. Inicia sesión nuevamente.");
        }

        token.setRevokedAt(Instant.now());
        token.setRotated(true);
        refreshTokenRepository.save(token);
        return user;
    }

    // Cierra la sesión de este dispositivo. No falla si el token no existe o ya estaba cerrado.
    @Transactional
    public void revoke(String raw) {
        refreshTokenRepository.findByTokenHash(hash(raw))
                .filter(t -> t.getRevokedAt() == null)
                .ifPresent(t -> {
                    t.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(t);
                });
    }

    public long validitySeconds() {
        return validity.toSeconds();
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }

    private static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
