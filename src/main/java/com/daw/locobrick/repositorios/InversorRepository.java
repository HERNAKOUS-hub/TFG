package com.daw.locobrick.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.daw.locobrick.modelos.Inversor;
import java.util.Optional;

@Repository
public interface InversorRepository extends JpaRepository<Inversor, Long> {
    // Busca un usuario por su email (Vital para el Login)
    Optional<Inversor> findByEmail(String email);

    // Verifica si ya existe un email antes de registrar a alguien nuevo
    boolean existsByEmail(String email);
    
    // Verifica si ya existe un DNI antes de registrar a alguien nuevo
    boolean existsByDni(String dni);
}