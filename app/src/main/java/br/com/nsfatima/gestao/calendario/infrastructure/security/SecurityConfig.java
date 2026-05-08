package br.com.nsfatima.gestao.calendario.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider(
            UsuarioDetailsService usuarioDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            DaoAuthenticationProvider daoAuthenticationProvider,
            JsonAuthenticationOutcomeProcessor authenticationOutcomeProcessor,
            ObjectMapper objectMapper) throws Exception {
        JsonUsernamePasswordAuthenticationFilter loginFilter =
                new JsonUsernamePasswordAuthenticationFilter(authenticationManager, objectMapper);
        loginFilter.setAuthenticationSuccessHandler(authenticationOutcomeProcessor);
        loginFilter.setAuthenticationFailureHandler(authenticationOutcomeProcessor);

        http.authenticationProvider(daoAuthenticationProvider)
                // Sessao stateful mantida via JSESSIONID; CSRF desabilitado explicitamente para a API JSON.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationOutcomeProcessor)
                        .accessDeniedHandler(authenticationOutcomeProcessor))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/usuarios/**").hasAnyRole("CLERO_PAROCO", "CONSELHO_COORDENADOR")
                        .anyRequest().authenticated())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(authenticationOutcomeProcessor))
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
