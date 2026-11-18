package com.curso.pv.config;

import com.curso.pv.usuario.Usuario;
import com.curso.pv.usuario.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CORRECCION CRY-03: migra en caliente las contrasenas sembradas en texto
 * plano (admin/admin, vendedor/vendedor, etc.) a BCrypt. seed.sql solo corre
 * una vez al crear el volumen de MySQL, asi que sin esto los usuarios ya
 * existentes no podrian volver a iniciar sesion tras el fix.
 *
 * Es idempotente: una vez que una contrasena ya esta hasheada (prefijo
 * $2a$/$2b$/$2y$), se deja intacta. Pueden dejarlo permanentemente o
 * borrarlo despues de confirmar que todos los usuarios migraron.
 */
@Component
public class MigracionPasswordsRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MigracionPasswordsRunner.class);

    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder passwordEncoder;

    public MigracionPasswordsRunner(UsuarioRepositorio usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Usuario> todos = usuarios.findAll();
        int migrados = 0;
        for (Usuario usuario : todos) {
            String actual = usuario.getPassword();
            if (actual != null && !pareceBcrypt(actual)) {
                usuario.setPassword(passwordEncoder.encode(actual));
                usuarios.save(usuario);
                migrados++;
            }
        }
        if (migrados > 0) {
            log.info("Migracion CRY-03: {} contrasena(s) re-hasheadas con BCrypt", migrados);
        }
    }

    private boolean pareceBcrypt(String valor) {
        return valor.startsWith("$2a$") || valor.startsWith("$2b$") || valor.startsWith("$2y$");
    }
}