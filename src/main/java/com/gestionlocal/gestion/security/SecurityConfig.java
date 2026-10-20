package com.gestionlocal.gestion.security;

import com.gestionlocal.gestion.usuario.UsuarioDetailsServiceImpl;
import com.gestionlocal.gestion.usuario.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService)
    {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Público: login, health check, seed de catálogo y configuración pública de licencia
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/seed-catalogo").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/licencia/public").permitAll()
                        .requestMatchers("/api/health").permitAll()

                        // Licencia y Planes: SOLO SUPERADMIN
                        .requestMatchers("/api/licencia/**").hasAuthority("SUPERADMIN")

                        // Gestión de usuarios: SUPERADMIN y ADMIN
                        .requestMatchers("/api/auth/**").hasAnyAuthority("SUPERADMIN", "ADMIN")

                        // Productos: lectura pública (catálogo visible), escritura SUPERADMIN y ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                        .requestMatchers("/api/productos/**").hasAnyAuthority("SUPERADMIN", "ADMIN")

                        // Reportes de ingresos y ventas: SUPERADMIN y ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/reportes/**").hasAnyAuthority("SUPERADMIN", "ADMIN")

                        // Notificaciones y stream en tiempo real: SUPERADMIN, ADMIN, COCINA y EMPLEADO
                        .requestMatchers(HttpMethod.GET, "/api/notificaciones/stream").hasAnyAuthority("SUPERADMIN", "ADMIN", "COCINA", "EMPLEADO")
                        .requestMatchers("/api/notificaciones/**").hasAnyAuthority("SUPERADMIN", "ADMIN")

                        // Pedidos: consultar todos los roles autenticados
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasAnyAuthority("SUPERADMIN", "ADMIN", "COCINA", "EMPLEADO")

                        // Cargar o cancelar pedidos: SUPERADMIN y ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/**").hasAnyAuthority("SUPERADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**").hasAnyAuthority("SUPERADMIN", "ADMIN")

                        // Modificar seña de pedido: SUPERADMIN y ADMIN
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/sena").hasAnyAuthority("SUPERADMIN", "ADMIN")

                        // Avanzar estados en Cocina: SUPERADMIN, ADMIN, COCINA y EMPLEADO
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/**").hasAnyAuthority("SUPERADMIN", "ADMIN", "COCINA", "EMPLEADO")
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/**").hasAnyAuthority("SUPERADMIN", "ADMIN", "COCINA", "EMPLEADO")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(jwtAuthFilter);
        registration.setEnabled(false);
        return registration;
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "https://*.vercel.app",
                "https://frontend-gestion.vercel.app",
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider()
    {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception
    {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
