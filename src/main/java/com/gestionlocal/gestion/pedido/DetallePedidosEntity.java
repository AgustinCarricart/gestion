package com.gestionlocal.gestion.pedido;

import com.gestionlocal.gestion.producto.ProductoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "detalle_pedido")
public class DetallePedidosEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private ProductoEntity producto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Column(name = "telefono_cliente")
    private String telefonoCliente;

    @Column(name = "fecha_pedido")
    private LocalDate fechaPedido;


    // Para TORTA_POR_KILO y TORTA_POR_KILO_FRUTA: peso en kg
    @Column(name = "peso_kg")
    private Double pesoKg;

    // Precio calculado al momento del pedido (puede ajustarse manualmente)
    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(name = "subtotal", nullable = false)
    private Double subtotal;

    @Column(name = "observaciones")
    private String observaciones;
}
