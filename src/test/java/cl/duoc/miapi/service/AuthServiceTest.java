package cl.duoc.miapi.service;

import cl.duoc.miapi.TestData;
import cl.duoc.miapi.config.FieldValidationException;
import cl.duoc.miapi.dto.AuthResponse;
import cl.duoc.miapi.dto.LoginRequest;
import cl.duoc.miapi.dto.RegisterRequest;
import cl.duoc.miapi.model.Role;
import cl.duoc.miapi.model.User;
import cl.duoc.miapi.repository.UserRepository;
import cl.duoc.miapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // BCrypt real (con costo bajo para que el test sea rápido)
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, encoder, jwtService, refreshTokenService);
        lenient().when(jwtService.generarToken(any())).thenReturn("jwt");
        lenient().when(refreshTokenService.create(any())).thenReturn("refresh");
    }

    @Nested
    class Register {

        @Test
        void creaClienteConClaveEncriptadaYCorreoNormalizado() {
            when(userRepository.existsByEmail("ana123@gmail.com")).thenReturn(false);

            AuthResponse response = authService.register(TestData.validRegister());

            ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(saved.capture());
            User user = saved.getValue();
            assertThat(user.getEmail()).isEqualTo("ana123@gmail.com");
            assertThat(user.getRole()).isEqualTo(Role.CLIENT);
            assertThat(user.getPassword()).isNotEqualTo("Clave1").startsWith("$2a$");
            assertThat(encoder.matches("Clave1", user.getPassword())).isTrue();
            // "+" es el teléfono vacío del formulario
            assertThat(user.getPhone()).isNull();
            assertThat(user.getTermsAcceptedAt()).isNotNull();

            assertThat(response.getToken()).isEqualTo("jwt");
            assertThat(response.getRefreshToken()).isEqualTo("refresh");
            assertThat(response.getRole()).isEqualTo("CLIENT");
        }

        @Test
        void correoRepetidoResponde409() {
            when(userRepository.existsByEmail("ana123@gmail.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(TestData.validRegister()))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                            .isEqualTo(HttpStatus.CONFLICT));
            verify(userRepository, never()).save(any());
        }

        @Test
        void rechazaNacidosDespuesDe2012YComunaDeOtraRegion() {
            RegisterRequest request = TestData.validRegister();
            request.setBirthDate(LocalDate.of(2013, 1, 1));
            request.setComuna("Talca"); // Talca es de Maule, no de la Metropolitana

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(FieldValidationException.class)
                    .satisfies(e -> assertThat(((FieldValidationException) e).getErrors())
                            .containsKeys("birthDate", "comuna"));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class Login {

        private User user;

        @BeforeEach
        void setUp() {
            user = TestData.user(1L, "ana@gmail.com", Role.CLIENT);
            user.setPassword(encoder.encode("Clave1"));
        }

        @Test
        void claveCorrectaDevuelveTokensYReiniciaIntentos() {
            user.setFailedAttempts(2);
            when(userRepository.findByEmail("ana@gmail.com")).thenReturn(Optional.of(user));

            AuthResponse response = authService.login(new LoginRequest(" ANA@gmail.com ", "Clave1"));

            assertThat(response.getToken()).isEqualTo("jwt");
            assertThat(user.getFailedAttempts()).isZero();
        }

        @Test
        void correoInexistenteResponde401() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(new LoginRequest("x@gmail.com", "Clave1")))
                    .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                            .isEqualTo(HttpStatus.UNAUTHORIZED));
        }

        @Test
        void tresClavesIncorrectasBloqueanLaCuenta() {
            when(userRepository.findByEmail("ana@gmail.com")).thenReturn(Optional.of(user));
            LoginRequest wrong = new LoginRequest("ana@gmail.com", "Mala1");

            for (int i = 1; i <= 2; i++) {
                assertThatThrownBy(() -> authService.login(wrong))
                        .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                                .isEqualTo(HttpStatus.UNAUTHORIZED));
            }
            assertThat(user.isLocked()).isFalse();

            assertThatThrownBy(() -> authService.login(wrong))
                    .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                            .isEqualTo(HttpStatus.FORBIDDEN));
            assertThat(user.getFailedAttempts()).isEqualTo(3);
            assertThat(user.isLocked()).isTrue();
        }

        @Test
        void cuentaBloqueadaNoEntraNiConLaClaveCorrecta() {
            user.setLocked(true);
            when(userRepository.findByEmail("ana@gmail.com")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> authService.login(new LoginRequest("ana@gmail.com", "Clave1")))
                    .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                            .isEqualTo(HttpStatus.FORBIDDEN));
            verify(jwtService, never()).generarToken(any());
        }
    }

    @Test
    void refreshDeCuentaBloqueadaResponde403() {
        User user = TestData.user(1L, "ana@gmail.com", Role.CLIENT);
        user.setLocked(true);
        when(refreshTokenService.consume("rt")).thenReturn(user);

        assertThatThrownBy(() -> authService.refresh("rt"))
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
        verify(jwtService, never()).generarToken(any());
    }
}
