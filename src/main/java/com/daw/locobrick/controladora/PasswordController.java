package com.daw.locobrick.controladora;

import com.daw.locobrick.modelos.Inversor;
import com.daw.locobrick.modelos.TokenSeguridad;
import com.daw.locobrick.repositorios.InversorRepository;
import com.daw.locobrick.repositorios.TokenSeguridadRepository;
import com.daw.locobrick.servicios.EmailService;
import com.daw.locobrick.servicios.InversorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Controller
public class PasswordController {

    @Autowired
    private InversorService inversorService;
    
    @Autowired
    private InversorRepository inversorRepo;

    @Autowired
    private TokenSeguridadRepository tokenRepo;

    @Autowired
    private EmailService emailService;


    // ==========================================
    // VERIFICACIÓN DE REGISTRO (DOUBLE OPT-IN)
    // ==========================================

    @GetMapping("/verificar-cuenta")
    public String verificarCuentaRegistro(@RequestParam String token) {
        Optional<TokenSeguridad> tokenDb = tokenRepo.findByToken(token);

        // Comprobamos que el token existe, no está caducado y es de tipo REGISTRO
        if (tokenDb.isPresent() && !tokenDb.get().isExpirado() && "REGISTRO".equals(tokenDb.get().getTipo())) {
            
            Inversor usuario = tokenDb.get().getInversor();
            
            // ¡MAGIA! Activamos la cuenta
            usuario.setCuentaVerificada(true);
            inversorRepo.save(usuario); // Guardamos el cambio en BD
            
            // Borramos el token porque ya se ha usado
            tokenRepo.delete(tokenDb.get());
            
            return "redirect:/login?aviso=cuenta_verificada";
        }
        
        return "redirect:/login?error=token_invalido";
    }


    // ==========================================
    // RECUPERACIÓN DE CONTRASEÑA
    // ==========================================

    @GetMapping("/recuperar-password")
    public String mostrarFormularioRecuperacion() {
        return "recuperar-password"; 
    }

    @PostMapping("/recuperar-password")
    public String procesarPeticionRecuperacion(@RequestParam String email) {
        System.out.println("🔎 Buscando en BD el correo: '" + email + "'");
        
        Optional<Inversor> usuario = inversorService.buscarPorEmail(email);
        
        if (usuario.isPresent()) {
            System.out.println("✅ ¡Usuario encontrado! Generando token...");
            String tokenRandom = UUID.randomUUID().toString();
            
            try {
                TokenSeguridad token = new TokenSeguridad(tokenRandom, "PASSWORD", usuario.get(), 15);
                tokenRepo.save(token);
                System.out.println("💾 Token guardado en BD. Intentando enviar email...");
                
                emailService.enviarEmailRecuperacion(usuario.get().getEmail(), tokenRandom);
                System.out.println("🚀 ¡EMAIL ENVIADO CON ÉXITO A GOOGLE!");
                
            } catch (Exception e) {
                System.out.println("❌ ERROR FATAL AL ENVIAR EL CORREO: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ ATENCIÓN: El usuario NO existe en la Base de Datos. El email no se enviará.");
        }
        
        return "redirect:/login?recuperacion=enviada";
    }

    @GetMapping("/reset-password")
    public String mostrarFormularioNuevaClave(@RequestParam String token, Model model) {
        Optional<TokenSeguridad> tokenDb = tokenRepo.findByToken(token);

        if (tokenDb.isEmpty() || tokenDb.get().isExpirado() || !"PASSWORD".equals(tokenDb.get().getTipo())) {
            return "redirect:/login?error=token_invalido";
        }

        model.addAttribute("token", token);
        return "reset-password"; 
    }

    @PostMapping("/reset-password")
    public String guardarNuevaContrasena(@RequestParam String token, @RequestParam String nuevaPassword) {
        Optional<TokenSeguridad> tokenDb = tokenRepo.findByToken(token);

        if (tokenDb.isPresent() && !tokenDb.get().isExpirado()) {
            Inversor usuario = tokenDb.get().getInversor();
            
            inversorService.actualizarPassword(usuario.getInvestorID(), nuevaPassword);
            tokenRepo.delete(tokenDb.get());
            
            System.out.println("✅ Contraseña actualizada correctamente para: " + usuario.getEmail());
            return "redirect:/login?exito=password_cambiada";
        }

        System.out.println("❌ Error: Token inválido o expirado al intentar cambiar la clave.");
        return "redirect:/login?error=token_invalido";
    }


    // ==========================================
    // BORRADO DE CUENTA
    // ==========================================

    @PostMapping("/solicitar-borrado")
    public String solicitarBorradoCuenta(Principal principal) {
        String emailUsuario = principal.getName();
        Optional<Inversor> usuario = inversorService.buscarPorEmail(emailUsuario);
        
        if (usuario.isPresent()) {
            String tokenRandom = UUID.randomUUID().toString();
            TokenSeguridad token = new TokenSeguridad(tokenRandom, "BORRADO", usuario.get(), 60); 
            tokenRepo.save(token);
            
            emailService.enviarEmailBorrado(emailUsuario, tokenRandom);
        }
        
        return "redirect:/?aviso=borrado_solicitado";
    }

    @GetMapping("/confirmar-borrado")
    public String ejecutarBorradoDefinitivo(@RequestParam String token, HttpServletRequest request) {
        Optional<TokenSeguridad> tokenDb = tokenRepo.findByToken(token);

        if (tokenDb.isPresent() && !tokenDb.get().isExpirado() && "BORRADO".equals(tokenDb.get().getTipo())) {
            Inversor usuario = tokenDb.get().getInversor();
            
            tokenRepo.delete(tokenDb.get());
            inversorService.borrarDeVerdad(usuario.getInvestorID()); 
            
            try {
                request.logout();
            } catch (jakarta.servlet.ServletException e) {
                e.printStackTrace();
            }
            
            return "redirect:/login?cuenta_eliminada";
        }
        
        return "redirect:/login?error=token_invalido";
    }
}