# API REST Clínica NutriVifs

Backend de la clínica veterinaria NutriVifs, hecho con **Spring Boot 4** y **Java 21**.
Maneja catálogo de productos y categorías, pacientes, usuarios con login JWT y roles,
compras con boleta y reportes para el dashboard. Lo consume el frontend en React
([Clinica_NutriVifs](https://github.com/bycode02/Clinica_NutriVifs)).

---

## Tecnologías

- Java 21, Spring Boot 4 (Web MVC, Data JPA, Validation, Security)
- JWT con `spring-boot-starter-oauth2-resource-server` (HS256) y contraseñas con BCrypt
- MySQL / MariaDB (XAMPP)
- springdoc-openapi (Swagger UI)
- Lombok, Maven (wrapper incluido)
- JUnit 6, Mockito y AssertJ para las pruebas

---

## Cómo ejecutarlo

### Requisitos

1. **JDK 21** (en IntelliJ: *File → Project Structure → SDK*).
2. **MySQL o MariaDB** corriendo en `localhost:3306` con el usuario `root`.
   Con XAMPP basta con iniciar **MySQL** en el panel de control.

La base `bd_clinica` y sus tablas se crean solas al arrancar.

### Ejecutar

```bash
./mvnw spring-boot:run
```

En Windows (PowerShell), si `JAVA_HOME` no apunta a un JDK 21:

```powershell
$env:JAVA_HOME = "C:\ruta\a\jdk-21"; .\mvnw spring-boot:run
```

También se puede ejecutar `MiapiApplication` desde IntelliJ.

- API: `http://localhost:8080/api/v1/...`
- Swagger: **http://localhost:8080/swagger-ui.html**

### Probar con Swagger

1. Ejecuta `POST /api/v1/auth/login` con uno de los usuarios de prueba.
2. Copia el `token` de la respuesta.
3. Pulsa **Authorize** (arriba a la derecha), pega el token y acepta.
   Desde ahí todas las peticiones van con `Authorization: Bearer <token>`.

---

## Configuración

Todo está en `src/main/resources/application.properties`. Los valores con `${VARIABLE:defecto}`
se pueden cambiar con variables de entorno, sin tocar el archivo.

| Variable de entorno | Por defecto | Para qué sirve |
|---|---|---|
| `DB_PASSWORD` | *(vacía)* | Clave del usuario `root` de MySQL |
| `DB_DIALECT` | `MariaDBDialect` | XAMPP trae MariaDB. Con MySQL 8 usa `org.hibernate.dialect.MySQLDialect` |
| `JWT_SECRET` | clave de desarrollo | Firma de los JWT, **mínimo 32 caracteres**. Cámbiala fuera de desarrollo |
| `JWT_EXPIRATION_MINUTES` | `60` | Duración del JWT |
| `JWT_REFRESH_EXPIRATION_DAYS` | `7` | Duración del refresh token |
| `SEED_USERS` | `true` | Crea los usuarios de prueba. Usa `false` en producción |
| `SEED_ADMIN_PASSWORD`, `SEED_SELLER_PASSWORD`, `SEED_CLIENT_PASSWORD` | ver archivo | Claves de los usuarios de prueba |

Otros valores del archivo: `app.timezone` (zona para agrupar ventas por día, `America/Santiago`)
y `app.reports.low-stock-threshold` (stock bajo en el dashboard, `5`).

---

## Datos iniciales

`DataInitializer` carga estos datos al arrancar, solo si no existen:

- **Usuarios de prueba**, uno por rol. Las claves están en `application.properties`
  (`app.seed.*.password`); el admin es el mismo que usaba el frontend.

  | Correo | Rol |
  |---|---|
  | `admin@duoc.cl` | `ADMIN` |
  | `vendedor@duoc.cl` | `SELLER` |
  | `cliente@duoc.cl` | `CLIENT` |

- **9 categorías** y **22 productos** (`ME001` a `ME022`), el mismo catálogo que tenía el frontend.
- **3 pacientes** de ejemplo.

---

## Roles y permisos

| Recurso | Anónimo | Cliente | Vendedor | Admin |
|---|---|---|---|---|
| Ver productos y categorías | ✅ | ✅ | ✅ | ✅ |
| Crear y editar productos, cambiar stock | — | — | ✅ | ✅ |
| Eliminar productos | — | — | — | ✅ |
| Crear, editar y eliminar categorías | — | — | — | ✅ |
| Pacientes | — | — | — | ✅ |
| Comprar, ver su propio historial y sus boletas | — | ✅ | ✅ | ✅ |
| Ver todas las ventas | — | — | ✅ | ✅ |
| Reportes del dashboard | — | — | — | ✅ |

Sin token la API responde **401**; con un rol sin permiso responde **403**.

---

## Endpoints

Todas las rutas empiezan con `/api/v1`. El detalle completo, con ejemplos, está en Swagger.

### Autenticación — `/auth`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/register` | Crea una cuenta de cliente y devuelve los tokens (201) |
| POST | `/auth/login` | Valida correo y clave y devuelve los tokens |
| POST | `/auth/refresh` | Cambia un refresh token por un JWT y un refresh token nuevos |
| POST | `/auth/logout` | Invalida el refresh token (204) |

Respuesta de `register`, `login` y `refresh`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "expiresIn": 3600,
  "refreshToken": "qN3...",
  "refreshExpiresIn": 604800,
  "name": "Cliente",
  "lastName": "Prueba",
  "email": "cliente@duoc.cl",
  "birthDate": "2000-08-20",
  "phone": "+56922222222",
  "role": "CLIENT"
}
```

- **3 claves incorrectas bloquean la cuenta** (403), igual que en el frontend.
- **Rotación:** cada refresh token sirve una sola vez. Si se reutiliza uno ya rotado (posible robo),
  se cierran todas las sesiones del usuario.

### Productos — `/products`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/products?categoryId=` | Lista el catálogo (filtro opcional por categoría) |
| GET | `/products/{id}` | Un producto (`id` es el código, ej. `ME001`) |
| POST | `/products` | Crea un producto |
| PUT | `/products/{id}` | Edita un producto (el código no cambia) |
| PATCH | `/products/{id}/stock` | Cambia solo el stock: `{ "stock": 4 }` |
| DELETE | `/products/{id}` | Elimina un producto |

```json
{
  "id": "ME016",
  "name": "Vetmedin 2.5mg",
  "distributor": "Distribuidora VetSur",
  "description": null,
  "price": 28000,
  "stock": 6,
  "image": "Vetmedin.jpg",
  "activeIngredient": "Pimobendan — Blíster 10 comp.",
  "species": "Perro",
  "category": { "id": 6, "name": "CARDÍACO" }
}
```

Al crear o editar, la categoría se envía solo con su id: `"category": { "id": 6 }`.

### Categorías — `/categories`

`GET /categories`, `GET /categories/{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`.
Los nombres se guardan en MAYÚSCULAS. Una categoría con productos no se puede eliminar (409).

### Pacientes — `/patients`

`GET /patients`, `GET /patients/{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`.
Campos: `name`, `species`, `breed`, `age`, `owner`.

### Boletas — `/receipts`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/receipts` | Confirma la compra del carrito: descuenta stock y crea la boleta (201) |
| GET | `/receipts/me` | Historial de compras del usuario conectado |
| GET | `/receipts/{id}` | Una boleta (su dueño, Vendedor o Admin; a otros les responde 404) |
| GET | `/receipts?from=&to=` | Todas las ventas del período (Vendedor y Admin) |

Cuerpo de `POST /receipts` (`price` es el precio que vio el cliente):

```json
{ "items": [ { "productId": "ME001", "quantity": 2, "price": 4200 } ] }
```

- **Comprador:** siempre es el usuario del token, nunca uno indicado en el cuerpo.
- **Validación completa antes de descontar:** si un producto no tiene stock o cambió de precio,
  responde 409 y no se descuenta nada.
- **Compras simultáneas:** los productos se bloquean (`SELECT ... FOR UPDATE`) mientras se confirma
  la compra, así dos compras no pueden vender la misma última unidad.
- **Copia del producto:** la boleta guarda nombre, categoría y precio de ese momento. Editar o
  borrar un producto después no cambia las boletas antiguas.
- **IVA:** el total incluye IVA (19%) y la boleta trae el desglose `netAmount` + `taxAmount`.

### Reportes — `/reports`

`GET /reports/sales?from=2026-11-01&to=2026-11-30` (solo Admin) devuelve los datos del dashboard:
total vendido, cantidad de boletas, ticket promedio, unidades, compradores, ventas por día
(incluye días en 0), los 5 productos más vendidos, ventas por categoría y productos con poco stock.

Las fechas van como `yyyy-MM-dd` y ambos días se incluyen. Sin fechas: los últimos 30 días.

---

## Errores

Todos los errores tienen el mismo formato:

```json
{
  "status": 400,
  "message": "Hay datos inválidos",
  "errors": { "password": "La contraseña debe tener entre 4 y 13 caracteres y al menos una mayúscula." }
}
```

`errors` aparece solo en las validaciones de formularios, con un mensaje por campo. Los mensajes
son los mismos del frontend (`src/utils/validaciones.ts`, `registro.ts` y `administracion.ts`).

| Código | Cuándo |
|---|---|
| 400 | Datos inválidos |
| 401 | Sin sesión, token vencido o credenciales incorrectas |
| 403 | El rol no tiene permiso, o la cuenta está bloqueada |
| 404 | No existe |
| 409 | Conflicto: correo o código repetido, sin stock, precio cambiado |

---

## Pruebas

```bash
./mvnw test
```

| Clase | Qué prueba |
|---|---|
| `AuthServiceTest` | Registro (BCrypt, correo normalizado, rol CLIENT), login, bloqueo a los 3 intentos |
| `RefreshTokenServiceTest` | Solo se guarda el hash, rotación, reuso de un token rotado, logout, vencimiento |
| `ReceiptServiceTest` | Compra con IVA, nada se descuenta si un producto falla, precio cambiado, boleta ajena (404) |
| `ProductServiceTest` | Código único, stock mínimo al crear, edición, cambio de stock |
| `CategoryServiceTest` | Mayúsculas, nombre repetido, no borrar con productos |
| `ReportServiceTest` | Totales, ventas por día en hora de Chile, ranking de productos y categorías |
| `DateRangeTest` | Períodos por defecto y fechas inválidas |
| `FormRulesTest` | Que las reglas de los formularios coincidan con las del frontend |
| `MiapiApplicationTests` | Que la aplicación arranque. **Necesita MySQL/MariaDB corriendo** |

Todas las pruebas, salvo `MiapiApplicationTests`, son unitarias (Mockito): no usan la base de datos.

---

## Estructura

```
cl.duoc.miapi
├── config       DataInitializer (datos iniciales), OpenApiConfig (Swagger),
│                GlobalExceptionHandler (errores en JSON), ChileRegions
├── controller   Auth, Product, Category, Patient, Receipt, Report
├── dto          Requests y responses de la API (la entidad User nunca se devuelve)
├── model        User, Role, Product, Category, Patient, Receipt, ReceiptDetail, RefreshToken
├── repository   Repositorios JPA
├── security     SecurityConfig (permisos, CORS, JWT), JwtService, JsonSecurityHandlers
└── service      Lógica de negocio
```

---

## CORS

Cada controlador tiene `@CrossOrigin(originPatterns = "${app.cors.allowed-origins}")`, y los
orígenes permitidos se configuran en un solo lugar, en `application.properties`:

```properties
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:[*],http://127.0.0.1:[*]}
```

Por defecto acepta `localhost` en **cualquier puerto** (5173, 5174, 3000...), así el frontend
funciona aunque Vite cambie de puerto. Para limitarlo a puertos específicos, sepáralos con coma:

```properties
app.cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

o usa la variable de entorno `CORS_ALLOWED_ORIGINS`. `SecurityConfig` usa `.cors(Customizer.withDefaults())`,
que toma estos `@CrossOrigin`; por eso las respuestas 401 y 403 también llevan los headers CORS
y el frontend puede leer el mensaje de error. Los orígenes que no están en la lista reciben 403.
