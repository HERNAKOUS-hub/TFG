package com.daw.locobrick.repositorios;

import com.daw.locobrick.modelos.Inversor;
import com.daw.locobrick.modelos.TokenSeguridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface TokenSeguridadRepository extends JpaRepository<TokenSeguridad, Long> {
    
    // Encuentra un token específico cuando el usuario pincha el enlace
    Optional<TokenSeguridad> findByToken(String token);

    @Transactional
    void deleteByInversor(Inversor inversor);
}