package com.gestionlocal.gestion.producto.mapper;

import com.gestionlocal.gestion.producto.ProductoEntity;
import com.gestionlocal.gestion.producto.dto.ProductoRequestDto;
import com.gestionlocal.gestion.producto.dto.ProductoResponseDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductoMapper
{
    ProductoResponseDto toDto(ProductoEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "tipoProducto", source = "tipo")
    ProductoEntity toEntity(ProductoRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "tipoProducto", source = "tipo")
    void updateEntity(ProductoRequestDto dto, @MappingTarget ProductoEntity entity);


}
