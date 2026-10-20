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
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

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

    public AdminUserInitializer(UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder,
                                org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // En PostgreSQL con Hibernate 6, se genera un check constraint 'usuario_rol_check'.
        // Al agregar nuevos valores al enum (SUPERADMIN, COCINA), debemos eliminar ese constraint viejo si existe.
        try {
            jdbcTemplate.execute("ALTER TABLE usuario DROP CONSTRAINT IF EXISTS usuario_rol_check");
            jdbcTemplate.execute("ALTER TABLE usuario DROP CONSTRAINT IF EXISTS usuario_rol_check1");
            log.info("Constraint usuario_rol_check verificado / eliminado exitosamente.");
        } catch (Exception e) {
            log.warn("Aviso al verificar constraints de tabla usuario: {}", e.getMessage());
        }

        // 1. SuperAdmin (Dueño de la plataforma / Distribuidor)
        try {
            if (usuarioRepository.findByUsernameIgnoreCase(defaultSuperAdminUser).isEmpty()) {
                UsuarioEntity superAdmin = new UsuarioEntity();
                superAdmin.setPublicId(UUID.randomUUID());
                superAdmin.setUsername(defaultSuperAdminUser);
                superAdmin.setPassword(passwordEncoder.encode(defaultSuperAdminPassword));
                superAdmin.setRol(RolEnum.SUPERADMIN);
                superAdmin.setActivo(true);
                usuarioRepository.save(superAdmin);
                log.info("Usuario SuperAdmin sembrado exitosamente: '{}'", defaultSuperAdminUser);
            }
        } catch (Exception e) {
            log.error("Error al sembrar usuario SuperAdmin '{}': {}", defaultSuperAdminUser, e.getMessage());
        }

        // 2. Admin (Dueño de la pastelería / negocio cliente)
        try {
            if (usuarioRepository.findByUsernameIgnoreCase(defaultAdminUser).isEmpty()) {
                UsuarioEntity admin = new UsuarioEntity();
                admin.setPublicId(UUID.randomUUID());
                admin.setUsername(defaultAdminUser);
                admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
                admin.setRol(RolEnum.ADMIN);
                admin.setActivo(true);
                usuarioRepository.save(admin);
                log.info("Usuario Admin cliente sembrado exitosamente: '{}'", defaultAdminUser);
            }
        } catch (Exception e) {
            log.error("Error al sembrar usuario Admin '{}': {}", defaultAdminUser, e.getMessage());
        }

        // 3. Cocina (Personal operativo KDS)
        try {
            if (usuarioRepository.findByUsernameIgnoreCase(defaultCocinaUser).isEmpty()) {
                UsuarioEntity cocina = new UsuarioEntity();
                cocina.setPublicId(UUID.randomUUID());
                cocina.setUsername(defaultCocinaUser);
                cocina.setPassword(passwordEncoder.encode(defaultCocinaPassword));
                cocina.setRol(RolEnum.COCINA);
                cocina.setActivo(true);
                usuarioRepository.save(cocina);
                log.info("Usuario Cocina sembrado exitosamente: '{}'", defaultCocinaUser);
            }
        } catch (Exception e) {
            log.error("Error al sembrar usuario Cocina '{}': {}", defaultCocinaUser, e.getMessage());
        }
    }
}
