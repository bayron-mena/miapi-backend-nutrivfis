package cl.duoc.miapi.security;


import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${app.jwt.secret}") String secret) {

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET debe tener al menos 32 bytes"
            );
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(
                new ImmutableSecret<>(key)
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey key) {
        return NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("role");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(roles);

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter converter
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                // Usa los @CrossOrigin de cada controlador (orígenes en app.cors.allowed-origins).
                // Así las respuestas 401/403 también llevan los headers CORS.
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                // Mismos permisos que usa el frontend (requireRole en AppProvider.tsx).
                // Se evalúan en orden: la primera regla que calza es la que se aplica.
                .authorizeHttpRequests(auth -> auth
                        // ---- Público ----
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                // Públicos porque se usan cuando el JWT ya venció;
                                // los protege el propio refresh token
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                // Sin esto, cualquier error (400, 409...) llega como 401
                                "/error"
                        ).permitAll()
                        // Catálogo: cualquiera puede verlo, sin iniciar sesión
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products", "/api/v1/products/**",
                                "/api/v1/categories", "/api/v1/categories/**"
                        ).permitAll()

                        // ---- Productos: Admin y Vendedor crean y editan (incluye stock) ----
                        .requestMatchers(HttpMethod.POST, "/api/v1/products")
                        .hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**")
                        .hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/products/*/stock")
                        .hasAnyRole("ADMIN", "SELLER")
                        // Solo Admin elimina
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**")
                        .hasRole("ADMIN")

                        // ---- Solo Admin ----
                        .requestMatchers("/api/v1/categories", "/api/v1/categories/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/patients", "/api/v1/patients/**")
                        .hasRole("ADMIN")
                        // Perfil propio: cualquier usuario con sesión (va antes de /users/**)
                        .requestMatchers("/api/v1/users/me").authenticated()
                        .requestMatchers("/api/v1/users", "/api/v1/users/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/reports", "/api/v1/reports/**")
                        .hasRole("ADMIN")

                        // ---- Boletas ----
                        // Listado de todas las ventas: Admin y Vendedor
                        .requestMatchers(HttpMethod.GET, "/api/v1/receipts")
                        .hasAnyRole("ADMIN", "SELLER")
                        // Comprar (POST), historial propio (/me) y ver una boleta (/{id}):
                        // cualquier usuario con sesión. ReceiptService revisa que cada uno
                        // vea solo sus boletas.

                        // Todo lo demás pide sesión iniciada
                        .anyRequest().authenticated()
                )
                // Respuestas 401 y 403 en JSON, con el mismo formato que GlobalExceptionHandler
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(JsonSecurityHandlers.unauthorized())
                        .accessDeniedHandler(JsonSecurityHandlers.forbidden())
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(converter)
                        )
                        // Token vencido, mal formado o con firma inválida
                        .authenticationEntryPoint(JsonSecurityHandlers.unauthorized())
                        .accessDeniedHandler(JsonSecurityHandlers.forbidden())
                );
        return http.build();
    }
}

