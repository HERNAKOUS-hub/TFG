package com.daw.locobrick.seguridad;

import com.daw.locobrick.modelos.Inversor;
import com.daw.locobrick.repositorios.InversorRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioDetallesUsuario implements UserDetailsService {

    private final InversorRepository repositorio;

    public ServicioDetallesUsuario(InversorRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Inversor inversor = repositorio.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Convertimos nuestro 'Inversor' en un 'User' de Spring Security
        // FÍJATE EN EL TERCER PARÁMETRO: Bloquea a los que no tienen la cuenta verificada
        return new org.springframework.security.core.userdetails.User(
                inversor.getEmail(),
                inversor.getPassword(),
                inversor.isCuentaVerificada(), // <-- AQUÍ SE DECIDE SI ENTRA O NO
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + inversor.getRol()))
        );
    }
}