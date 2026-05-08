package com.daw.locobrick.controladora;

import com.daw.locobrick.modelos.Inversor;
import com.daw.locobrick.modelos.Participacion;
import com.daw.locobrick.modelos.Propiedad;
import com.daw.locobrick.modelos.Transaccion;
import com.daw.locobrick.modelos.Zona;
import com.daw.locobrick.modelos.Comision;
import com.daw.locobrick.repositorios.ParticipacionRepository;
import com.daw.locobrick.repositorios.PropiedadRepository;
import com.daw.locobrick.repositorios.TransaccionRepository;
import com.daw.locobrick.repositorios.ZonaRepository;
import com.daw.locobrick.repositorios.ComisionRepository;
import com.daw.locobrick.servicios.InversorService;
import com.daw.locobrick.servicios.PropiedadService;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ViewController {

    @Autowired
    private PropiedadService propiedadService;

    @Autowired
    private InversorService inversorService;

    @Autowired
    private ParticipacionRepository participacionRepo; 

    @Autowired
    private TransaccionRepository transaccionRepo; 

    @Autowired
    private ComisionRepository comisionRepo;

    @Autowired
    private PropiedadRepository propiedadRepo;

    @Autowired
    private ZonaRepository zonaRepo;

    @GetMapping("/")
    public String inicio() { return "index"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Long precio,
            Model model) {

        List<Propiedad> propiedades;

        // AQUÍ ESTÁ LA MAGIA PARA QUE FUNCIONE LA BÚSQUEDA
        if (ciudad != null && !ciudad.isEmpty() && precio != null) {
            Long precioEnCentimos = precio * 100L;
            propiedades = propiedadRepo.findByZonaCityContainingIgnoreCaseAndValueLessThanEqual(ciudad, precioEnCentimos);
        } else if (ciudad != null && !ciudad.isEmpty()) {
            propiedades = propiedadRepo.findByZonaCityContainingIgnoreCase(ciudad);
        } else if (precio != null) {
            Long precioEnCentimos = precio * 100L;
            propiedades = propiedadRepo.findByValueLessThanEqual(precioEnCentimos);
        } else {
            propiedades = propiedadRepo.findAll();
        }

        // Por seguridad, si alguna propiedad recién creada tiene el valorRestante nulo, lo igualamos a su valor total
        for (Propiedad p : propiedades) {
            if (p.getValorRestante() == null) {
                p.setValorRestante(p.getValue());
            }
        }

        // Listamos las zonas para el desplegable del buscador
        List<Zona> zonasDisponibles = zonaRepo.findAll();
        List<String> ciudadesUnicas = zonasDisponibles.stream()
                .map(Zona::getCity)
                .distinct()
                .collect(Collectors.toList());

        // PASAMOS LOS DATOS A LA VISTA (Usando addAttribute, sin errores)
        model.addAttribute("ciudadesUnicas", ciudadesUnicas);
        model.addAttribute("propiedades", propiedades); 
        model.addAttribute("ciudadSeleccionada", ciudad);
        model.addAttribute("precioSeleccionado", precio); 

        return "catalogo";
    }

    @GetMapping("/propiedad/{id}")
    public String verPropiedad(@PathVariable Long id, Model model, Authentication auth) {
        Propiedad p = propiedadService.obtenerPorId(id).orElseThrow();

        if (p.getValorRestante() == null) {
            p.setValorRestante(p.getValue());
            propiedadService.crear(p); 
        }

        Long valorCentimos = p.getValue();
        Double valorEuros = valorCentimos / 100.0;
        
        // LEEMOS LA RENTA REAL DE LA BASE DE DATOS PARA ESTA CASA
        Double rendimientoAnualEuros;
        if (p.getRentaMensual() != null) {
            rendimientoAnualEuros = (p.getRentaMensual() * 12) / 100.0;
        } else {
            rendimientoAnualEuros = valorEuros * 0.065; // Fallback al 6.5% si es antigua
        }
        
        Double valorRestanteEuros = p.getValorRestante() / 100.0;

        Long costeTotalComprarTodoCentimos = (long) Math.ceil(p.getValorRestante() / 0.98);
        Double costeTotalComprarTodoEuros = costeTotalComprarTodoCentimos / 100.0;

        if (auth != null && auth.isAuthenticated()) {
            inversorService.buscarPorEmail(auth.getName()).ifPresent(usuario -> {
                model.addAttribute("usuario", usuario);
                model.addAttribute("saldoEuros", usuario.getSaldo() / 100.0);
            });
        }

        model.addAttribute("propiedad", p);
        model.addAttribute("valorEuros", valorEuros);
        model.addAttribute("valorRestanteEuros", valorRestanteEuros);
        model.addAttribute("rendimientoAnual", rendimientoAnualEuros);
        model.addAttribute("costeTotalComprarTodoEuros", costeTotalComprarTodoEuros); 

        return "propiedad";
    }

    @Transactional
    @PostMapping("/propiedad/{id}/comprar")
    public String comprarPropiedad(@PathVariable Long id,
            @RequestParam Double cantidad, 
            Authentication auth) {
        
        if (auth == null || !auth.isAuthenticated()) return "redirect:/login";

        try {
            Inversor usuario = inversorService.buscarPorEmail(auth.getName()).orElseThrow();
            Propiedad propiedad = propiedadService.obtenerPorId(id).orElseThrow();

            Long totalPagadoCentimos = Math.round(cantidad * 100.0); 
            Long comisionApertura = (totalPagadoCentimos * 2L) / 100L; 
            Long inversionRealCentimos = totalPagadoCentimos - comisionApertura; 

            Long saldoActual = usuario.getSaldo() != null ? usuario.getSaldo() : 0L;
            
            if (saldoActual < totalPagadoCentimos) {
                BigDecimal maxInvEuros = new BigDecimal(saldoActual).divide(new BigDecimal("100"), 2, RoundingMode.DOWN); 
                String maximoString = maxInvEuros.toString().replace('.', ',');
                return "redirect:/propiedad/" + id + "?error=saldo&max=" + maximoString;
            }

            if (inversionRealCentimos > propiedad.getValorRestante()) {
                if (inversionRealCentimos - propiedad.getValorRestante() <= 5) { 
                    inversionRealCentimos = propiedad.getValorRestante();
                    comisionApertura = totalPagadoCentimos - inversionRealCentimos;
                } else {
                    return "redirect:/propiedad/" + id + "?error=exceso";
                }
            }

            Long porcentaje = (inversionRealCentimos * 10000L) / propiedad.getValue();

            List<Participacion> misInversiones = participacionRepo.findByInversor_InvestorID(usuario.getInvestorID());
            Participacion participacionExistente = misInversiones.stream()
                .filter(p -> p.getPropiedad().getPropertyID().equals(propiedad.getPropertyID()))
                .findFirst()
                .orElse(null);

            Participacion participacionFinal;
            if (participacionExistente != null) {
                participacionExistente.setInvestmentAmount(participacionExistente.getInvestmentAmount() + inversionRealCentimos);
                participacionExistente.setPercentage(participacionExistente.getPercentage() + porcentaje);
                participacionExistente.setPurchaseDate(LocalDate.now()); 
                participacionFinal = participacionRepo.save(participacionExistente);
            } else {
                participacionFinal = participacionRepo.save(new Participacion(usuario, propiedad, porcentaje, inversionRealCentimos, LocalDate.now()));
            }
            participacionRepo.flush(); 

            inversorService.retirar(usuario.getInvestorID(), totalPagadoCentimos);

            Long valorRestanteActual = propiedad.getValorRestante() != null ? propiedad.getValorRestante() : propiedad.getValue();
            propiedad.setValorRestante(valorRestanteActual - inversionRealCentimos);
            
            if (propiedad.getValorRestante() <= 0L) {
                propiedad.setStatus("Financiado");
            }
            propiedadService.crear(propiedad);

            Comision comision = comisionRepo.findById(1L).orElse(null);
            
            Transaccion tx = new Transaccion(usuario, participacionFinal, comision, "Compra", inversionRealCentimos, LocalDate.now());
            tx = transaccionRepo.save(tx);

            return "redirect:/compra-exitosa/" + tx.getTransactionID();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/catalogo?error";
        }
    }

    @GetMapping("/compra-exitosa/{txId}")
    public String compraExitosa(@PathVariable Long txId, Model model) {
        Transaccion tx = transaccionRepo.findById(txId).orElseThrow();
        
        Double inversionEuros = tx.getAmount() / 100.0;
        Double totalEuros = Math.round((inversionEuros / 0.98) * 100.0) / 100.0; 
        Double comisionEuros = Math.round((totalEuros - inversionEuros) * 100.0) / 100.0; 
        
        model.addAttribute("transaccion", tx);
        model.addAttribute("inversion", inversionEuros);
        model.addAttribute("comision", comisionEuros);
        model.addAttribute("total", totalEuros);
        
        return "compra-exitosa";
    }

    @GetMapping("/factura/pdf/{txId}")
    public ResponseEntity<byte[]> descargarFactura(@PathVariable Long txId) {
        try {
            Transaccion tx = transaccionRepo.findById(txId).orElseThrow();
            Participacion p = tx.getParticipacion();
            
            Double inversionEuros = tx.getAmount() / 100.0;
            Double totalEuros = Math.round((inversionEuros / 0.98) * 100.0) / 100.0;
            Double comisionEuros = Math.round((totalEuros - inversionEuros) * 100.0) / 100.0;

            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();
            
            try {
                Image logo = Image.getInstance(new org.springframework.core.io.ClassPathResource("static/images/LocoBrick.png").getURL());
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.scaleToFit(200, 100);
                document.add(logo);
                document.add(new Paragraph(" "));
            } catch (Exception e) { }
            
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.ORANGE);
            Paragraph title = new Paragraph("FACTURA DE INVERSION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            document.add(new Paragraph("Inversor: " + p.getInversor().getFirstName() + " " + p.getInversor().getLastName()));
            document.add(new Paragraph("Fecha: " + tx.getDate().toString()));
            document.add(new Paragraph("Operacion ID: #" + tx.getTransactionID()));
            document.add(new Paragraph(" "));
            
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            
            table.addCell(new PdfPCell(new Phrase("Concepto", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));
            table.addCell(new PdfPCell(new Phrase("Importe (EUR)", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));
            
            table.addCell("Inversion en Propiedad: " + p.getPropiedad().getName() + " (98%)");
            table.addCell(String.format("%.2f EUR", inversionEuros));
            
            table.addCell("Comision de Apertura y Gestion (2%)");
            table.addCell(String.format("%.2f EUR", comisionEuros));
            
            PdfPCell totalCellDesc = new PdfPCell(new Phrase("TOTAL PAGADO", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            PdfPCell totalCellValue = new PdfPCell(new Phrase(String.format("%.2f EUR", totalEuros), FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            totalCellDesc.setBackgroundColor(BaseColor.LIGHT_GRAY);
            totalCellValue.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(totalCellDesc);
            table.addCell(totalCellValue);
            
            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Gracias por confiar en LocoBrick.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
            
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "Factura_LocoBrick_" + tx.getTransactionID() + ".pdf");
            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/terminos")
    public String terminos() { return "terminos"; }

    @GetMapping("/privacidad")
    public String privacidad() { return "privacidad"; }

    @GetMapping("/riesgos")
    public String riesgos() { return "riesgos"; }
}