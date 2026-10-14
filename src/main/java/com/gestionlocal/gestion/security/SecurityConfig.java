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

                        // Público: login, health check y seed de catálogo
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/seed-catalogo").permitAll()
                        .requestMatchers("/api/health").permitAll()

                        // Gestión de usuarios: solo ADMIN
                        .requestMatchers("/api/auth/**").hasAuthority("ADMIN")

                        // Productos: lectura pública (catálogo visible), escritura solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                        .requestMatchers("/api/productos/**").hasAuthority("ADMIN")

                        // Reportes de ingresos y ventas: SOLO ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/reportes/**").hasAuthority("ADMIN")

                        // Notificaciones y alertas en tiempo real: SOLO ADMIN
                        .requestMatchers("/api/notificaciones/**").hasAuthority("ADMIN")

                        // Pedidos: consultar ADMIN y EMPLEADO
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasAnyAuthority("ADMIN", "EMPLEADO")

                        // Cargar o cancelar pedidos: solo ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**").hasAuthority("ADMIN")

                        // Modificar seña de pedido: solo ADMIN
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/sena").hasAuthority("ADMIN")

                        // Avanzar estados: ADMIN y EMPLEADO
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/**").hasAnyAuthority("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/**").hasAnyAuthority("ADMIN", "EMPLEADO")

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
