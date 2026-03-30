package com.daw.locobrick.seguridad;

import com.daw.locobrick.modelos.*;
import com.daw.locobrick.repositorios.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            PropiedadRepository propiedadRepo,
            ZonaRepository zonaRepo,
            InversorRepository inversorRepo,
            ComisionRepository comisionRepo,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // 1. CARGAR LAS ZONAS NECESARIAS PARA TUS 25 PROPIEDADES
            if (zonaRepo.count() == 0) {
                zonaRepo.saveAll(List.of(
                        new Zona("Madrid", "Usera"),
                        new Zona("Madrid", "Retiro"),
                        new Zona("Madrid", "Goya"),
                        new Zona("Barcelona", "Eixample"),
                        new Zona("Barcelona", "Gràcia"),
                        new Zona("Barcelona", "Poblenou"),
                        new Zona("Valencia", "Ruzafa"),
                        new Zona("Valencia", "Poblats Marítims"),
                        new Zona("Valencia", "Cabanyal"),
                        new Zona("Alicante", "Playa de San Juan"),
                        new Zona("Málaga", "Teatinos"),
                        new Zona("Málaga", "Soho"),
                        new Zona("Málaga", "Malagueta"),
                        new Zona("Granada", "Albaicín"),
                        new Zona("Granada", "Sagrario"),
                        new Zona("Sevilla", "Nervión"),
                        new Zona("Bilbao", "Abando"),
                        new Zona("Bilbao", "Casco Viejo"),
                        new Zona("Bilbao", "Ensanche"),
                        new Zona("Las Palmas", "Vegueta"),
                        new Zona("Ciudad", "Universidad"),
                        new Zona("Internacional", "GOAT") // Para ANTONY
                ));
            }

            // 2. CARGAR COMISIONES (Base 10000: 200L = 2.00%)
            if (comisionRepo.count() == 0) {
                comisionRepo.saveAll(List.of(
                        new Comision("Apertura", 200L, "Comisión del 2.00% aplicada al realizar una inversión."),
                        new Comision("Gestión Mantenimiento", 150L,
                                "Comisión anual del 1.50% por gestión del inmueble."),
                        new Comision("Salida", 100L,
                                "Comisión del 1.00% aplicada al vender participaciones anticipadamente.")));
            }

            // 3. CARGAR USUARIOS (¡Con los nuevos campos obligatorios y dirección
            // profesional!)
            if (inversorRepo.count() == 0) {
                Inversor admin = new Inversor("Aitor", "De la Cueva", "admin@locobrick.com",
                        passwordEncoder.encode("password"), LocalDate.now());
                admin.setRol("ADMIN");
                admin.setSaldo(10000000L); // 100.000,00 €
                admin.setDni("12345678Z");
                admin.setResidenciaFiscal("España");
                // Dirección formateada: Vía, Nº, Piso, CP, Ciudad, Provincia
                admin.setDireccion("Calle Gran Vía, 1, 4º Izquierda, 28013, Madrid, Madrid");
                admin.setCuentaVerificada(true);
                inversorRepo.save(admin);

                Inversor user = new Inversor("Inversor", "Demo", "user@locobrick.com",
                        passwordEncoder.encode("password"), LocalDate.now());
                user.setRol("USER");
                user.setSaldo(500000L); // 5.000,00 €
                user.setDni("87654321X");
                user.setResidenciaFiscal("España");
                // Dirección formateada: Vía, Nº, Piso, CP, Ciudad, Provincia
                user.setDireccion("Avenida Diagonal, 450, 2º 1ª, 08006, Barcelona, Barcelona");
                user.setCuentaVerificada(true);
                inversorRepo.save(user);
            }

            // 4. CARGAR TUS 25 PROPIEDADES EXACTAS (Convertidas a céntimos)
            if (propiedadRepo.count() == 0) {
                List<Zona> zonas = zonaRepo.findAll();

                propiedadRepo.saveAll(List.of(
                        new Propiedad("ANTONY", buscarZona(zonas, "GOAT"), 9500000000L, "GOAT", "Disponible"),
                        new Propiedad("Ático Lujo Eixample", buscarZona(zonas, "Eixample"), 85000000L,
                                "Espectacular con vistas a la Sagrada Familia.", "Disponible"),
                        new Propiedad("Estudio Joven Ruzafa", buscarZona(zonas, "Ruzafa"), 16500000L,
                                "Ideal para nómadas digitales.", "Disponible"),
                        new Propiedad("Piso Señorial Retiro", buscarZona(zonas, "Retiro"), 120000000L,
                                "A escasos pasos del parque.", "Disponible"),
                        new Propiedad("Loft Creativo Gràcia", buscarZona(zonas, "Gràcia"), 34000000L,
                                "Espacio diáfano con bóveda catalana.", "Disponible"),
                        new Propiedad("Apartamento Playa San Juan", buscarZona(zonas, "Playa de San Juan"), 29000000L,
                                "Segunda línea de playa con piscina.", "Disponible"),
                        new Propiedad("Chalet Adosado Teatinos", buscarZona(zonas, "Teatinos"), 41000000L,
                                "Cerca de la ciudad universitaria.", "Disponible"),
                        new Propiedad("Piso Histórico Albaicín", buscarZona(zonas, "Albaicín"), 24500000L,
                                "Vistas parciales a la Alhambra.", "Disponible"),
                        new Propiedad("Estudio Rentable Universidad", buscarZona(zonas, "Universidad"), 11500000L,
                                "Alta demanda estudiantil.", "Disponible"),
                        new Propiedad("Bajo con Jardín Vegueta", buscarZona(zonas, "Vegueta"), 19500000L,
                                "Tranquilidad en el centro histórico.", "Disponible"),
                        new Propiedad("Piso Señorial Abando", buscarZona(zonas, "Abando"), 59000000L,
                                "Vivienda de gran tamaño para directivos.", "Disponible"),
                        new Propiedad("Local Hostelería Casco Viejo", buscarZona(zonas, "Casco Viejo"), 34000000L,
                                "Ubicado en las Siete Calles. Tránsito continuo.", "Disponible"),
                        new Propiedad("Estudio Ensanche Inversión", buscarZona(zonas, "Ensanche"), 19000000L,
                                "Rentabilidad estable enfocada a profesionales.", "Disponible"),
                        new Propiedad("Piso Estudiantes Poblats", buscarZona(zonas, "Poblats Marítims"), 16000000L,
                                "Cerca de los campus. 4 habitaciones.", "Disponible"),
                        new Propiedad("Loft Diseño Ruzafa", buscarZona(zonas, "Ruzafa"), 27500000L,
                                "Acabados de lujo en el barrio de moda.", "Disponible"),
                        new Propiedad("Bajo Comercial Cabanyal", buscarZona(zonas, "Cabanyal"), 19000000L,
                                "Potencial de reconversión a vivienda.", "Disponible"),
                        new Propiedad("Dúplex Moderno Soho", buscarZona(zonas, "Soho"), 52000000L,
                                "Diseño vanguardista en el barrio de las artes.", "Disponible"),
                        new Propiedad("Apartamento Malagueta Beach", buscarZona(zonas, "Malagueta"), 36000000L,
                                "A 100 metros de la arena. Rentabilidad dual.", "Disponible"),
                        new Propiedad("Piso Nervión Plaza", buscarZona(zonas, "Nervión"), 28000000L,
                                "Residencial consolidado junto al estadio.", "Disponible"),
                        new Propiedad("Edificio Pequeño Nervión", buscarZona(zonas, "Nervión"), 95000000L,
                                "Bloque de 3 viviendas ideal para fraccionar.", "Disponible"),
                        new Propiedad("Piso Familiar Eixample", buscarZona(zonas, "Eixample"), 48000000L,
                                "Vivienda clásica con techos altos.", "Disponible"),
                        new Propiedad("Oficina Startup Poblenou", buscarZona(zonas, "Poblenou"), 55000000L,
                                "Distrito 22@. Alquilada a tecnológica.", "Disponible"),
                        new Propiedad("Piso Reformado Goya", buscarZona(zonas, "Goya"), 62000000L,
                                "Exterior muy luminoso en plena Milla de Oro.", "Disponible"),
                        new Propiedad("Estudio Coliving Usera", buscarZona(zonas, "Usera"), 13500000L,
                                "Preparado para modelo de vivienda compartida.", "Disponible"),
                        new Propiedad("Piso Compartido Sagrario", buscarZona(zonas, "Sagrario"), 22000000L,
                                "5 habitaciones. Alta rentabilidad bruta.", "Disponible")));
            }
        };
    }

    private Zona buscarZona(List<Zona> zonas, String distrito) {
        return zonas.stream()
                .filter(z -> z.getDistrict().equalsIgnoreCase(distrito))
                .findFirst()
                .orElse(zonas.get(0));
    }
}