package com.curso.pv.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;

/**
 * VULNERABILIDAD: codificador de contrasenas que devuelve el valor tal cual.
 * Las contrasenas quedan almacenadas en texto plano, sin hash ni salt.
 */
public class CodificadorEnClaro implements PasswordEncoder {

    @Override
    public String encode(CharSequence passwordBruto) {
        return passwordBruto != null ? passwordBruto.toString() : null;
    }

    @Override
    public boolean matches(CharSequence passwordBruto, String passwordAlmacenado) {
        return passwordBruto != null && passwordBruto.toString().equals(passwordAlmacenado);
    }

    public static Collection<GrantedAuthority> autoridadesDe(String rol) {
        return List.of(new SimpleGrantedAuthority("ROL_" + rol));
    }
}