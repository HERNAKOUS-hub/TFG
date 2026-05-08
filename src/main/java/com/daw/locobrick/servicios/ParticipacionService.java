package com.daw.locobrick.servicios;

import com.daw.locobrick.modelos.Participacion;
import com.daw.locobrick.repositorios.ParticipacionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ParticipacionService {

    private final ParticipacionRepository repositorio;

    public ParticipacionService(ParticipacionRepository repositorio) { this.repositorio = repositorio; }

    public Participacion crear(Participacion p) { return repositorio.save(p); }
    public List<Participacion> obtenerTodas() { return repositorio.findAll(); }
    public Optional<Participacion> obtenerPorId(Long id) { return repositorio.findById(id); }
    
    // Ver inversiones de UN usuario
    public List<Participacion> obtenerPorInversor(Long idInversor) {
        return repositorio.findByInversor_InvestorID(idInversor);
    }
}