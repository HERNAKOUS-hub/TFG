package com.daw.locobrick.servicios;

import com.daw.locobrick.modelos.Transaccion;
import com.daw.locobrick.repositorios.TransaccionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TransaccionService {
    private final TransaccionRepository repositorio;
    public TransaccionService(TransaccionRepository repositorio) { this.repositorio = repositorio; }

    public Transaccion crear(Transaccion t) { return repositorio.save(t); }
    public List<Transaccion> obtenerTodas() { return repositorio.findAll(); }
    public Optional<Transaccion> obtenerPorId(Long id) { return repositorio.findById(id); }
}