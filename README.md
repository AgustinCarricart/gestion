# Gestión Local 🍰

Sistema de gestión de pedidos para locales de repostería. Permite administrar clientes, productos y pedidos con control de estados, roles de usuario y autenticación JWT. Deployado en Railway y documentado con Swagger.

🔗 **API en producción:** https://gestion-production-989a.up.railway.app  
📄 **Documentación Swagger:** https://gestion-production-989a.up.railway.app/swagger-ui/index.html

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
- Maven
- Railway (deploy)

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
| GET | `/api/pedidos/fecha?fecha=YYYY-MM-DD` | Pedidos por fecha | ADMIN |
| GET | `/api/pedidos/buscar?nombre=&apellido=` | Buscar por cliente | ADMIN + EMPLEADO |
| GET | `/api/pedidos/cocina/hoy` | Pedidos del día (cocina) | ADMIN + EMPLEADO |
| POST | `/api/pedidos` | Crear pedido | ADMIN |
| PATCH | `/api/pedidos/{publicId}/avanzar` | Avanzar estado | ADMIN + EMPLEADO |
| PATCH | `/api/pedidos/{publicId}/estado?nuevoEstado=` | Cambiar estado | ADMIN + EMPLEADO |
| PATCH | `/api/pedidos/{publicId}/cancelar` | Cancelar pedido | ADMIN |
| PATCH | `/api/pedidos/{publicId}/no-retiro` | Marcar no retiro | ADMIN |

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
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gestion?createDatabaseIfNotExist=true
    username: tu_usuario
    password: tu_contraseña
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    open-in-view: false

jwt:
  secret: "claveSecretaSuperSeguraParaFirmarTokensJWT12345"
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

Generá el hash en [bcrypt-generator.com]( https://bcrypt-generator.com).  
A partir de ahí el ADMIN puede crear nuevos usuarios desde el endpoint `/api/auth/register`.

---

### Tests

El proyecto cuenta con tests unitarios usando JUnit 5 y Mockito, con patrón AAA (Arrange, Act, Assert).

Cobertura actual
Módulo	Tests
PedidoService	Búsqueda por publicId, búsqueda por estado, búsqueda por cliente, transiciones de estado (avanzar, cancelar, marcarNoRetiro, cambiarEstado)

## Deploy

El proyecto está deployado en **Railway** con MySQL como base de datos.  
Para el entorno de producción se usa `application-prod.yaml` activado con la variable de entorno `SPRING_PROFILES_ACTIVE=prod`.

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
- [x] Deploy en Railway
- [x] Pruebas unitarias en capa Service con JUnit 5 y Mockito
- [ ] Frontend
- [ ] Reportes de ventas
