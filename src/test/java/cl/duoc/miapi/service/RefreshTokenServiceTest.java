package cl.duoc.miapi.service;

import cl.duoc.miapi.TestData;
import cl.duoc.miapi.model.RefreshToken;
import cl.duoc.miapi.model.Role;
import cl.duoc.miapi.model.User;
import cl.duoc.miapi.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    private RefreshTokenService service;
    private final User user = TestData.user(1L, "ana@gmail.com", Role.CLIENT);

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repository, 7);
    }

    private RefreshToken token(Instant expiresAt) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(expiresAt);
        return token;
    }

    @Test
    void createGuardaSoloElHashYDuraSieteDias() {
        String raw = service.create(user);

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(saved.capture());
        assertThat(raw).hasSizeGreaterThanOrEqualTo(40);
        // En la base nunca queda el token en texto, solo su SHA-256 (64 caracteres hex)
        assertThat(saved.getValue().getTokenHash()).hasSize(64).isNotEqualTo(raw);
        assertThat(saved.getValue().getExpiresAt())
                .isCloseTo(Instant.now().plus(Duration.ofDays(7)), within(Duration.ofSeconds(5)));
    }

    @Test
    void consumeDeUnTokenValidoLoRotaYDevuelveElUsuario() {
        RefreshToken token = token(Instant.now().plus(Duration.ofDays(1)));
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThat(service.consume("raw")).isSameAs(user);
        assertThat(token.isRotated()).isTrue();
        assertThat(token.getRevokedAt()).isNotNull();
    }

    @Test
    void reusarUnTokenRotadoCierraTodasLasSesiones() {
        RefreshToken token = token(Instant.now().plus(Duration.ofDays(1)));
        token.setRotated(true);
        token.setRevokedAt(Instant.now());
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.consume("raw"))
                .hasMessageContaining("cerrada por seguridad");
        verify(repository).revokeAllByUser(eq(user), any());
    }

    @Test
    void tokenCerradoConLogoutNoCierraLasOtrasSesiones() {
        RefreshToken token = token(Instant.now().plus(Duration.ofDays(1)));
        token.setRevokedAt(Instant.now());
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.consume("raw"))
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        verify(repository, never()).revokeAllByUser(any(), any());
    }

    @Test
    void tokenVencidoResponde401() {
        when(repository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(token(Instant.now().minusSeconds(1))));

        assertThatThrownBy(() -> service.consume("raw")).hasMessageContaining("expiró");
    }

    @Test
    void tokenInexistenteResponde401() {
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consume("raw"))
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void revokeNoFallaSiElTokenNoExiste() {
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        service.revoke("raw");

        verify(repository, never()).save(any());
    }
}
