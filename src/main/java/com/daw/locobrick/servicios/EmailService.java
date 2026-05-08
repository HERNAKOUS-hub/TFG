package com.daw.locobrick.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Esto lee automáticamente la IP o el localhost del archivo application.properties
    @Value("${app.base-url}")
    private String baseUrl;

    public void enviarEmailRecuperacion(String emailDestino, String token) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(emailDestino);
        mensaje.setSubject("Recupera tu contraseña - LocoBrick");
        
        String enlace = baseUrl + "/reset-password?token=" + token;
        
        mensaje.setText("Hola,\n\nHas solicitado restablecer tu contraseña en LocoBrick.\n"
                + "Haz clic en el siguiente enlace para crear una nueva (este enlace caducará en 15 minutos):\n\n"
                + enlace + "\n\n"
                + "Si no has sido tú, puedes ignorar este correo.");

        mailSender.send(mensaje);
    }

    public void enviarEmailBorrado(String emailDestino, String token) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(emailDestino);
        mensaje.setSubject("⚠️ Confirmación para ELIMINAR tu cuenta - LocoBrick");
        
        String enlace = baseUrl + "/confirmar-borrado?token=" + token;
        
        mensaje.setText("Hola,\n\n"
                + "Hemos recibido una solicitud para ELIMINAR de forma permanente tu cuenta en LocoBrick.\n"
                + "Si has sido tú y estás seguro, haz clic en el siguiente enlace para borrar todos tus datos:\n\n"
                + enlace + "\n\n"
                + "Si NO has sido tú, ignora este correo. Tu cuenta seguirá intacta y segura.\n\n"
                + "El equipo de LocoBrick.");

        mailSender.send(mensaje);
    }

    // --- NUEVO: CORREO DE VERIFICACIÓN DE REGISTRO ---
    public void enviarEmailVerificacion(String emailDestino, String token) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(emailDestino);
        mensaje.setSubject("👋 ¡Bienvenido a LocoBrick! Verifica tu correo");
        
        String enlace = baseUrl + "/verificar-cuenta?token=" + token;
        
        mensaje.setText("Hola,\n\n"
                + "¡Gracias por registrarte en LocoBrick!\n"
                + "Para proteger tu cuenta y confirmar que este correo es tuyo, haz clic en el siguiente enlace:\n\n"
                + enlace + "\n\n"
                + "Este enlace expirará en 24 horas.\n"
                + "Si no te has registrado en LocoBrick, ignora este correo.\n\n"
                + "El equipo de LocoBrick.");

        mailSender.send(mensaje);
    }
}