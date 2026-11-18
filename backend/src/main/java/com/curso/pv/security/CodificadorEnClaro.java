package com.curso.pv.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;

/**
 * CORRECCION CRY-03: ya no implementa PasswordEncoder ni "cifra" devolviendo
 * el valor tal cual. Las contrasenas se hashean con BCryptPasswordEncoder
 * (bean definido en SecurityConfig). Esta clase solo conserva el helper que
 * traduce el rol guardado en la base a una autoridad de Spring Security.
 */
public final class CodificadorEnClaro {

    private CodificadorEnClaro() {
    }

    public static Collection<GrantedAuthority> autoridadesDe(String rol) {
        return List.of(new SimpleGrantedAuthority("ROL_" + rol));
    }
}