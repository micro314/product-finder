package com.rosati.productfinder.identity.internal;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(KeycloakProperties.class)
class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, KeycloakProperties keycloak) {
        if (!keycloak.hasIssuer()) {
            throw new IllegalStateException("KEYCLOAK_ISSUER_URI must be configured to secure the API.");
        }

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // These endpoints only begin the Keycloak flow; they expose no application data.
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/index/status").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.decoder(jwtDecoder(keycloak))));

        return http.build();
    }

    private JwtDecoder jwtDecoder(KeycloakProperties keycloak) {
        if (!keycloak.hasJwkSetUri()) {
            return JwtDecoders.fromIssuerLocation(keycloak.issuerUri().toString());
        }

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(keycloak.jwkSetUri().toString()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(keycloak.issuerUri().toString()));
        return decoder;
    }
}
