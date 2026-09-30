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
@Table(name = "pedidos")
public class PedidoEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false)
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
    }
}