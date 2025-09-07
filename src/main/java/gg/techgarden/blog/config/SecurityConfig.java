package gg.techgarden.blog.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;

@Configuration
@Slf4j
public class SecurityConfig {

    private static final String REQUIRED_AUD = "api.blog";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(System.getenv()
                .getOrDefault("KEYCLOAK_ISSUER_URI", "https://sso.dev.techgarden.gg/realms/techgarden"));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(System.getenv()
                        .getOrDefault("KEYCLOAK_ISSUER_URI", "https://sso.dev.techgarden.gg/realms/techgarden")),
                audienceValidator()
        ));
        return decoder;
    }

    @Bean
    OAuth2TokenValidator<Jwt> audienceValidator() {
        return token -> {
            List<String> aud = token.getAudience();
            if (aud != null && aud.contains(REQUIRED_AUD)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Required audience 'api.blog' not present", null));
        };
    }

    /** Merge Keycloak roles into SCOPE_* authorities for uniform @PreAuthorize checks. */
    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // 1) OAuth scopes -> SCOPE_*
            var scopes = Optional.ofNullable(jwt.getClaimAsString("scope"))
                    .map(s -> Arrays.asList(s.split(" "))).orElse(List.of());
            scopes.forEach(s -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + s)));

            // 2) Client roles under resource_access[client].roles -> SCOPE_*
            var resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                resourceAccess.forEach((client, val) -> {
                    if (val instanceof Map<?,?> rolesMap) {
                        Object rolesObj = rolesMap.get("roles");
                        if (rolesObj instanceof Collection<?> rolesCol) {
                            rolesCol.forEach(r -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + r.toString())));
                        }
                    }
                });
            }

            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }
}
