package com.daw.locobrick.repositorios;

import com.daw.locobrick.modelos.Propiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {

    // 1. Busca por ciudad (ignorando mayúsculas/minúsculas) Y por debajo de un precio
    List<Propiedad> findByZonaCityContainingIgnoreCaseAndValueLessThanEqual(String city, Long value);

    // 2. Busca solo por ciudad
    List<Propiedad> findByZonaCityContainingIgnoreCase(String city);

    // 3. Busca solo por debajo de un precio
    List<Propiedad> findByValueLessThanEqual(Long value);

}