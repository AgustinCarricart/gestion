package com.gestionlocal.gestion.cliente.mapper;

import com.gestionlocal.gestion.cliente.ClienteEntity;
import com.gestionlocal.gestion.cliente.dto.ClienteRequestDto;
import com.gestionlocal.gestion.cliente.dto.ClienteResponseDto;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface ClienteMapper
{
    ClienteEntity toEntity(ClienteRequestDto dto);

    ClienteResponseDto toResponseDTO(ClienteEntity entity);

}
