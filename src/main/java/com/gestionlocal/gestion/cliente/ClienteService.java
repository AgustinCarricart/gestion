package com.gestionlocal.gestion.cliente;

import com.gestionlocal.gestion.cliente.dto.ClienteRequestDto;
import com.gestionlocal.gestion.cliente.dto.ClienteResponseDto;
import com.gestionlocal.gestion.cliente.exception.ClienteAlreadyExists;
import com.gestionlocal.gestion.cliente.exception.ClienteConPedidosActivosException;
import com.gestionlocal.gestion.cliente.exception.ClienteInactivoException;
import com.gestionlocal.gestion.cliente.exception.ClienteNotFoundException;
import com.gestionlocal.gestion.cliente.mapper.ClienteMapper;
import com.gestionlocal.gestion.pedido.EstadoPedido;
import com.gestionlocal.gestion.pedido.PedidoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClienteService
{
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final PedidoRepository pedidoRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          ClienteMapper clienteMapper,
                          PedidoRepository pedidoRepository)
    {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.pedidoRepository = pedidoRepository;
    }

    public List<ClienteResponseDto> getAllClientes()
    {
        return clienteRepository.findAll()
                .stream()
                .map(clienteMapper::toResponseDTO)
                .toList();
    }

    public List<ClienteResponseDto> getAllClientesActivos()
    {
        return clienteRepository.findAll()
                .stream()
                .filter(ClienteEntity::isActivo)
                .map(clienteMapper::toResponseDTO)
                .toList();
    }
    public ClienteResponseDto getClienteByPublicId(UUID publicId)
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
    public ClienteResponseDto updateCliente(UUID publicId,ClienteRequestDto clienteRequestDto)
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
                .orElseThrow(() -> new ClienteNotFoundException("No se encontro cliente con ese Id"));

        boolean tienePedidosActivos = pedidoRepository.existsByCliente_PublicIdAndEstadoPedidoIn(
                publicId,
                List.of(EstadoPedido.PENDIENTE, EstadoPedido.LISTO)
        );

        if (tienePedidosActivos)
        {
            throw new ClienteConPedidosActivosException("No se puede eliminar un cliente con pedidos pendientes o listos");
        }

        cliente.setActivo(false);
        clienteRepository.save(cliente);

    }
    @Transactional
    public ClienteResponseDto reactivarCliente(UUID publicId)
    {
        ClienteEntity cliente = clienteRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró cliente con ese id"));

        if (cliente.isActivo())
        {
            throw new IllegalArgumentException("El cliente ya está activo");
        }

        cliente.setActivo(true);
        return clienteMapper.toResponseDTO(cliente);
    }

    public List<ClienteResponseDto> buscarClientes(String nombre, String apellido)
    {
        return clienteRepository.findAll()
                .stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombre) &&
                        (apellido == null || c.getApellido().equalsIgnoreCase(apellido)))
                .map(clienteMapper::toResponseDTO)
                .toList();
    }


}
