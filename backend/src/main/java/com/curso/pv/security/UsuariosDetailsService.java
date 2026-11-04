package com.curso.pv.security;

import com.curso.pv.usuario.Usuario;
import com.curso.pv.usuario.UsuarioConsultasInseguras;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuariosDetailsService implements UserDetailsService {

    private final UsuarioConsultasInseguras consultas;

    public UsuariosDetailsService(UsuarioConsultasInseguras consultas) {
        this.consultas = consultas;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // VULNERABILIDAD (SQLi): la busqueda concatena el username en el SQL,
        // por lo que es posible iniciar sesion sin conocer credenciales validas
        Usuario usuario = consultas.porUsername(username);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }

        // VULNERABILIDAD: no se verifica que el usuario este activo
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(CodificadorEnClaro.autoridadesDe(usuario.getRol()))
                .build();
    }
}