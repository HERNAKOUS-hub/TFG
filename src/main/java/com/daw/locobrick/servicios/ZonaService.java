package com.daw.locobrick.servicios;

import com.daw.locobrick.modelos.Zona;
import com.daw.locobrick.repositorios.ZonaRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ZonaService {
    private final ZonaRepository repositorio;
    public ZonaService(ZonaRepository repositorio) { this.repositorio = repositorio; }

    public Zona crear(Zona z) { return repositorio.save(z); }
    public List<Zona> obtenerTodas() { return repositorio.findAll(); }
    public Optional<Zona> obtenerPorId(Long id) { return repositorio.findById(id); }
    public void eliminar(Long id) { repositorio.deleteById(id); }
}