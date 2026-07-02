package com.gestionlocal.gestion.cliente;

import com.gestionlocal.gestion.cliente.dto.ClienteRequestDto;
import com.gestionlocal.gestion.cliente.dto.ClienteResponseDto;
import com.gestionlocal.gestion.cliente.exception.ClienteAlreadyExists;
import com.gestionlocal.gestion.cliente.exception.ClienteInactivoException;
import com.gestionlocal.gestion.cliente.exception.ClienteNotFoundException;
import com.gestionlocal.gestion.cliente.mapper.ClienteMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClienteService
{
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    public List<ClienteResponseDto> getAllClientes()
    {
        List<ClienteResponseDto>clientes = clienteRepository.findAll()
                .stream()
                .filter(c -> c.isActivo())
                .map(clienteMapper :: toResponseDTO)
                .toList();

        return clientes;
    }

    public ClienteResponseDto getByPublicId(UUID publicId)
    {
        ClienteEntity cliente = clienteRepository.findByPublicId(publicId)
                .orElseThrow(()-> new ClienteNotFoundException("No se encontro al cliente con ese id"));

        if(!cliente.isActivo())
        {
            throw new ClienteInactivoException("El cliente esta inactivo");
        }

        ClienteResponseDto clienteResponseDto = clienteMapper.toResponseDTO(cliente);
        return clienteResponseDto;
    }

    @Transactional
    public ClienteResponseDto updateEntity(UUID publicId,ClienteRequestDto clienteRequestDto)
    {
        ClienteEntity cliente = clienteRepository.findByPublicId(publicId)
                .orElseThrow(()-> new ClienteNotFoundException("No se encontro cliente con ese Id"));

        clienteMapper.updateEntity(clienteRequestDto,cliente);

        return clienteMapper.toResponseDTO(cliente);

    }

    @Transactional
    public ClienteResponseDto createCliente(ClienteRequestDto clienteRequestDto) {
        if (clienteRepository.existsByNumero(clienteRequestDto.getNumero())) {
            throw new ClienteAlreadyExists("Ya se creo un cliente con ese numero");
        }

        ClienteEntity cliente = clienteMapper.toEntity(clienteRequestDto);
        cliente.setActivo(true);
        clienteRepository.save(cliente);

        return clienteMapper.toResponseDTO(cliente) ;
    }

    @Transactional
    public void deleteCliente(UUID publicId)
    {
        ClienteEntity cliente = clienteRepository.findByPublicId(publicId)
                .orElseThrow(()-> new ClienteNotFoundException("No se encontro cliente con ese Id"));
        /*if(cliente.tiene pedidos pendientes)
        {
            que no se pueda borrar
        }*/ //No genere todavia infraestructura de pedidos
        cliente.setActivo(false);
    }



}
