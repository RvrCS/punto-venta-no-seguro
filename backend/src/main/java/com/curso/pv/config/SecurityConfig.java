package com.curso.pv.config;

import com.curso.pv.security.CodificadorEnClaro;
import com.curso.pv.security.UsuariosDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
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

                // VULNERABILIDAD: toda la API es publica, no hay control de acceso por rol
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/**").permitAll()
                        .anyRequest().permitAll())

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

    @Bean
    public DaoAuthenticationProvider proveedorDeAutenticacion(UsuariosDetailsService detailsService) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(detailsService);
        // VULNERABILIDAD: comparacion de contrasenas en texto plano
        proveedor.setPasswordEncoder(new CodificadorEnClaro());
        return proveedor;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}