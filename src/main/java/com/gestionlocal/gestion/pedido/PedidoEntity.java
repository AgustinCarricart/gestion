package com.gestionlocal.gestion.pedido;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pedidos", indexes = {
    @Index(name = "idx_pedidos_public_id", columnList = "public_id", unique = true),
    @Index(name = "idx_pedidos_entrega_estado", columnList = "fecha_entrega, estado_pedido"),
    @Index(name = "idx_pedidos_nombre_cliente", columnList = "nombre_cliente")
})
public class PedidoEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    // Nuevos campos de texto plano directos en el pedido (en lugar de la relación con ClienteEntity)
    @Column(name = "nombre_cliente", nullable = false)
    private String nombreCliente;

    @Column(name = "telefono_cliente")
    private String telefonoCliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedidosEntity> detalles;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pedido", nullable = false)
    private EstadoPedido estadoPedido;

    @Column(name = "total", nullable = false)
    private Double total;

    @Column(name = "sena")
    private Double sena;

    @Column(name = "observaciones")
    private String observaciones;

    @PrePersist
    private void prePersist()
    {
        if (this.publicId == null)
        {
            this.publicId = UUID.randomUUID();
        }
        this.fechaCreacion = LocalDateTime.now();
        if (this.estadoPedido == null)
        {
            this.estadoPedido = EstadoPedido.PENDIENTE;
        }
        if (this.sena == null)
        {
            this.sena = 0.0;
        }
    }
}