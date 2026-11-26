package cl.duoc.miapi.repository;

import cl.duoc.miapi.model.RefreshToken;
import cl.duoc.miapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Cierra todas las sesiones de un usuario (ej. si se detecta un token robado)
    @Modifying
    @Query("UPDATE RefreshToken t SET t.revokedAt = :now WHERE t.user = :user AND t.revokedAt IS NULL")
    int revokeAllByUser(@Param("user") User user, @Param("now") Instant now);

    // Limpieza: borra los tokens que ya vencieron
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < :now")
    int deleteExpired(@Param("now") Instant now);
}
