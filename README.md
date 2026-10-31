# Gestión Local — Sistema de Pedidos y Cocina (Tortas Olivia) 🍰

Sistema backend en producción diseñado para la gestión de pedidos, catálogo de productos, control de señas y saldos pendientes, reportes mensuales de ventas y vista de cocina para el comercio gastronómico **Tortas Olivia**.

💻 **Repositorio:** [github.com/AgustinCarricart/gestion](https://github.com/AgustinCarricart/gestion)

---

## 🛠️ Tecnologías

- **Java 17**
- **Spring Boot 3.4.5** (Spring Web, Spring Data JPA, Spring Security, Bean Validation)
- **Spring Security + JWT** (`jjwt` 0.11.5) + encriptación `BCrypt`
- **PostgreSQL** (Supabase en producción / Docker en entorno local)
- **Hibernate / JPA** (con `@EntityGraph` e índices `@Index`)
- **MapStruct 1.5.5** + **Lombok**
- **Springdoc OpenAPI / Swagger UI** (`2.8.6`)
- **JUnit 5 + Mockito** (Tests unitarios)
- **Docker & Docker Compose**

---

## 🏗️ Arquitectura

El proyecto sigue una **arquitectura en capas estricta** organizada por módulos de dominio:

```text
Controller (DTOs) → Service (Lógica de Negocio + @Transactional) → Repository (JPA) → PostgreSQL
```

### Decisiones técnicas principales:
1. **Patrón de Clave Dual (`Long id` + `UUID publicId`):** Las entidades utilizan `Long` autoincremental como clave primaria interna para las relaciones en la base de datos, y exponen únicamente un `UUID publicId` en los DTOs y endpoints REST.
2. **Optimización de Consultas JPA:** Configuración de `spring.jpa.open-in-view: false` junto con `@EntityGraph(attributePaths = {"detalles", "detalles.producto"})` en `PedidoRepository` para evitar el problema de consultas $N+1$ al listar pedidos con sus ítems y productos.
3. **Modelo de Pedidos Ágil con Índices:** Los datos de contacto del cliente (`nombreCliente`, `telefonoCliente`) están integrados e indexados (`@Index`) directamente en la entidad `PedidoEntity` para agilizar la toma de pedidos en mostrador.
4. **Inicialización Automática (`CommandLineRunner`):** Al iniciar la aplicación, se verifica e inicializa automáticamente el catálogo base de 43 productos (`CatalogoDataInitializer`) y los usuarios por defecto (`AdminUserInitializer`).

### Estructura de paquetes:
```text
com.gestionlocal.gestion
├── auth           # Login, registro de usuarios y reseteo de contraseñas
├── producto       # CRUD de catálogo, baja lógica, reactivación y filtro por categoría
├── pedido         # Flujo de estados, cálculo de precios por tipo/peso, señas y reportes mensuales
├── usuario        # Entidad de usuario, roles e integración con UserDetails de Spring Security
├── security       # Filtro JWT (OncePerRequestFilter), generación de tokens y SecurityConfig
├── config         # Manejo global de excepciones (@RestControllerAdvice), Swagger e inicializadores
├── licencia       # Configuración del plan activo del comercio
└── notificacion   # Avisos internos de actualización de pedidos para la vista de cocina
```

---

## 🔐 Seguridad y Roles

El sistema utiliza autenticación **JWT Stateless** (`SessionCreationPolicy.STATELESS`):

| Rol | Permisos |
| :--- | :--- |
| `ADMIN` / `SUPERADMIN` | Acceso total: crear, editar, cancelar y eliminar pedidos, registrar señas, consultar reportes mensuales de ventas, administrar el catálogo de productos y gestionar usuarios. |
| `COCINA` / `EMPLEADO` | Consultar pedidos activos y pedidos del día en la vista de cocina, y avanzar el estado de elaboración (`PENDIENTE` → `LISTO` → `ENTREGADO`). |

---

## 🧮 Tipos de Producto, Precios y Estados

### Cálculo automático de precios según tipo de producto
| Tipo | Descripción | Regla y Cálculo |
| :--- | :--- | :--- |
| `TORTA_COMUN` | Torta estándar de catálogo | `precioFijo × cantidad` (o cotización por kilo si se especifica un peso entre `1.5 kg` y `5.0 kg`) |
| `TORTA_POR_KILO` / `TORTA_POR_KILO_FRUTA` | Torta grande vendida por peso | Rango permitido de `> 1.5 kg` a `5.0 kg` (`precioPorKilo × pesoKg`) |
| `INDIVIDUAL` | Porción o producto individual | `precio × cantidad` |
| `MINI` / `POR_DOCENA` | Alfajores, shots, mini pastelería | Compra mínima de **12 unidades** (`precio × cantidad`) |

### Flujo de estados de un pedido
```text
PENDIENTE ──► LISTO ──► ENTREGADO
                └──► NO_RETIRO

CANCELADO (desde cualquier estado activo previo a ENTREGADO)
```

---

## 📡 Endpoints Principales

### Auth (`/api/auth`)
| Método | Endpoint | Descripción | Rol |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Iniciar sesión y obtener token JWT | Público |
| `GET` | `/api/auth/health` | Estado del servidor | Público |
| `POST` | `/api/auth/register` | Crear nuevo usuario | `ADMIN` |
| `GET` | `/api/auth/usuarios` | Listar todos los usuarios | `ADMIN` |
| `PATCH` | `/api/auth/usuarios/{publicId}/reset-password` | Resetear contraseña de un usuario | `ADMIN` |

### Productos (`/api/productos`)
| Método | Endpoint | Descripción | Rol |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/productos` | Listar productos activos | Público |
| `GET` | `/api/productos/{publicId}` | Buscar producto por ID público | Público |
| `GET` | `/api/productos/tipo/{tipo}` | Filtrar productos activos por tipo | Público |
| `POST` | `/api/productos` | Crear nuevo producto | `ADMIN` |
| `PUT` | `/api/productos/{publicId}` | Modificar producto existente | `ADMIN` |
| `DELETE` | `/api/productos/{publicId}` | Baja lógica (valida que no tenga pedidos pendientes/listos) | `ADMIN` |
| `PATCH` | `/api/productos/{publicId}/reactivar` | Reactivar un producto inactivo | `ADMIN` |

### Pedidos y Reportes (`/api/pedidos`)
| Método | Endpoint | Descripción | Rol |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/pedidos` | Listar pedidos activos (o historial con `?historico=true`) | `ADMIN` + `COCINA` |
| `GET` | `/api/pedidos/{publicId}` | Buscar pedido por ID público | `ADMIN` + `COCINA` |
| `GET` | `/api/pedidos/estado/{estado}` | Filtrar pedidos por estado | `ADMIN` + `COCINA` |
| `GET` | `/api/pedidos/fecha?fecha=YYYY-MM-DD` | Filtrar pedidos por fecha de entrega | `ADMIN` + `COCINA` |
| `GET` | `/api/pedidos/buscar-por-nombre?nombre=` | Buscar pedidos por nombre de cliente | `ADMIN` + `COCINA` |
| `GET` | `/api/pedidos/cocina` | Vista de cocina (activos o filtrados por `?fecha=YYYY-MM-DD`) | `ADMIN` + `COCINA` |
| `GET` | `/api/pedidos/cocina/hoy` | Pedidos del día para cocina ordenados por horario | `ADMIN` + `COCINA` |
| `GET` | `/api/pedidos/reportes/mensual?anio=&mes=` | Reporte mensual de facturación, señas, saldos y productos más vendidos | `ADMIN` |
| `POST` | `/api/pedidos` | Crear pedido con múltiples ítems y seña opcional | `ADMIN` |
| `PATCH` | `/api/pedidos/{publicId}/sena` | Actualizar monto de seña abonada | `ADMIN` |
| `PATCH` | `/api/pedidos/{publicId}/avanzar` | Avanzar al siguiente estado (`PENDIENTE` → `LISTO` → `ENTREGADO`) | `ADMIN` + `COCINA` |
| `PATCH` | `/api/pedidos/{publicId}/estado?nuevoEstado=` | Cambiar estado manualmente | `ADMIN` + `COCINA` |
| `PATCH` | `/api/pedidos/{publicId}/cancelar` | Cancelar pedido | `ADMIN` |
| `PATCH` | `/api/pedidos/{publicId}/no-retiro` | Marcar como no retirado (desde `LISTO`) | `ADMIN` |
| `DELETE` | `/api/pedidos/{publicId}` | Eliminar pedido | `ADMIN` |

---

## 🧪 Tests Unitarios

Tests unitarios implementados con **JUnit 5** y **Mockito** siguiendo el patrón **AAA (Arrange, Act, Assert)**:

| Clase de Test | Cobertura de Escenarios |
| :--- | :--- |
| `ProductoServiceTest` | Listar activos, filtrar por tipo, buscar por ID, crear, validar duplicados, actualizar, baja lógica validando pedidos activos y reactivación. |
| `PedidoServiceTest` | Búsqueda por ID/estado/fecha/cocina, transiciones válidas e inválidas de estado (`avanzar`, `cancelar`, `marcarNoRetiro`, `cambiarEstado`), validación de mínimo 12 unidades en productos `MINI` y cálculo de tortas grandes por kilo (`1.5 kg` a `5.0 kg`). |
| `GestionApplicationTests` | Levantamiento integral del contexto de Spring Boot con base de datos en memoria H2. |

```bash
./mvnw test
```

---

## 🚀 Cómo correr el proyecto localmente

### Opción 1: Con Docker Compose (Recomendado)
Levanta automáticamente la base de datos **PostgreSQL 16** y el **Backend Spring Boot**:

```bash
docker compose up --build
```

### Opción 2: Con Maven y PostgreSQL local
1. Asegurate de tener PostgreSQL corriendo con una base de datos llamada `gestion_pedidos` (o configurá las variables de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`).
2. Ejecutá el proyecto:
```bash
./mvnw spring-boot:run
```

### Usuarios iniciales y Documentación Swagger
Al iniciar la aplicación se crean automáticamente el catálogo de productos y los usuarios iniciales (`admin` / `admin123` y `cocina` / `cocina123`).

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html` (Podés autenticarte pegando el token devuelto por `/api/auth/login` en el botón **Authorize**).
