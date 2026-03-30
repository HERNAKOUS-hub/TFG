package com.daw.locobrick.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desactivamos CSRF para simplificar las pruebas
            .csrf(csrf -> csrf.disable())
            
            .authorizeHttpRequests(auth -> auth
                // 1. RECURSOS ESTÁTICOS (CSS, JS, Imágenes)
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                
                // 2. PÁGINAS PÚBLICAS 
                .requestMatchers("/", "/index", "/catalogo", "/propiedad/**", "/registro", "/login", "/recuperar-password", "/reset-password", "/confirmar-borrado", "/verificar-cuenta", "/terminos", "/privacidad", "/riesgos", "/error").permitAll()
                
                // 3. API PÚBLICA (si la usas)
                .requestMatchers("/api/propiedades/**").permitAll()
                .requestMatchers("/api/inversores/registro").permitAll()

                // 4. RUTAS DE ADMIN (¡LA CLAVE!)
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 5. RESTO DE RUTAS (Privadas)
                .anyRequest().authenticated()
            )
            
            .formLogin(login -> login
                .loginPage("/login")           // Tu página login.html
                .loginProcessingUrl("/login")  // Donde se envían los datos
                .defaultSuccessUrl("/", true)  // A dónde ir tras loguearse
                .permitAll()
            )
            
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")         // Volver al inicio tras salir
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}