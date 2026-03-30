# 🧱 LocoBrick - Plataforma de Inversión Inmobiliaria Fraccionada

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-563D7C?style=for-the-badge&logo=bootstrap&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-%23005F0F.svg?style=for-the-badge&logo=Thymeleaf&logoColor=white)

**LocoBrick** es una aplicación web (PropTech/Fintech) desarrollada con Spring Boot que democratiza la inversión inmobiliaria. Permite a los usuarios comprar participaciones de propiedades desde tan solo 100€, generar rentas mensuales y gestionar su cartera de activos de forma intuitiva.

---

## ✨ Características Principales

* 🏠 **Catálogo de Inversiones:** Exploración de proyectos inmobiliarios con filtros dinámicos por ciudad y presupuesto máximo.
* 💰 **Billetera Virtual (Wallet):** Sistema de depósitos y retiros de capital integrados con validaciones de saldo en tiempo real.
* 📈 **Dashboard del Inversor:** Panel de control ("Mi Cartera") que muestra el capital invertido, el saldo disponible, la rentabilidad estimada y gráficos de diversificación generados con *Chart.js*.
* 🔄 **Mercado Secundario:** Opción de venta parcial o total de participaciones inmobiliarias.
* 🧾 **Generación de Facturas:** Creación y descarga automática de recibos en formato PDF (vía *iTextPDF*) al realizar compras o recibir dividendos.
* 🔐 **Seguridad y Validación:** Registro seguro de usuarios con encriptación de contraseñas y validación mediante expresiones regulares (Regex) de documentos de identidad a nivel internacional (DNI, NIE, Pasaportes, etc.).
* 🛡️ **Panel de Administración:** Interfaz exclusiva para el rol `ADMIN` para crear, editar, gestionar y eliminar el inventario de propiedades.
* 🤖 **Asistente Virtual (LocoBot):** Chatbot interactivo integrado en el frontend para guiar a los usuarios en sus primeros pasos y resolver dudas frecuentes.

---

## 🛠️ Stack Tecnológico

**Backend:**
* Java 17+
* Spring Boot (Web, Data JPA, Security)
* iTextPDF (Generación de documentos)

**Frontend:**
* HTML5 / CSS3 / JavaScript
* Thymeleaf (Motor de plantillas)
* Bootstrap 5 (Diseño responsive y componentes)
* Chart.js (Visualización de datos)

**Base de Datos:**
* MySQL (Conexión vía XAMPP / Hibernate ORM)

---

## 🚀 Instalación y Despliegue Local

Sigue estos pasos para ejecutar el proyecto en tu máquina local:

### 1. Requisitos Previos
* Tener instalado [Java JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) o superior.
* Tener instalado un servidor MySQL (por ejemplo, [XAMPP](https://www.apachefriends.org/es/index.html)).
* Maven para la gestión de dependencias.

### 2. Configuración de la Base de Datos
1.  Abre XAMPP y arranca el servicio de MySQL.
2.  Entra en *phpMyAdmin* (o tu cliente SQL favorito) y crea una base de datos vacía llamada:
    ```sql
    CREATE DATABASE LocoBrick_DB;
    ```

### 3. Configurar Propiedades
Verifica el archivo `src/main/resources/application.properties` para asegurarte de que las credenciales de la base de datos coinciden con las de tu entorno local:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/LocoBrick_DB?serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
