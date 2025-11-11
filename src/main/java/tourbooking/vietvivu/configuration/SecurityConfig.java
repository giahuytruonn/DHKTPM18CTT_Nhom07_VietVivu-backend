package tourbooking.vietvivu.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINTS = {
            "/users",
            "/auth/token",
            "/auth/introspect",
            "/auth/logout",
            "/auth/refresh",
            "/auth/outbound/authentication"
    };

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()

                        // Tour endpoints - PUBLIC (User & Guest)
                        .requestMatchers(HttpMethod.GET, "/tours", "/tours/**", "/tours/search").permitAll()

                        // Tour endpoints - ADMIN only
                        .requestMatchers(HttpMethod.POST, "/tours").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/tours/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tours/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tours/admin/**").hasRole("ADMIN")

                        // ===== FIX: Favorite tours - Cho phép cả USER và ADMIN =====
                        .requestMatchers(HttpMethod.GET, "/users/favorite-tours").authenticated()
                        .requestMatchers(HttpMethod.POST, "/users/favorite-tours/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/users/favorite-tours/**").authenticated()

                        // User endpoints
                        .requestMatchers(HttpMethod.GET, "/users/my-info").authenticated()
                        .requestMatchers(HttpMethod.POST, "/users/create-password").authenticated()

                        // Booking & Review endpoints - ADMIN only
                        .requestMatchers("/bookings/**").hasRole("ADMIN")
                        .requestMatchers("/reviews/**").hasRole("ADMIN")

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )

                // OAuth2 Resource Server configuration
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                )

                // Disable CSRF
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");
        authoritiesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}