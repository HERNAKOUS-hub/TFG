package com.daw.locobrick.controladora;

import com.daw.locobrick.modelos.Inversor;
import com.daw.locobrick.modelos.Participacion;
import com.daw.locobrick.modelos.Transaccion;
import com.daw.locobrick.modelos.Comision;
import com.daw.locobrick.modelos.Propiedad;
import com.daw.locobrick.repositorios.ParticipacionRepository;
import com.daw.locobrick.repositorios.TransaccionRepository;
import com.daw.locobrick.repositorios.ComisionRepository;
import com.daw.locobrick.servicios.InversorService;
import com.daw.locobrick.servicios.PropiedadService;
import com.daw.locobrick.servicios.PdfService;

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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private InversorService inversorService;

    @Autowired
    private PropiedadService propiedadService;

    @Autowired
    private ParticipacionRepository participacionRepo;

    @Autowired
    private TransaccionRepository transaccionRepo;

    @Autowired
    private ComisionRepository comisionRepo;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/mis-inversiones")
    public String verDashboard(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            Inversor usuario = inversorService.buscarPorEmail(auth.getName()).orElseThrow();

            List<Participacion> misInversiones = participacionRepo.findByInversor_InvestorID(usuario.getInvestorID())
                    .stream()
                    .filter(p -> p.getInvestmentAmount() > 0L)
                    .collect(Collectors.toList());

            Double totalInvertido = misInversiones.stream()
                    .mapToDouble(p -> p.getInvestmentAmount() / 100.0)
                    .sum();

            java.util.Map<String, Double> datosGrafico = misInversiones.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getPropiedad().getZona().getCity(),
                            Collectors.summingDouble(p -> p.getInvestmentAmount() / 100.0)));

            Double gananciaEstimada = misInversiones.stream()
                    .mapToDouble(part -> {
                        Propiedad p = part.getPropiedad();
                        Long renta = p.getRentaMensual() != null ? p.getRentaMensual()
                                : Math.round((p.getValue() * 0.065) / 12.0);
                        double porcentajeDuenio = (double) part.getInvestmentAmount() / p.getValue();
                        return (renta * 12 * porcentajeDuenio) / 100.0;
                    }).sum();

            List<Transaccion> transacciones = transaccionRepo.findAll().stream()
                    .filter(tx -> tx.getInversor().getInvestorID().equals(usuario.getInvestorID()))
                    .collect(Collectors.toList());

            Transaccion ultimaRenta = transacciones.stream()
                    .filter(t -> "RENTA".equals(t.getType()))
                    .filter(t -> t.getDate().isEqual(LocalDate.now())
                            || t.getDate().isEqual(LocalDate.now().minusDays(1)))
                    .max(java.util.Comparator.comparing(Transaccion::getTransactionID))
                    .orElse(null);

            model.addAttribute("ultimaRenta", ultimaRenta);
            model.addAttribute("usuario", usuario);
            model.addAttribute("saldoEuros", usuario.getSaldo() / 100.0);
            model.addAttribute("inversiones", misInversiones);
            model.addAttribute("totalInvertido", totalInvertido);
            model.addAttribute("gananciaEstimada", gananciaEstimada);
            model.addAttribute("datosGrafico", datosGrafico);

            return "mis-inversiones";
        } catch (Exception e) {
            return "redirect:/login?error";
        }
    }

    @GetMapping("/banco/depositar")
    public String mostrarFormularioBanco(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return "redirect:/login";
        Inversor usuario = inversorService.buscarPorEmail(auth.getName()).orElseThrow();
        model.addAttribute("usuario", usuario);
        return "form-deposito";
    }

    @GetMapping("/banco/retirar")
    public String mostrarFormularioRetiro(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return "redirect:/login";
        Inversor usuario = inversorService.buscarPorEmail(auth.getName()).orElseThrow();
        model.addAttribute("usuario", usuario);
        model.addAttribute("saldoEuros", usuario.getSaldo() / 100.0);
        return "form-retiro";
    }

    @PostMapping("/wallet/operar")
    public String operarWallet(@RequestParam String tipo,
            @RequestParam Long cantidadCentimos,
            @RequestParam(required = false) String numeroTarjeta,
            Authentication auth) {

        Inversor usuario = inversorService.buscarPorEmail(auth.getName()).orElseThrow();

        try {
            if ("depositar".equals(tipo)) {
                if (numeroTarjeta != null && !numeroTarjeta.isEmpty()) {
                    String numerosLimpios = numeroTarjeta.replaceAll("\\s+", "");
                    if (numerosLimpios.length() >= 4) {
                        String ultimos4 = numerosLimpios.substring(numerosLimpios.length() - 4);
                        inversorService.guardarUltimosDigitos(usuario.getInvestorID(), ultimos4);
                    }
                }
                inversorService.depositar(usuario.getInvestorID(), cantidadCentimos);
                transaccionRepo
                        .save(new Transaccion(usuario, null, null, "Ingreso", cantidadCentimos, LocalDate.now()));

            } else if ("retirar".equals(tipo)) {
                if (cantidadCentimos > usuario.getSaldo()) {
                    return "redirect:/mis-inversiones?error=saldo";
                }
                inversorService.retirar(usuario.getInvestorID(), cantidadCentimos);
                transaccionRepo
                        .save(new Transaccion(usuario, null, null, "Retirada", cantidadCentimos, LocalDate.now()));
            }
        } catch (Exception e) {
            return "redirect:/mis-inversiones?error=saldo";
        }
        return "redirect:/mis-inversiones";
    }

    @Transactional
    @PostMapping("/inversiones/vender")
    public String venderActivo(
            @RequestParam Long idParticipacion,
            @RequestParam Long cantidadVenderCentimos,
            Authentication auth) {

        try {
            Inversor usuario = inversorService.buscarPorEmail(auth.getName()).orElseThrow();
            Participacion participacion = participacionRepo.findById(idParticipacion).orElseThrow();
            Propiedad propiedad = participacion.getPropiedad();

            Long amountAVender = cantidadVenderCentimos;
            Long investmentActual = participacion.getInvestmentAmount();

            if (amountAVender > investmentActual) {
                return "redirect:/mis-inversiones?error=venta";
            }

            if (amountAVender.equals(investmentActual)) {
                participacion.setInvestmentAmount(0L);
                participacion.setPercentage(0L);
            } else {
                Long porcentajeVendido = (amountAVender * 10000L) / propiedad.getValue();
                participacion.setInvestmentAmount(investmentActual - amountAVender);
                participacion.setPercentage(participacion.getPercentage() - porcentajeVendido);
            }

            participacionRepo.save(participacion);

            usuario.setSaldo(usuario.getSaldo() + amountAVender);
            inversorService.crear(usuario);

            propiedad.setValorRestante(propiedad.getValorRestante() + amountAVender);
            if (propiedad.getValorRestante() > 0L) {
                propiedad.setStatus("Disponible");
            }
            propiedadService.crear(propiedad);

            Comision comisionSalida = comisionRepo.findById(3L).orElse(null);
            Transaccion tx = new Transaccion(usuario, participacion, comisionSalida, "Venta", amountAVender,
                    LocalDate.now());
            transaccionRepo.save(tx);

            return "redirect:/venta-exitosa/" + tx.getTransactionID();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/mis-inversiones?error=venta";
        }
    }

    @GetMapping("/venta-exitosa/{txId}")
    public String ventaExitosa(@PathVariable Long txId, Model model) {
        Transaccion tx = transaccionRepo.findById(txId).orElseThrow();
        model.addAttribute("transaccion", tx);
        model.addAttribute("importeEuros", tx.getAmount() / 100.0);
        return "venta-exitosa";
    }

    // --- PDF ACTUALIZADO PARA RECIBOS MÚLTIPLES ---
    @GetMapping("/factura/renta/pdf/{txId}")
    public ResponseEntity<byte[]> descargarReciboRenta(@PathVariable Long txId) {
        try {
            Transaccion tx = transaccionRepo.findById(txId).orElseThrow();
            Inversor inversor = tx.getInversor();
            Locale localeES = new Locale("es", "ES");

            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            try {
                Image logo = Image.getInstance(
                        new org.springframework.core.io.ClassPathResource("static/images/LocoBrick.png").getURL());
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.scaleToFit(200, 100);
                document.add(logo);
                document.add(new Paragraph(" "));
            } catch (Exception e) {
            }

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.GREEN.darker());
            Paragraph title = new Paragraph("LIQUIDACION DE RENTAS MENSUALES", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("Inversor: " + inversor.getFirstName() + " " + inversor.getLastName()));
            document.add(new Paragraph("Fecha de abono: " + tx.getDate().toString()));
            document.add(new Paragraph("Operacion ID: #" + tx.getTransactionID()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            table.addCell(new PdfPCell(new Phrase("Concepto", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));
            table.addCell(new PdfPCell(new Phrase("Importe (EUR)", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));

            // Si tiene varias participaciones, las desglosamos en la tabla
            List<Participacion> misInversiones = participacionRepo.findByInversor_InvestorID(inversor.getInvestorID())
                    .stream().filter(p -> p.getInvestmentAmount() > 0L).collect(Collectors.toList());

            for (Participacion part : misInversiones) {
                Propiedad p = part.getPropiedad();
                Long rentaMensual = p.getRentaMensual() != null ? p.getRentaMensual()
                        : Math.round((p.getValue() * 0.065) / 12.0);
                double porcentaje = (double) part.getInvestmentAmount() / p.getValue();
                long dividendoCasa = Math.round(rentaMensual * porcentaje);

                if (dividendoCasa > 0) {
                    table.addCell("Beneficios de explotación: " + p.getName());
                    table.addCell(String.format(localeES, "+%.2f EUR", dividendoCasa / 100.0));
                }
            }

            PdfPCell totalCellDesc = new PdfPCell(
                    new Phrase("TOTAL INGRESADO EN SALDO", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            PdfPCell totalCellValue = new PdfPCell(
                    new Phrase(String.format(localeES, "+%.2f EUR", tx.getAmount() / 100.0),
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.GREEN.darker())));
            totalCellDesc.setBackgroundColor(BaseColor.LIGHT_GRAY);
            totalCellValue.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(totalCellDesc);
            table.addCell(totalCellValue);

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("El dinero se ha sumado de forma agrupada a tu saldo disponible.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));

            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "Recibo_Global_Rentas_" + tx.getTransactionID() + ".pdf");
            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/factura/venta/pdf/{txId}")
    public ResponseEntity<byte[]> descargarReciboVenta(@PathVariable Long txId) {
        try {
            Transaccion tx = transaccionRepo.findById(txId).orElseThrow();
            Participacion p = tx.getParticipacion();
            Locale localeES = new Locale("es", "ES");

            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            try {
                Image logo = Image.getInstance(
                        new org.springframework.core.io.ClassPathResource("static/images/LocoBrick.png").getURL());
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.scaleToFit(200, 100);
                document.add(logo);
                document.add(new Paragraph(" "));
            } catch (Exception e) {
            }

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.RED);
            Paragraph title = new Paragraph("RECIBO DE VENTA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(
                    new Paragraph("Inversor: " + p.getInversor().getFirstName() + " " + p.getInversor().getLastName()));
            document.add(new Paragraph("Fecha de la operacion: " + tx.getDate().toString()));
            document.add(new Paragraph("ID de Transaccion: #" + tx.getTransactionID()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("Concepto", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));
            table.addCell(new PdfPCell(new Phrase("Importe (EUR)", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));

            double importeEuros = tx.getAmount() / 100.0;
            table.addCell("Desinversion en: " + p.getPropiedad().getName());
            table.addCell(String.format(localeES, "%.2f EUR", importeEuros));

            PdfPCell totalCellDesc = new PdfPCell(
                    new Phrase("SALDO INGRESADO EN CARTERA", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            PdfPCell totalCellValue = new PdfPCell(new Phrase(String.format(localeES, "%.2f EUR", importeEuros),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            totalCellDesc.setBackgroundColor(BaseColor.LIGHT_GRAY);
            totalCellValue.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(totalCellDesc);
            table.addCell(totalCellValue);

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("El dinero ya esta disponible en tu saldo para nuevas inversiones.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));

            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "Recibo_Venta_" + tx.getTransactionID() + ".pdf");
            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cartera/informe")
    public ResponseEntity<byte[]> descargarInformeGlobal(Authentication auth) {
        try {
            Inversor usuario = inversorService.buscarPorEmail(auth.getName()).orElseThrow();
            List<Transaccion> todasTx = transaccionRepo.findAll();

            List<Transaccion> misTx = todasTx.stream()
                    .filter(tx -> tx.getInversor().getInvestorID().equals(usuario.getInvestorID()))
                    .toList();

            Locale localeES = new Locale("es", "ES");

            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            try {
                Image logo = Image.getInstance(
                        new org.springframework.core.io.ClassPathResource("static/images/LocoBrick.png").getURL());
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.scaleToFit(200, 100);
                document.add(logo);
                document.add(new Paragraph(" "));
            } catch (Exception e) {
            }

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, BaseColor.ORANGE);
            Paragraph title = new Paragraph("INFORME GLOBAL DE CARTERA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("Titular: " + usuario.getFirstName() + " " + usuario.getLastName()));
            document.add(new Paragraph("Fecha de emision: " + LocalDate.now().toString()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("RESUMEN DE CUENTA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(
                    "Saldo Liquido Disponible: " + String.format(localeES, "%.2f EUR", usuario.getSaldo() / 100.0)));
            document.add(new Paragraph(" "));

            document.add(
                    new Paragraph("HISTORIAL DE MOVIMIENTOS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));

            PdfPTable tableHistorial = new PdfPTable(4);
            tableHistorial.setWidthPercentage(100);
            tableHistorial.addCell("Fecha");
            tableHistorial.addCell("Tipo");
            tableHistorial.addCell("Propiedad/Concepto");
            tableHistorial.addCell("Importe");

            for (Transaccion tx : misTx) {
                tableHistorial.addCell(tx.getDate().toString());
                tableHistorial.addCell(tx.getType());

                // Si la participación es nula (como en nuestro nuevo recibo global), ponemos
                // que es de toda la cartera
                String nombrePropiedad = tx.getParticipacion() != null ? tx.getParticipacion().getPropiedad().getName()
                        : (tx.getType().equals("RENTA") ? "Múltiples Activos" : "-");
                tableHistorial.addCell(nombrePropiedad);

                boolean esSuma = tx.getType().equalsIgnoreCase("Venta") || tx.getType().equalsIgnoreCase("Ingreso")
                        || tx.getType().equalsIgnoreCase("RENTA");
                Font fontImporte = esSuma ? FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GREEN.darker())
                        : FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.RED);
                String signo = esSuma ? "+" : "-";

                tableHistorial.addCell(
                        new Phrase(signo + String.format(localeES, "%.2f EUR", tx.getAmount() / 100.0), fontImporte));
            }
            document.add(tableHistorial);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "Informe_Global_LocoBrick.pdf");
            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/inversiones/certificado")
    public void descargarCertificado(@RequestParam Long id, Authentication auth,
            jakarta.servlet.http.HttpServletResponse response) {
        try {
            Participacion p = participacionRepo.findById(id).orElseThrow();
            if (!p.getInversor().getEmail().equals(auth.getName())) {
                throw new RuntimeException("No tienes permiso");
            }
            pdfService.generarCertificado(p, response);
        } catch (Exception e) {
            try {
                response.sendRedirect("/mis-inversiones?error=pdf");
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}