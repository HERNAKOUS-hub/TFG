package com.daw.locobrick.servicios;

import com.daw.locobrick.modelos.Participacion;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public void generarCertificado(Participacion participacion, HttpServletResponse response) throws IOException, DocumentException {
        // Configuramos la respuesta HTTP para que el navegador sepa que es un PDF descargable
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Certificado_LocoBrick_" + participacion.getParticipationID() + ".pdf";
        response.setHeader(headerKey, headerValue);

        // Creamos el documento PDF
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // ==========================================
        // 1. AÑADIR LOGO CENTRADO
        // ==========================================
        try {
            Image logo = Image.getInstance(new ClassPathResource("static/images/LocoBrick.png").getURL());
            logo.setAlignment(Element.ALIGN_CENTER);
            logo.scaleToFit(200, 100); // Ajusta el tamaño según necesites
            document.add(logo);
            document.add(new Paragraph(" ")); // Espacio extra
        } catch (Exception e) {
            System.out.println("Aviso: No se pudo cargar el logo en el PDF (revisa la ruta static/images/LocoBrick.png).");
        }

        // --- ESTILOS DE FUENTE ---
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitulo.setSize(22);
        fontTitulo.setColor(new BaseColor(251, 98, 34)); // Naranja LocoBrick

        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontSubtitulo.setSize(14);
        fontSubtitulo.setColor(BaseColor.DARK_GRAY);

        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA);
        fontNormal.setSize(12);

        // --- CONTENIDO DEL PDF ---
        // 2. Título
        Paragraph titulo = new Paragraph("CERTIFICADO DE INVERSION", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(30);
        document.add(titulo);

        // 3. Texto introductorio
        Paragraph textoIntro = new Paragraph(
                "Por la presente se certifica que el usuario ha adquirido participaciones sobre los derechos economicos del siguiente activo inmobiliario a traves de la plataforma LocoBrick.",
                fontNormal);
        textoIntro.setSpacingAfter(20);
        document.add(textoIntro);

        // 4. Datos del Inversor
        document.add(new Paragraph("DATOS DEL TITULAR:", fontSubtitulo));
        document.add(new Paragraph("Nombre: " + participacion.getInversor().getFirstName() + " " + participacion.getInversor().getLastName(), fontNormal));
        document.add(new Paragraph("Email: " + participacion.getInversor().getEmail(), fontNormal));
        
        document.add(Chunk.NEWLINE);

        // 5. Datos de la Inversión
        document.add(new Paragraph("DETALLES DE LA PARTICIPACION:", fontSubtitulo));
        document.add(new Paragraph("ID de Registro: #" + participacion.getParticipationID(), fontNormal));
        document.add(new Paragraph("Propiedad: " + participacion.getPropiedad().getName() + " (" + participacion.getPropiedad().getZona().getCity() + ")", fontNormal));
        document.add(new Paragraph("Porcentaje Adquirido: " + participacion.getPercentage() + " %", fontNormal));
        document.add(new Paragraph("Capital Invertido: " + participacion.getInvestmentAmount() + " EUR", fontNormal));
        
        String fechaFormateada = participacion.getPurchaseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        document.add(new Paragraph("Fecha de Adquisicion: " + fechaFormateada, fontNormal));

        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        // 6. Pie de página legal
        Font fontPie = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE);
        fontPie.setSize(10);
        fontPie.setColor(BaseColor.GRAY);
        Paragraph pie = new Paragraph("Este documento tiene caracter informativo y certifica su participacion en la plataforma LocoBrick. Guarde este certificado para sus registros.", fontPie);
        pie.setAlignment(Element.ALIGN_CENTER);
        document.add(pie);

        document.close();
    }
}