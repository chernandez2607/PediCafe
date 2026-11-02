package cr.pedicafe.config;

import cr.pedicafe.domain.Rol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Públicas
                .requestMatchers("/", "/menu", "/menu/**",
                        "/carrito", "/carrito/**",
                        "/login", "/registro", "/acceso-denegado", "/error",
                        "/css/**", "/js/**", "/img/**", "/webjars/**",
                        "/favicon.ico").permitAll()
                // Pedidos: solo clientes
                .requestMatchers("/pedidos/**").hasRole(Rol.CLIENTE)
                // Solo administrador
                .requestMatchers("/admin/usuarios/**", "/admin/roles/**",
                        "/admin/productos/**", "/admin/categorias/**")
                    .hasRole(Rol.ADMINISTRADOR)
                // Inventario y recetas
                .requestMatchers("/admin/inventario/**", "/admin/recetas/**")
                    .hasAnyRole(Rol.ADMINISTRADOR, Rol.BARISTA)
                // Reportes
                .requestMatchers("/admin/reportes/**")
                    .hasAnyRole(Rol.ADMINISTRADOR, Rol.CAJERO)
                //  todo el personal
                .requestMatchers("/admin", "/admin/**")
                    .hasAnyRole(Rol.ADMINISTRADOR, Rol.BARISTA, Rol.CAJERO)
                // solo con sesión iniciada
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("correo")
                .passwordParameter("contrasena")
                .defaultSuccessUrl("/redirigir")
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?salir")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/acceso-denegado")
            );

        return http.build();
    }
}
