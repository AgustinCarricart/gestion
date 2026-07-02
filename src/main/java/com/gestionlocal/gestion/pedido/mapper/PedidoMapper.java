package com.gestionlocal.gestion.pedido.mapper;

import com.gestionlocal.gestion.pedido.DetallePedidosEntity;
import com.gestionlocal.gestion.pedido.PedidoEntity;
import com.gestionlocal.gestion.pedido.dto.DetallePedidoResponseDto;
import com.gestionlocal.gestion.pedido.dto.PedidoDtoRequest;
import com.gestionlocal.gestion.pedido.dto.PedidoDtoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;



@Mapper(componentModel = "spring")
public interface PedidoMapper
{
    @Mapping(target = "publicId",        ignore = true)
    @Mapping(target = "fechaCreacion",   ignore = true)
    @Mapping(target = "estado",          ignore = true)
    @Mapping(target = "total",           ignore = true)
    @Mapping(target = "detalles",        ignore = true)
    @Mapping(target = "cliente",         ignore = true)
    PedidoEntity toEntity(PedidoDtoRequest pedidoDtoRequest);

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "clientePublicId", source = "cliente.publicId")
    @Mapping(target = "clienteNombre",   source = "cliente.nombre")
    @Mapping(target = "clienteApellido", source = "cliente.apellido")
    @Mapping(target = "detalles",        source = "detalles")
    PedidoDtoResponse toDto(PedidoEntity pedidoEntity);

    @Mapping(target = "productoPublicId", source = "producto.publicId")
    @Mapping(target = "productoNombre",   source = "producto.nombre")
    @Mapping(target = "tipoProducto",     source = "producto.tipoProducto")
    DetallePedidoResponseDto detalleToDto(DetallePedidosEntity detalle);
}
