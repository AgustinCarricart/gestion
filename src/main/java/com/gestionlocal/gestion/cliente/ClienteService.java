package com.gestionlocal.gestion.cliente;

import com.gestionlocal.gestion.cliente.dto.ClienteRequestDto;
import com.gestionlocal.gestion.cliente.exception.ClienteAlreadyExists;
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

    public List<ClienteEntity> getAllClientes()
    {
        List<ClienteEntity>clientes = clienteRepository.findAll()
                .stream()
                .toList();
        return clientes;
    }

    public ClienteEntity getByPublicId(UUID publicId)
    {
        ClienteEntity cliente = clienteRepository.findByPublicId(publicId)
                .orElseThrow(()-> new ClienteNotFoundException("No se encontro al cliente con ese id"));

        return cliente;
    }

    @Transactional
    public ClienteEntity updateEntity(UUID publicId,ClienteRequestDto clienteRequestDto)
    {
        ClienteEntity cliente = clienteRepository.findByPublicId(publicId)
                .orElseThrow(()-> new ClienteNotFoundException("No se encontro cliente con ese Id"));

        clienteMapper.updateEntity(clienteRequestDto,cliente);
        return cliente;

    }

    @Transactional
    public ClienteEntity createCliente(ClienteRequestDto clienteRequestDto) {
        if (clienteRepository.existsByNumero(clienteRequestDto.getNumero())) {
            throw new ClienteAlreadyExists("Ya se creo un cliente con ese numero");
        }

        ClienteEntity cliente = clienteMapper.toEntity(clienteRequestDto);
        cliente.setActivo(true);
        clienteRepository.save(cliente);

        return cliente;
    }

    @Transactional
    public ClienteEntity deleteCliente(UUID publicId)
    {
        ClienteEntity cliente = clienteRepository.findByPublicId(publicId)
                .orElseThrow(()-> new ClienteNotFoundException("No se encontro cliente con ese Id"));
        /*if(cliente.tiene pedidos pendientes)
        {
            que no se pueda borrar
        }*/ //No genere todavia infraestructura de pedidos
        cliente.setActivo(false);
        return cliente;
    }



}
