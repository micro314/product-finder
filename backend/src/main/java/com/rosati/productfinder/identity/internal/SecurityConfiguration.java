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
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(KeycloakProperties.class)
class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, KeycloakProperties keycloak) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/me", "/api/query-history").authenticated()
                        .anyRequest().permitAll());

        if (keycloak.hasIssuer()) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                    jwt.decoder(jwtDecoder(keycloak))));
        }

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
