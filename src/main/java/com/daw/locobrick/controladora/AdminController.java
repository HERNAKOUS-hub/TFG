package com.daw.locobrick.controladora;

import com.daw.locobrick.modelos.Propiedad;
import com.daw.locobrick.modelos.Zona;
import com.daw.locobrick.modelos.Participacion;
import com.daw.locobrick.modelos.Transaccion;
import com.daw.locobrick.modelos.Inversor;
import com.daw.locobrick.servicios.PropiedadService;
import com.daw.locobrick.servicios.ZonaService;
import com.daw.locobrick.servicios.InversorService;
import com.daw.locobrick.repositorios.ParticipacionRepository;
import com.daw.locobrick.repositorios.TransaccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private PropiedadService propiedadService;

    @Autowired
    private ZonaService zonaService;

    @Autowired
    private ParticipacionRepository participacionRepo;

    @Autowired
    private TransaccionRepository transaccionRepo;

    @Autowired
    private InversorService inversorService;

    @GetMapping("/propiedades")
    public String listarPropiedades(Model model) {
        List<Propiedad> propiedades = propiedadService.obtenerTodas();
        model.addAttribute("propiedades", propiedades);
        return "admin/propiedades-lista";
    }

    @GetMapping("/propiedades/nueva")
    public String formularioNuevaPropiedad(Model model) {
        model.addAttribute("propiedad", new Propiedad());
        model.addAttribute("zonas", zonaService.obtenerTodas());
        return "admin/propiedad-form";
    }

    @PostMapping("/propiedades/guardar")
    public String guardarPropiedad(
            @RequestParam String name,
            @RequestParam Long zoneID,
            @RequestParam Double value,
            @RequestParam(required = false) Double rentaMensual,
            @RequestParam String description,
            @RequestParam String status) {

        Zona zona = zonaService.obtenerPorId(zoneID)
                .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada"));

        Propiedad propiedad = new Propiedad();
        propiedad.setName(name);
        propiedad.setZona(zona);
        propiedad.setValue((long) (value * 100));
        propiedad.setValorRestante((long) (value * 100));

        if (rentaMensual != null && rentaMensual > 0) {
            propiedad.setRentaMensual((long) (rentaMensual * 100));
        } else {
            double rentaCalculadaEuros = (value * 0.065) / 12.0;
            propiedad.setRentaMensual(Math.round(rentaCalculadaEuros * 100));
        }

        propiedad.setDescription(description);
        propiedad.setStatus(status);

        propiedadService.crear(propiedad);
        return "redirect:/admin/propiedades?exito=creada";
    }

    @GetMapping("/propiedades/editar/{id}")
    public String formularioEditarPropiedad(@PathVariable Long id, Model model) {
        Propiedad propiedad = propiedadService.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada"));

        model.addAttribute("valorEuros", propiedad.getValue() / 100.0);

        if (propiedad.getRentaMensual() != null) {
            model.addAttribute("rentaEuros", propiedad.getRentaMensual() / 100.0);
        } else {
            model.addAttribute("rentaEuros", Math.round((propiedad.getValue() / 100.0) * 0.065 / 12.0 * 100.0) / 100.0);
        }

        model.addAttribute("propiedad", propiedad);
        model.addAttribute("zonas", zonaService.obtenerTodas());
        return "admin/propiedad-form";
    }

    @PostMapping("/propiedades/actualizar/{id}")
    public String actualizarPropiedad(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam Long zoneID,
            @RequestParam Double value,
            @RequestParam(required = false) Double rentaMensual,
            @RequestParam String description,
            @RequestParam String status) {

        Propiedad propiedad = propiedadService.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada"));

        Zona zona = zonaService.obtenerPorId(zoneID)
                .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada"));

        propiedad.setName(name);
        propiedad.setZona(zona);
        propiedad.setValue((long) (value * 100));

        if (rentaMensual != null && rentaMensual > 0) {
            propiedad.setRentaMensual((long) (rentaMensual * 100));
        } else {
            double rentaCalculadaEuros = (value * 0.065) / 12.0;
            propiedad.setRentaMensual(Math.round(rentaCalculadaEuros * 100));
        }

        propiedad.setDescription(description);
        propiedad.setStatus(status);

        propiedadService.crear(propiedad);

        return "redirect:/admin/propiedades?exito=actualizada";
    }

    @PostMapping("/propiedades/eliminar/{id}")
    public String eliminarPropiedad(@PathVariable Long id) {
        propiedadService.eliminar(id);
        return "redirect:/admin/propiedades?exito=eliminada";
    }

    @Transactional
    @PostMapping("/propiedades/repartir-rentas")
    public String repartirRentasMes() {
        List<Propiedad> todasLasPropiedades = propiedadService.obtenerTodas();

        // Creamos una "bolsa" para ir sumando lo que gana cada usuario
        Map<Inversor, Long> dividendosPorUsuario = new HashMap<>();

        // 1. Calculamos lo que corresponde a cada inversor por todas sus casas
        for (Propiedad p : todasLasPropiedades) {
            Long renta = p.getRentaMensual() != null ? p.getRentaMensual() : Math.round((p.getValue() * 0.065) / 12.0);
            if (renta <= 0)
                continue;

            List<Participacion> participaciones = participacionRepo.findByPropiedad_PropertyID(p.getPropertyID());

            for (Participacion part : participaciones) {
                if (part.getInvestmentAmount() == null || part.getInvestmentAmount() <= 0)
                    continue;

                double porcentajePropiedad = (double) part.getInvestmentAmount() / p.getValue();
                long dividendoCasa = Math.round(renta * porcentajePropiedad);

                if (dividendoCasa > 0) {
                    Inversor inv = part.getInversor();
                    // Sumamos a su bolsa personal
                    dividendosPorUsuario.put(inv, dividendosPorUsuario.getOrDefault(inv, 0L) + dividendoCasa);
                }
            }
        }

        if (dividendosPorUsuario.isEmpty()) {
            return "redirect:/admin/propiedades?error=no_hay_rentas";
        }

        // 2. Hacemos UN SOLO INGRESO y UN SOLO RECIBO por cada inversor
        for (Map.Entry<Inversor, Long> entry : dividendosPorUsuario.entrySet()) {
            Inversor inversor = entry.getKey();
            Long dividendoTotal = entry.getValue();

            inversorService.depositar(inversor.getInvestorID(), dividendoTotal);

            // Al ser un recibo de varias casas juntas, dejamos la 'participacion' en null
            Transaccion tx = new Transaccion(inversor, null, null, "RENTA", dividendoTotal, java.time.LocalDate.now());
            transaccionRepo.save(tx);
        }

        return "redirect:/admin/propiedades?exito=rentas_repartidas";
    }
}