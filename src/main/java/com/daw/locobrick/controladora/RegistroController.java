package com.daw.locobrick.controladora;

import com.daw.locobrick.modelos.Inversor;
import com.daw.locobrick.servicios.InversorService;
import com.daw.locobrick.repositorios.TokenSeguridadRepository;
import com.daw.locobrick.servicios.EmailService;
import com.daw.locobrick.modelos.TokenSeguridad;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistroController {

    @Autowired
    private InversorService inversorService;

    @Autowired
    private TokenSeguridadRepository tokenRepo;

    @Autowired
    private EmailService emailService;

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Inversor()); 
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarInversor(
            @ModelAttribute("usuario") Inversor usuarioRecibido, 
            @RequestParam String via,
            @RequestParam String numero,
            @RequestParam(required = false) String piso,
            @RequestParam String cp,
            @RequestParam String ciudad,
            @RequestParam String provincia,
            Model model) {
        try {
            // Juntamos la dirección de forma profesional: "Calle Mayor, 5, 2B, 28001, Madrid, Madrid"
            String direccionCompleta = String.format("%s, %s, %s, %s, %s, %s", 
                via, numero, (piso == null || piso.isEmpty() ? "N/A" : piso), cp, ciudad, provincia);
            
            usuarioRecibido.setDireccion(direccionCompleta);

            // Guardamos el usuario
            Inversor nuevoUsuario = inversorService.registrar(usuarioRecibido);

            // Proceso de token y email...
            String tokenRandom = UUID.randomUUID().toString();
            TokenSeguridad token = new TokenSeguridad(tokenRandom, "REGISTRO", nuevoUsuario, 1440);
            tokenRepo.save(token);

            emailService.enviarEmailVerificacion(nuevoUsuario.getEmail(), tokenRandom);

            return "redirect:/login?aviso=revisar_correo";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", new Inversor());
            return "registro";
        } catch (Exception e) {
            model.addAttribute("error", "Error inesperado al procesar el registro.");
            model.addAttribute("usuario", new Inversor());
            return "registro";
        }
    }
}