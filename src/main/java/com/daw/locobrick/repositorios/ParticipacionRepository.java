package com.daw.locobrick.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.daw.locobrick.modelos.Participacion;
import com.daw.locobrick.modelos.Propiedad;
import java.util.List;

@Repository
public interface ParticipacionRepository extends JpaRepository<Participacion, Long> {

    // Devuelve todas las inversiones de un usuario concreto
    List<Participacion> findByInversor_InvestorID(Long investorID);
    
    // Devuelve quiénes han invertido en una propiedad concreta buscando por la ID de la propiedad
    List<Participacion> findByPropiedad_PropertyID(Long propertyID);

    // NUEVO: Devuelve quiénes han invertido buscando por el objeto Propiedad entero (Para el reparto de rentas del Admin)
    List<Participacion> findByPropiedad(Propiedad propiedad);
}