package com.gestionlocal.gestion.pedido;


import com.gestionlocal.gestion.cliente.ClienteEntity;
import com.gestionlocal.gestion.producto.ProductoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

   @Column(name = "public_id",nullable = false)
    private UUID publicId;

   @Column(name = "estado_pedido",nullable = false)
    private EstadoPedido estadoPedido;

    @JoinColumn(name = "producto_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductoEntity productoEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedidosEntity> detalles;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPedido estado;

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
        this.estado = EstadoPedido.PENDIENTE;
    }


}
