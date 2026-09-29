# Gestión Local 🍰

Sistema de gestión de pedidos para locales de repostería. Permite administrar clientes, productos y pedidos con control de estados, roles de usuario y autenticación JWT. Incluye frontend integrado accesible desde el navegador.

💻 **Código en GitHub:** [github.com/AgustinCarricart/gestion](https://github.com/AgustinCarricart)

---

## Tecnologías

- Java 17
- Spring Boot 3.4.5
- Spring Security + JWT (jjwt 0.11.5)
- Spring Data JPA / Hibernate
- MySQL
- MapStruct 1.5.5
- Lombok
- Springdoc OpenAPI (Swagger) 2.8.6
- JUnit 5 + Mockito (tests)
- Maven

---

## Arquitectura

El proyecto sigue una arquitectura en capas estricta con separación por módulo:

```
Controller → Service → Repository → Base de datos
```

```
com.gestionlocal.gestion
├── auth
│   ├── AuthController
│   ├── AuthService
│   ├── AuthRequest
│   ├── AuthResponse
│   ├── RegisterRequest
│   └── ResetPasswordRequest
├── cliente
│   ├── dto
│   ├── exception
│   ├── mapper
│   ├── ClienteController
│   ├── ClienteEntity
│   ├── ClienteRepository
│   └── ClienteService
├── producto
│   ├── dto
│   ├── exception
│   ├── mapper
│   ├── ProductoController
│   ├── ProductoEntity
│   ├── ProductoRepository
│   ├── ProductoService
│   └── TipoProducto (enum)
├── pedido
│   ├── dto
│   ├── exception
│   ├── mapper
│   ├── DetallePedidosEntity
│   ├── EstadoPedido (enum)
│   ├── PedidoController
│   ├── PedidoEntity
│   ├── PedidoRepository
│   └── PedidoService
├── usuario
│   ├── RolEnum
│   ├── UsuarioDetailsServiceImpl
│   ├── UsuarioEntity
│   ├── UsuarioRepository
│   ├── UsuarioResponseDto
│   └── UsernameAlreadyExistsException
├── security
│   ├── JwtAuthFilter
│   ├── JwtService
│   └── SecurityConfig
└── config
    ├── CorsConfig
    ├── GlobalExceptionHandler
    ├── ErrorResponse
    ├── ValidationErrorResponse
    └── SwaggerConfig
```

---

## Seguridad

El sistema usa autenticación **JWT** con dos roles:

| Rol | Permisos |
|-----|----------|
| `ADMIN` | Acceso total — crear/editar/eliminar clientes, productos, pedidos y usuarios |
| `EMPLEADO` | Ver pedidos del día y avanzar estado |

---

## Tipos de producto y cálculo de precio

| Tipo | Descripción | Cálculo |
|------|-------------|---------|
| `TORTA_COMUN` | Torta estándar precio fijo | `precioFijo` |
| `TORTA_POR_KILO` | Torta grande vendida por peso | `precio × pesoKg` |
| `TORTA_POR_KILO_FRUTA` | Igual que anterior con fruta | `precio × pesoKg` |
| `INDIVIDUAL` | Porción individual precio fijo | `precio × cantidad` |
| `MINI` | Porción pequeña precio fijo | `precio × cantidad` |
| `POR_DOCENA` | Alfajores, medialunas, etc. Mínimo 12 unidades | `precioDocena × docenas` |

---

## Estados de pedido

```
PENDIENTE → LISTO → ENTREGADO
                 ↘ NO_RETIRO

CANCELADO (desde cualquier estado activo)
```

---

## Frontend

El proyecto incluye un frontend integrado en `src/main/resources/static/index.html` que se sirve automáticamente con Spring Boot.

Al levantar el proyecto, accedés desde el navegador en `http://localhost:8080`.

**Panel ADMIN:**
- Gestión de pedidos con filtros combinados por fecha, estado y nombre de cliente
- Crear pedidos con múltiples productos
- Gestión de clientes y productos (CRUD completo)
- Gestión de usuarios (crear, resetear contraseña)

**Panel EMPLEADO (Cocina):**
- Vista de pedidos del día ordenados por hora de entrega
- Marcar pedidos como LISTO

**Uso en red local:** si el servidor corre en una PC de la red, las demás pueden acceder reemplazando `localhost` por la IP local del servidor (ej: `http://192.168.1.100:8080`).

---

## Endpoints

### Auth

| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| POST | `/api/auth/login` | Login | Público |
| POST | `/api/auth/register` | Crear usuario | ADMIN |
| GET | `/api/auth/usuarios` | Listar usuarios | ADMIN |
| PATCH | `/api/auth/usuarios/{publicId}/reset-password` | Resetear contraseña | ADMIN |

### Clientes

| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| GET | `/api/clientes` | Listar todos | ADMIN |
| GET | `/api/clientes/activos` | Listar activos | ADMIN |
| GET | `/api/clientes/{publicId}` | Buscar por ID | ADMIN |
| GET | `/api/clientes/buscar?nombre=&apellido=` | Buscar por nombre | ADMIN |
| POST | `/api/clientes` | Crear cliente | ADMIN |
| PUT | `/api/clientes/{publicId}` | Modificar cliente | ADMIN |
| DELETE | `/api/clientes/{publicId}` | Baja lógica | ADMIN |
| PATCH | `/api/clientes/{publicId}/reactivar` | Reactivar cliente | ADMIN |

### Productos

| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| GET | `/api/productos` | Listar todos | ADMIN |
| GET | `/api/productos/{publicId}` | Buscar por ID | ADMIN |
| GET | `/api/productos/tipo/{tipo}` | Filtrar por tipo | ADMIN |
| POST | `/api/productos` | Crear producto | ADMIN |
| PUT | `/api/productos/{publicId}` | Modificar producto | ADMIN |
| DELETE | `/api/productos/{publicId}` | Baja lógica | ADMIN |
| PATCH | `/api/productos/{publicId}/reactivar` | Reactivar producto | ADMIN |

### Pedidos

| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| GET | `/api/pedidos` | Listar todos | ADMIN + EMPLEADO |
| GET | `/api/pedidos/{publicId}` | Ver pedido | ADMIN + EMPLEADO |
| GET | `/api/pedidos/cliente/{clientePublicId}` | Pedidos por cliente | ADMIN + EMPLEADO |
| GET | `/api/pedidos/estado/{estado}` | Pedidos por estado | ADMIN + EMPLEADO |
| GET | `/api/pedidos/fecha?fecha=YYYY-MM-DD` | Pedidos por fecha | ADMIN + EMPLEADO |
| GET | `/api/pedidos/buscar?nombre=&apellido=` | Buscar por cliente | ADMIN + EMPLEADO |
| GET | `/api/pedidos/cocina/hoy` | Pedidos del día (cocina) | ADMIN + EMPLEADO |
| POST | `/api/pedidos` | Crear pedido | ADMIN |
| PATCH | `/api/pedidos/{publicId}/avanzar` | Avanzar estado | ADMIN + EMPLEADO |
| PATCH | `/api/pedidos/{publicId}/estado?nuevoEstado=` | Cambiar estado | ADMIN + EMPLEADO |
| PATCH | `/api/pedidos/{publicId}/cancelar` | Cancelar pedido | ADMIN |
| PATCH | `/api/pedidos/{publicId}/no-retiro` | Marcar no retiro | ADMIN |

---

## Tests

Tests unitarios con **JUnit 5** y **Mockito**, patrón AAA (Arrange, Act, Assert).

| Módulo | Cobertura |
|--------|-----------|
| `ClienteService` | Listar todos, listar activos, buscar por ID, buscar por nombre, crear, actualizar, baja lógica, reactivar |
| `ProductoService` | Listar todos, listar por tipo, buscar por ID, crear, actualizar, baja lógica, reactivar |
| `PedidoService` | Buscar por ID, por estado, por cliente — transiciones de estado (avanzar, cancelar, marcarNoRetiro, cambiarEstado) |

```bash
./mvnw test
```

---

## Cómo correr el proyecto localmente

### Requisitos

- Java 17
- MySQL corriendo localmente
- Maven

### Configuración

En `src/main/resources/application.yaml`:

```yaml
server:
  port: ${PORT:8080}

spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false
    open-in-view: false

jwt:
  secret: ${JWT_SECRET:claveSecretaSuperSeguraParaFirmarTokensJWT12345}
  expiration: 86400000
```

### Correr

```bash
./mvnw spring-boot:run
```

### Primer usuario

Al levantar el proyecto las tablas se crean automáticamente. Insertá el primer usuario ADMIN en la BD:

```sql
INSERT INTO usuario (public_id, username, password, rol, activo)
VALUES (UUID_TO_BIN(UUID()), 'admin', '$2a$12$TU_HASH_BCRYPT', 'ADMIN', 1);
```

Generá el hash en [bcrypt-generator.com](https://bcrypt-generator.com).  
A partir de ahí el ADMIN puede crear nuevos usuarios desde el frontend o desde el endpoint `/api/auth/register`.

---

## Estado del proyecto

- [x] Gestión completa de Clientes (CRUD + baja lógica + reactivar + buscar)
- [x] Gestión completa de Productos (CRUD + baja lógica + reactivar + filtrar por tipo)
- [x] Gestión completa de Pedidos con múltiples ítems
- [x] Cálculo automático de precios según tipo de producto
- [x] Flujo de estados de pedido
- [x] Vista de cocina (pedidos del día)
- [x] Autenticación JWT con roles ADMIN y EMPLEADO
- [x] Gestión de usuarios (crear, listar, resetear contraseña)
- [x] Manejo global de excepciones con respuestas JSON estructuradas
- [x] Documentación con Swagger / OpenAPI
- [x] Tests unitarios — ClienteService, ProductoService y PedidoService
- [ ] Métricas y reportes de ventas
