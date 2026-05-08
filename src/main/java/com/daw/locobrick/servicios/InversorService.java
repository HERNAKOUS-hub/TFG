package com.daw.locobrick.servicios;

import com.daw.locobrick.modelos.Inversor;
import com.daw.locobrick.repositorios.InversorRepository;
import com.daw.locobrick.repositorios.TokenSeguridadRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InversorService {

    private final InversorRepository repositorio;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private TokenSeguridadRepository tokenRepo;

    public InversorService(InversorRepository repositorio, PasswordEncoder passwordEncoder) {
        this.repositorio = repositorio;
        this.passwordEncoder = passwordEncoder;
    }

    public Inversor crear(Inversor inversor) {
        return repositorio.save(inversor);
    }

    public void guardarUltimosDigitos(Long idUsuario, String ultimosDigitos) {
        Inversor inversor = repositorio.findById(idUsuario).orElseThrow();
        inversor.setUltimosDigitosTarjeta(ultimosDigitos);
        repositorio.save(inversor);
    }

    // --- EL MÉTODO DE REGISTRO CON VALIDACIÓN GLOBAL ---
    public Inversor registrar(Inversor inversor) {
        if (repositorio.existsByEmail(inversor.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (repositorio.existsByDni(inversor.getDni())) {
            throw new RuntimeException("Este Documento de Identidad ya está asociado a otra cuenta");
        }

        // 1. Limpiamos espacios en blanco del DNI por si acaso
        String documentoLimpio = inversor.getDni().trim().toUpperCase();
        inversor.setDni(documentoLimpio);
        String pais = inversor.getResidenciaFiscal();

        // 2. Lógica de validación por país
        if ("España".equalsIgnoreCase(pais)) {
            if (!isDniNieValido(documentoLimpio)) {
                throw new RuntimeException("El DNI o NIE español no es válido o su letra es incorrecta");
            }
        } else {
            // Buscamos la regla Regex de su país
            String regexPais = obtenerRegexPorPais(pais);
            
            if (!documentoLimpio.matches(regexPais)) {
                throw new RuntimeException("El formato del documento no coincide con el estándar de " + pais);
            }
        }

        // 3. Guardamos al usuario
        inversor.setPassword(passwordEncoder.encode(inversor.getPassword()));
        inversor.setRol("USER");
        inversor.setRegistrationDate(java.time.LocalDate.now());
        inversor.setSaldo(0L); 
        
        return repositorio.save(inversor);
    }

    public List<Inversor> obtenerTodos() {
        return repositorio.findAll();
    }

    public Optional<Inversor> obtenerPorId(Long id) {
        return repositorio.findById(id);
    }

    public Optional<Inversor> buscarPorEmail(String email) {
        return repositorio.findByEmail(email);
    }

    public void actualizarSaldo(Long id, Long nuevoSaldoCentimos) {
        Inversor inversor = repositorio.findById(id).orElseThrow();
        inversor.setSaldo(nuevoSaldoCentimos);
        repositorio.save(inversor);
    }

    public void depositar(Long idUsuario, Long cantidadCentimos) {
        Inversor inversor = repositorio.findById(idUsuario).orElseThrow();
        Long saldoActual = inversor.getSaldo() != null ? inversor.getSaldo() : 0L;
        inversor.setSaldo(saldoActual + cantidadCentimos);
        repositorio.save(inversor);
    }

    public void retirar(Long idUsuario, Long cantidadCentimos) throws Exception {
        Inversor inversor = repositorio.findById(idUsuario).orElseThrow();
        Long saldoActual = inversor.getSaldo() != null ? inversor.getSaldo() : 0L;

        if (saldoActual < cantidadCentimos) {
            throw new Exception("Saldo insuficiente");
        }
        inversor.setSaldo(saldoActual - cantidadCentimos);
        repositorio.save(inversor);
    }

    public void actualizarPassword(Long idUsuario, String nuevaPassword) {
        Inversor inversor = repositorio.findById(idUsuario).orElseThrow();
        inversor.setPassword(passwordEncoder.encode(nuevaPassword));
        repositorio.save(inversor);
    }

    /**
     * ACTUALIZACIÓN: Borrado Lógico Profesional.
     * Transfiere el saldo al ADMIN y anonimiza la cuenta para no romper la integridad (Foreign Keys).
     */
    @Transactional
    public void borrarDeVerdad(Long id) {
        Inversor usuario = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. Buscamos al administrador para transferirle los activos
        Inversor admin = repositorio.findAll().stream()
                .filter(u -> "ADMIN".equals(u.getRol()))
                .findFirst()
                .orElse(null);

        // 2. Si el usuario tiene saldo, se le suma al Admin
        if (admin != null && usuario.getSaldo() > 0) {
            admin.setSaldo(admin.getSaldo() + usuario.getSaldo());
            repositorio.save(admin);
            usuario.setSaldo(0L);
        }

        // 3. Eliminamos sus tokens activos
        tokenRepo.deleteByInversor(usuario);

        // 4. ANONIMIZACIÓN: Cambiamos datos sensibles por genéricos
        // Esto evita el error de borrar filas con transacciones asociadas
        usuario.setFirstName("USUARIO");
        usuario.setLastName("ELIMINADO");
        usuario.setDni("DELETED-" + id);
        usuario.setDireccion("N/A");
        usuario.setResidenciaFiscal("N/A");
        
        // Cambiamos email por uno único aleatorio para liberar el original
        usuario.setEmail("deleted-" + UUID.randomUUID().toString() + "@locobrick.com");
        
        // Desactivamos la cuenta y cambiamos el rol
        usuario.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario.setCuentaVerificada(false);
        usuario.setRol("ELIMINADO");

        repositorio.save(usuario);
    }

    // ======================================================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN (DNI OFICIAL Y DICCIONARIO REGEX)
    // ======================================================================

    private String obtenerRegexPorPais(String pais) {
        if (pais == null) return "^[A-Z0-9\\-]{5,20}$";

        switch (pais) {
            case "Estados Unidos": return "^\\d{3}-?\\d{2}-?\\d{4}$";
            case "Reino Unido": return "^[A-Z]{2}\\d{6}[A-Z]$";
            case "Francia": return "^[12]\\d{14}$";
            case "Italia": return "^[A-Z]{6}\\d{2}[A-Z]\\d{2}[A-Z]\\d{3}[A-Z]$";
            case "Alemania": return "^[A-Z0-9]{9,11}$";
            case "Portugal": return "^\\d{9}$";
            case "Andorra": return "^[A-Z]-\\d{6}-[A-Z]$";
            case "México": return "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[0-9A-Z]\\d$";
            case "Argentina": return "^\\d{7,11}$";
            case "Colombia": return "^\\d{6,10}$";
            case "Chile": return "^\\d{7,8}-?[\\dK]$";
            case "Perú": return "^\\d{8}$";
            case "Brasil": return "^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$";
            case "Ecuador": return "^\\d{10}$";
            case "Venezuela": return "^[VEJG]-?\\d{8,9}$";
            case "Uruguay": return "^\\d{7}-?\\d$";
            default: return "^[A-Z0-9\\-]{5,20}$"; 
        }
    }

    private boolean isDniNieValido(String documento) {
        if (documento == null || !documento.matches("^[XYZ]?\\d{5,8}[A-Z]$")) {
            return false;
        }
        
        String letrasOficiales = "TRWAGMYFPDXBNJZSQVHLCKE";
        String numeros = documento.substring(0, documento.length() - 1);
        char letraProporcionada = documento.charAt(documento.length() - 1);

        try {
            if (numeros.startsWith("X")) numeros = numeros.replaceFirst("X", "0");
            else if (numeros.startsWith("Y")) numeros = numeros.replaceFirst("Y", "1");
            else if (numeros.startsWith("Z")) numeros = numeros.replaceFirst("Z", "2");
            
            int numero = Integer.parseInt(numeros);
            char letraCalculada = letrasOficiales.charAt(numero % 23);
            
            return letraCalculada == letraProporcionada;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}