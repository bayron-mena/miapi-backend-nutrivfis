# API REST de Clínica Veterinaria (NutriVifs)

API REST desarrollada con **Spring Boot 3** y **Java** para la gestión de pacientes de una clínica de animales, utilizando **Spring Data JPA** y **MySQL** como motor de base de datos. Está pensada para conectarse con el frontend en React del proyecto [Clinica_NutriVifs](https://github.com/bycode02/Clinica_NutriVifs).

---

## 🛠️ Tecnologías Utilizadas

- **Java 17** (o superior)
- **Spring Boot 3**
- **Spring Data JPA**
- **MySQL Server** (XAMPP o instalación local)
- **Lombok**
- **Maven**

---

## 📋 Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

1. **JDK 17** o superior configurado en las variables de entorno.
2. **MySQL Server** en ejecución (local, con XAMPP o mediante contenedor).
3. **Maven** (opcional si usas el wrapper incluido `./mvnw`).

---

## 🗄️ Base de Datos

Crea la base de datos en tu servidor MySQL antes de iniciar la aplicación (o deja `createDatabaseIfNotExist=true` en la URL para que se cree sola):

```sql
CREATE DATABASE clinica;
```

---

## ⚙️ Configuración (`application.properties`)

Asegúrate de que el archivo `src/main/resources/application.properties` tenga las credenciales correctas de tu MySQL local:

```properties
# Servidor y Puerto
server.port=8080

# Conexión a MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/clinica?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> Si usas XAMPP, el usuario `root` viene sin contraseña. Si tu MySQL tiene una, escríbela en `spring.datasource.password`.

---

## 🚀 Ejecución del Proyecto

1. **Clonar el repositorio:**
   ```bash
   git clone <URL_DE_TU_REPOSITORIO>
   cd <NOMBRE_DEL_PROYECTO>
   ```

2. **Compilar y ejecutar la aplicación:**
   ```bash
   ./mvnw spring-boot:run
   ```
   *(En Windows puedes usar `mvnw.cmd spring-boot:run` o ejecutar `MiapiApplication` directamente desde IntelliJ).*

3. La API estará disponible por defecto en: `http://localhost:8080/pacientes`

---

## 📦 Carga Inicial de Datos

Al iniciar por primera vez, la aplicación ejecuta automáticamente un `CommandLineRunner` (`DataInitializer`) que inserta los siguientes pacientes de prueba si la base de datos está vacía:

| ID | Nombre | Especie | Raza | Edad |
|---|---|---|---|---|
| `1` | Luna | Perro | Labrador | 3 |
| `2` | Milo | Gato | Siamés | 2 |
| `3` | Rocky | Perro | Bulldog | 5 |

---

## 📡 Endpoints de la API

| Método | Endpoint | Descripción | Estado HTTP Exitoso |
| :--- | :--- | :--- | :--- |
| **GET** | `/pacientes` | Obtiene el listado completo de pacientes | `200 OK` |

---

## 🌐 CORS

El controlador permite solicitudes desde el frontend de React en desarrollo mediante:

```java
@CrossOrigin(origins = "http://localhost:5173")
```

Si el frontend corre en otro puerto, modifica ese valor en `PacienteController`.

---

## 📝 Ejemplo de Respuesta JSON

### **GET /pacientes**
```json
[
  {
    "id": 1,
    "nombre": "Luna",
    "especie": "Perro",
    "raza": "Labrador",
    "edad": 3
  },
  {
    "id": 2,
    "nombre": "Milo",
    "especie": "Gato",
    "raza": "Siamés",
    "edad": 2
  }
]
```

---

## 🗂️ Estructura del Proyecto

```
cl.duoc.miapi
├── config        → DataInitializer (datos de prueba)
├── controller    → PacienteController (GET /pacientes)
├── model         → Paciente (entidad JPA)
├── repository    → PacienteRepository (JpaRepository)
├── service       → PacienteService (lógica de listado)
└── MiapiApplication
```
