package com.daw.locobrick.servicios;

import com.daw.locobrick.modelos.Comision;
import com.daw.locobrick.repositorios.ComisionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ComisionService {
    private final ComisionRepository repositorio;
    public ComisionService(ComisionRepository repositorio) { this.repositorio = repositorio; }

    public Comision crear(Comision c) { return repositorio.save(c); }
    public List<Comision> obtenerTodas() { return repositorio.findAll(); }
    public Optional<Comision> obtenerPorId(Long id) { return repositorio.findById(id); }
}