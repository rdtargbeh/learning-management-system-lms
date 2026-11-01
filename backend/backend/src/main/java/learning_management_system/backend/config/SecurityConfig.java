package learning_management_system.backend.config;

import learning_management_system.backend.security.RequestContextFilter;
import learning_management_system.backend.security.SecurityProperties;
import learning_management_system.backend.security.TokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                       // stateless bearer-token API
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .addFilterAfter(new RequestContextFilter(), BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(TokenService tokens) {
        return tokens.decoder();
    }

    /** Authorities: TYPE_<user_type>, PLATFORM_<platform_role>, PERM_<permission code>. */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> out = new ArrayList<>();
            String type = jwt.getClaimAsString(TokenService.CLAIM_USER_TYPE);
            if (type != null) out.add(new SimpleGrantedAuthority("TYPE_" + type));
            String prole = jwt.getClaimAsString(TokenService.CLAIM_PLATFORM_ROLE);
            if (prole != null) out.add(new SimpleGrantedAuthority("PLATFORM_" + prole));
            if (jwt.getClaimAsString(TokenService.CLAIM_SUPPORT_ACCESS) != null) out.add(new SimpleGrantedAuthority("SUPPORT_SESSION"));
            List<String> perms = jwt.getClaimAsStringList(TokenService.CLAIM_PERMISSIONS);
            if (perms != null) perms.forEach(p -> out.add(new SimpleGrantedAuthority("PERM_" + p)));
            return out;
        });
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
