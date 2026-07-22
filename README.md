# Gestión Local 🏪

Sistema de gestión de pedidos para locales de comida. Desarrollado con Spring Boot, pensado inicialmente para una repostería pero diseñado de forma genérica para poder adaptarse a otros negocios.

---

## Tecnologías

- Java 17
- Spring Boot 3.4.5
- Spring Data JPA / Hibernate
- Spring Security (JWT — en progreso)
- PostgreSQL
- MapStruct 1.5.5
- Lombok
- Maven

---

## Arquitectura

El proyecto sigue arquitectura en capas estricta:

```
Controller → Service → Repository → Base de datos
```

Cada módulo tiene su propia carpeta con todas sus clases:

```
com.gestionlocal.gestion
├── cliente
│   ├── dto
│   │   ├── ClienteRequestDTO
│   │   └── ClienteResponseDTO
│   ├── mapper
│   │   └── ClienteMapper
│   ├── ClienteEntity
│   ├── ClienteRepository
│   ├── ClienteService
│   └── ClienteController
├── producto
│   ├── dto
│   │   ├── ProductoRequestDTO
│   │   └── ProductoResponseDTO
│   ├── mapper
│   │   └── ProductoMapper
│   ├── ProductoEntity
│   ├── ProductoRepository
│   ├── ProductoService
│   ├── ProductoController
│   └── TipoProducto (enum)
├── pedido
│   └── detalle
├── usuario
├── seguridad
├── exception
└── config
```

---

## Modelo de base de datos

### clientes
| Campo | Tipo | Descripción |
|---|---|---|
| id | BIGINT | ID interno, nunca se expone |
| public_id | UUID | ID público usado en la API |
| nombre | VARCHAR | Nombre del cliente |
| apellido | VARCHAR | Apellido del cliente |
| num_telefono | VARCHAR | Teléfono de contacto |

### productos
| Campo | Tipo | Descripción |
|---|---|---|
| id | BIGINT | ID interno |
| public_id | UUID | ID público usado en la API |
| nombre | VARCHAR | Nombre del producto |
| tipo | VARCHAR | Enum: TORTA_COMUN, TORTA_POR_KILO, TORTA_POR_KILO_FRUTA, MINI |
| precio_fijo | INTEGER | Solo para TORTA_COMUN |
| precio_por_kilo | INTEGER | Solo para TORTA_POR_KILO y TORTA_POR_KILO_FRUTA |
| precio_docena | INTEGER | Solo para MINI |
| activo | BOOLEAN | Baja lógica del producto |

### pedidos (en progreso)
| Campo | Tipo | Descripción |
|---|---|---|
| id | BIGINT | ID interno |
| public_id | UUID | ID público usado en la API |
| fecha | DATE | Fecha del pedido |
| estado | VARCHAR | Enum: PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO |
| id_cliente | BIGINT | FK a clientes |

### pedido_detalle (en progreso)
| Campo | Tipo | Descripción |
|---|---|---|
| id | BIGINT | ID interno |
| id_pedido | BIGINT | FK a pedidos |
| id_producto | BIGINT | FK a productos |
| cantidad | INTEGER | Para MINI (cantidad de docenas) |
| peso_kg | DOUBLE | Para TORTA_POR_KILO (kilos pedidos) |
| precio_unitario | INTEGER | Precio en el momento del pedido |
| subtotal | INTEGER | Total del ítem |

---

## Reglas de negocio

### Tipos de producto y cálculo de precio

| Tipo | Descripción | Cálculo |
|---|---|---|
| TORTA_COMUN | Torta estándar ~1.2kg, precio fijo | `precioFijo` |
| TORTA_POR_KILO | Torta grande de 1.5kg a 5kg | `precioPorKilo × pesoKg` |
| TORTA_POR_KILO_FRUTA | Igual que anterior pero con fruta, precio por kilo más caro | `precioPorKilo × pesoKg` |
| MINI | Se vende por docena, mínimo 12 unidades | `precioDocena × cantidad` |

### Pedidos
- Un pedido puede tener varios productos
- El precio se registra en el detalle al momento del pedido (no cambia si el producto cambia de precio después)
- El peso en kg se especifica en el detalle del pedido para las tortas por kilo
- Mínimo 12 unidades (1 docena) para minis

### Usuarios y roles (en progreso)
- **ADMIN** — puede crear, modificar y ver pedidos. Acceso a reportes
- **EMPLEADO** — puede ver pedidos y marcarlos como listos

---

## Endpoints planeados

### Clientes
| Método | Endpoint | Descripción |
|---|---|---|
| POST | /api/clientes | Crear cliente |
| GET | /api/clientes | Listar clientes |
| GET | /api/clientes/{publicId} | Buscar cliente |
| PUT | /api/clientes/{publicId} | Modificar cliente |
| DELETE | /api/clientes/{publicId} | Baja lógica |

### Productos
| Método | Endpoint | Descripción |
|---|---|---|
| POST | /api/productos | Crear producto |
| GET | /api/productos | Listar productos activos |
| GET | /api/productos/{publicId} | Buscar producto |
| PUT | /api/productos/{publicId} | Modificar producto |
| DELETE | /api/productos/{publicId} | Baja lógica |

### Pedidos (en progreso)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | /api/pedidos | Crear pedido |
| GET | /api/pedidos | Listar pedidos |
| GET | /api/pedidos/{publicId} | Ver pedido con detalle |
| POST | /api/pedidos/{publicId}/estado | Cambiar estado |

### Reportes (en progreso)
| Método | Endpoint | Descripción |
|---|---|---|
| GET | /api/reportes/ventas | Total vendido por período |
| GET | /api/reportes/productos | Productos más vendidos |

---

## Cómo correr el proyecto

### Requisitos
- Java 17
- PostgreSQL corriendo localmente
- Maven

### Configuración
Creá un archivo `application.properties` en `src/main/resources` con:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gestion_local
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Correr
```bash
./mvnw spring-boot:run
```

---

## Estado del proyecto

- [x] Estructura base del proyecto
- [x] Entidad y Repository de Cliente
- [x] Entidad y Repository de Producto
- [x] DTO´s de Cliente y Producto
- [x] Mappers de Cliente y Producto
- [x] Service y Controller de Cliente
- [x] Service y Controller de Producto
- [x] Entidad Pedido y PedidoDetalle
- [x] Lógica de cálculo de precios
- [x] Autenticación JWT
- [ ] Reportes de ventas
- [ ] Deploy en Railway
