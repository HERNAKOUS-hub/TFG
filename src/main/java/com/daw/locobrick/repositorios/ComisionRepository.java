package com.daw.locobrick.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daw.locobrick.modelos.Comision;

@Repository
public interface ComisionRepository extends JpaRepository<Comision, Long>{
    // Hace que herede las funciones CRUD
    // Crear => save()
    // Leer => findById() / findAll()
    // Actualizar => save()
    // Borrar => deleteById() / delete()
}