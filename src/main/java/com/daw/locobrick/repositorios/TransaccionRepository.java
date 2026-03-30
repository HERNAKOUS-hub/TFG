package com.daw.locobrick.repositorios;

import com.daw.locobrick.modelos.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    // Esta es la consulta que te falta para que el Controller funcione
    @Modifying
    @Query("DELETE FROM Transaccion t WHERE t.participacion.participationID = :id")
    void borrarTransaccionesPorParticipacion(@Param("id") Long id);
    
}