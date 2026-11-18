package com.curso.pv.config;

import com.curso.pv.security.UsuariosDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http,
                                                 HttpSessionSecurityContextRepository contextoRepositorio) throws Exception {
        http
                // VULNERABILIDAD: sin proteccion CSRF
                .csrf(csrf -> csrf.disable())
                .cors(cors -> { })
                // VULNERABILIDAD: sin CSP ni demas cabeceras de seguridad
                .headers(headers -> { })

                // VULNERABILIDAD: la sesion no se renueva al iniciar sesion (session fixation)
                .sessionManagement(sesion -> sesion
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(fixacion -> { }))

                // VULNERABILIDAD: se reutiliza la sesion existente sin validar su origen
                .securityContext(contexto -> contexto.securityContextRepository(contextoRepositorio))

                // CORRECCION BAC-01 (ADR-0001): la API deja de ser publica.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/salud", "/actuator/health").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())

                // Sin sesion -> 401 (en vez del reto de HTTP Basic)
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                // VULNERABILIDAD: HTTP Basic habilitado, las credenciales viajan en cada peticion
                .httpBasic(basic -> { })
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public HttpSessionSecurityContextRepository contextoRepositorio() {
        // VULNERABILIDAD: la sesion se guarda tal cual en el contenedor, sin cifrar
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityContextRepository securityContextRepository(HttpSessionSecurityContextRepository repositorio) {
        return repositorio;
    }

    // CORRECCION CRY-03: BCrypt con factor de costo 10 (el valor por defecto de Spring).
    // encode() genera un salt distinto cada vez; matches() lo extrae del hash almacenado.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider proveedorDeAutenticacion(UsuariosDetailsService detailsService,
                                                              PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(detailsService);
        proveedor.setPasswordEncoder(passwordEncoder);
        return proveedor;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}