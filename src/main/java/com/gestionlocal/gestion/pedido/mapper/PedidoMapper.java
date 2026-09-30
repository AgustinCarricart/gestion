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
    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "publicId",         ignore = true)
    @Mapping(target = "fechaCreacion",    ignore = true)
    @Mapping(target = "estadoPedido",     ignore = true)
    @Mapping(target = "total",            ignore = true)
    @Mapping(target = "detalles",         ignore = true)
        // Los campos nombreCliente y telefonoCliente se mapean automáticamente por tener el mismo nombre en DTO y Entity
    PedidoEntity toEntity(PedidoDtoRequest pedidoDtoRequest);

    @Mapping(target = "estado",          source = "estadoPedido")
    // Mapeamos los campos directos del cliente desde la entidad al DTO de respuesta
    @Mapping(target = "nombreCliente",   source = "nombreCliente")
    @Mapping(target = "telefonoCliente", source = "telefonoCliente")
    @Mapping(target = "detalles",        source = "detalles")
    PedidoDtoResponse toDto(PedidoEntity pedidoEntity);

    @Mapping(target = "productoPublicId", source = "producto.publicId")
    @Mapping(target = "productoNombre",   source = "producto.nombreProducto")
    @Mapping(target = "tipoProducto",     source = "producto.tipoProducto")
    DetallePedidoResponseDto detalleToDto(DetallePedidosEntity detalle);
}