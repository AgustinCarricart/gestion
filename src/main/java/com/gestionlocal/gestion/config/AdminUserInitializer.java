package com.gestionlocal.gestion.config;

import com.gestionlocal.gestion.usuario.RolEnum;
import com.gestionlocal.gestion.usuario.UsuarioEntity;
import com.gestionlocal.gestion.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Order(1)
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.initial-superadmin.username:superadmin}")
    private String defaultSuperAdminUser;

    @Value("${app.initial-superadmin.password:superadmin123}")
    private String defaultSuperAdminPassword;

    @Value("${app.initial-admin.username:admin}")
    private String defaultAdminUser;

    @Value("${app.initial-admin.password:admin123}")
    private String defaultAdminPassword;

    @Value("${app.initial-cocina.username:cocina}")
    private String defaultCocinaUser;

    @Value("${app.initial-cocina.password:cocina123}")
    private String defaultCocinaPassword;

    public AdminUserInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. SuperAdmin (Dueño de la plataforma / Distribuidor)
        if (usuarioRepository.findByUsernameIgnoreCase(defaultSuperAdminUser).isEmpty()) {
            UsuarioEntity superAdmin = new UsuarioEntity();
            superAdmin.setPublicId(UUID.randomUUID());
            superAdmin.setUsername(defaultSuperAdminUser);
            superAdmin.setPassword(passwordEncoder.encode(defaultSuperAdminPassword));
            superAdmin.setRol(RolEnum.SUPERADMIN);
            superAdmin.setActivo(true);
            usuarioRepository.save(superAdmin);
            log.info("Usuario SuperAdmin sembrado: '{}'", defaultSuperAdminUser);
        }

        // 2. Admin (Dueño de la pastelería / negocio cliente)
        if (usuarioRepository.findByUsernameIgnoreCase(defaultAdminUser).isEmpty()) {
            UsuarioEntity admin = new UsuarioEntity();
            admin.setPublicId(UUID.randomUUID());
            admin.setUsername(defaultAdminUser);
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setRol(RolEnum.ADMIN);
            admin.setActivo(true);
            usuarioRepository.save(admin);
            log.info("Usuario Admin cliente sembrado: '{}'", defaultAdminUser);
        }

        // 3. Cocina (Personal operativo KDS)
        if (usuarioRepository.findByUsernameIgnoreCase(defaultCocinaUser).isEmpty()) {
            UsuarioEntity cocina = new UsuarioEntity();
            cocina.setPublicId(UUID.randomUUID());
            cocina.setUsername(defaultCocinaUser);
            cocina.setPassword(passwordEncoder.encode(defaultCocinaPassword));
            cocina.setRol(RolEnum.COCINA);
            cocina.setActivo(true);
            usuarioRepository.save(cocina);
            log.info("Usuario Cocina sembrado: '{}'", defaultCocinaUser);
        }
    }
}
