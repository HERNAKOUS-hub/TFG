package com.daw.locobrick.servicios;

import com.daw.locobrick.modelos.Propiedad;
import com.daw.locobrick.repositorios.PropiedadRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PropiedadService {

    private final PropiedadRepository repositorio;

    public PropiedadService(PropiedadRepository repositorio) { 
        this.repositorio = repositorio; 
    }

    // Guardar (Crear o Actualizar)
    public Propiedad crear(Propiedad p) { 
        return repositorio.save(p); 
    }
    
    public List<Propiedad> obtenerTodas() { 
        return repositorio.findAll(); 
    }
    
    public Optional<Propiedad> obtenerPorId(Long id) { 
        return repositorio.findById(id); 
    }
    
    public void eliminar(Long id) { 
        repositorio.deleteById(id); 
    }

    // Método de búsqueda avanzada para el catálogo público
    // Fíjate que aquí usamos Long, igual que en el Controlador y el Repositorio
    public List<Propiedad> buscar(String ciudad, Long precioMax) {
        if (ciudad != null && !ciudad.isEmpty() && precioMax != null) {
            return repositorio.findByZonaCityContainingIgnoreCaseAndValueLessThanEqual(ciudad, precioMax);
        } else if (ciudad != null && !ciudad.isEmpty()) {
            return repositorio.findByZonaCityContainingIgnoreCase(ciudad);
        } else if (precioMax != null) {
            return repositorio.findByValueLessThanEqual(precioMax);
        } else {
            return repositorio.findAll().stream().filter(p -> p.getZona() != null).collect(Collectors.toList());
        }
    }
}