package com.gestionlocal.gestion.producto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "producto")
public class ProductoEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id",nullable = false,updatable = false,unique = true)
    private UUID publicId;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(name = "categoria",nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoProducto tipoProducto;

    @Column(name = "precio")
    private Double precio;

    @Column(name = "peso_kg")
    private Double peso;

    @Column(name = "precio_docena")
    private Double precioDocena;

    @Column(name = "activo")
    private boolean activo;

    @PrePersist
    private void generateValue()
    {
        if(this.publicId == null)
        {
            this.publicId = UUID.randomUUID();
        }
    }
}
