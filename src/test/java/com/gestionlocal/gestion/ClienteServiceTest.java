package com.gestionlocal.gestion;

import com.gestionlocal.gestion.cliente.ClienteEntity;
import com.gestionlocal.gestion.cliente.ClienteRepository;
import com.gestionlocal.gestion.cliente.ClienteService;
import com.gestionlocal.gestion.cliente.dto.ClienteRequestDto;
import com.gestionlocal.gestion.cliente.dto.ClienteResponseDto;
import com.gestionlocal.gestion.cliente.exception.ClienteAlreadyExists;
import com.gestionlocal.gestion.cliente.exception.ClienteConPedidosActivosException;
import com.gestionlocal.gestion.cliente.exception.ClienteInactivoException;
import com.gestionlocal.gestion.cliente.exception.ClienteNotFoundException;
import com.gestionlocal.gestion.cliente.mapper.ClienteMapper;
import com.gestionlocal.gestion.pedido.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteEntity clienteActivo;
    private ClienteEntity clienteInactivo;
    private ClienteRequestDto requestDto;
    private ClienteResponseDto responseDto;
    private UUID publicId;

    @BeforeEach
    void setUp() {
        publicId = UUID.randomUUID();

        clienteActivo = new ClienteEntity();
        clienteActivo.setPublicId(publicId);
        clienteActivo.setNombre("Agustin");
        clienteActivo.setApellido("Carricart");
        clienteActivo.setActivo(true);

        clienteInactivo = new ClienteEntity();
        clienteInactivo.setPublicId(UUID.randomUUID());
        clienteInactivo.setNombre("Juan");
        clienteInactivo.setApellido("Perez");
        clienteInactivo.setActivo(false);

        requestDto = new ClienteRequestDto();
        requestDto.setNumero("123456789");

        responseDto = new ClienteResponseDto();
    }

    @Nested
    @DisplayName("Pruebas de Lectura (Getters)")
    class LecturaTests {

        @Test
        @DisplayName("getAllClientes - Debe retornar la lista completa de clientes DTO")
        void getAllClientes_Exito() {
            // Arrange
            when(clienteRepository.findAll()).thenReturn(List.of(clienteActivo, clienteInactivo));
            when(clienteMapper.toResponseDTO(any(ClienteEntity.class))).thenReturn(responseDto);

            // Act
            List<ClienteResponseDto> resultado = clienteService.getAllClientes();

            // Assert
            assertEquals(2, resultado.size());
            verify(clienteRepository, times(1)).findAll();
            verify(clienteMapper, times(2)).toResponseDTO(any(ClienteEntity.class));
        }

        @Test
        @DisplayName("getAllClientesActivos - Debe filtrar y retornar solo los clientes activos")
        void getAllClientesActivos_Exito() {
            // Arrange
            when(clienteRepository.findAll()).thenReturn(List.of(clienteActivo, clienteInactivo));
            when(clienteMapper.toResponseDTO(clienteActivo)).thenReturn(responseDto);

            // Act
            List<ClienteResponseDto> resultado = clienteService.getAllClientesActivos();

            // Assert
            assertEquals(1, resultado.size());
            verify(clienteMapper, times(1)).toResponseDTO(clienteActivo);
            verify(clienteMapper, never()).toResponseDTO(clienteInactivo);
        }

        @Test
        @DisplayName("getClienteByPublicId - Debe retornar el DTO cuando existe y está activo")
        void getClienteByPublicId_Exito() {
            // Arrange
            when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteActivo));
            when(clienteMapper.toResponseDTO(clienteActivo)).thenReturn(responseDto);

            // Act
            ClienteResponseDto resultado = clienteService.getClienteByPublicId(publicId);

            // Assert
            assertNotNull(resultado);
            verify(clienteRepository, times(1)).findByPublicId(publicId);
        }

        @Test
        @DisplayName("getClienteByPublicId - Debe lanzar ClienteNotFoundException si no existe")
        void getClienteByPublicId_NotFound() {
            // Arrange
            when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ClienteNotFoundException.class, () -> clienteService.getClienteByPublicId(publicId));
        }

        @Test
        @DisplayName("getClienteByPublicId - Debe lanzar ClienteInactivoException si el cliente no está activo")
        void getClienteByPublicId_ClienteInactivo() {
            // Arrange
            when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteInactivo));

            // Act & Assert
            assertThrows(ClienteInactivoException.class, () -> clienteService.getClienteByPublicId(publicId));
        }
    }

    @Nested
    @DisplayName("Pruebas de Creación y Edición")
    class EscrituraTests {

        @Test
        @DisplayName("createCliente - Debe crear un cliente exitosamente y marcarlo como activo")
        void createCliente_Exito() {
            // Arrange
            when(clienteRepository.existsByNumero(requestDto.getNumero())).thenReturn(false);
            when(clienteMapper.toEntity(requestDto)).thenReturn(clienteActivo);
            when(clienteRepository.save(clienteActivo)).thenReturn(clienteActivo);
            when(clienteMapper.toResponseDTO(clienteActivo)).thenReturn(responseDto);

            // Act
            ClienteResponseDto resultado = clienteService.createCliente(requestDto);

            // Assert
            assertNotNull(resultado);
            assertTrue(clienteActivo.isActivo());
            verify(clienteRepository, times(1)).save(clienteActivo);
        }

        @Test
        @DisplayName("createCliente - Debe lanzar ClienteAlreadyExists si ya existe el número")
        void createCliente_NumeroDuplicado() {
            // Arrange
            when(clienteRepository.existsByNumero(requestDto.getNumero())).thenReturn(true);

            // Act & Assert
            assertThrows(ClienteAlreadyExists.class, () -> clienteService.createCliente(requestDto));
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateCliente - Debe actualizar y retornar el cliente")
        void updateCliente_Exito() {
            // Arrange
            when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteActivo));
            when(clienteMapper.toResponseDTO(clienteActivo)).thenReturn(responseDto);

            // Act
            ClienteResponseDto resultado = clienteService.updateCliente(publicId, requestDto);

            // Assert
            assertNotNull(resultado);
            verify(clienteMapper, times(1)).updateEntity(requestDto, clienteActivo);
        }
    }

    @Nested
    @DisplayName("Pruebas de Baja Lógica y Reactivación")
    class EstadoClienteTests {

        @Test
        @DisplayName("deleteCliente - Debe desactivar el cliente si no tiene pedidos pendientes ni listos")
        void deleteCliente_Exito() {
            // Arrange
            when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteActivo));
            when(pedidoRepository.existsByCliente_PublicIdAndEstadoPedidoIn(eq(publicId), anyList())).thenReturn(false);

            // Act
            clienteService.deleteCliente(publicId);

            // Assert
            assertFalse(clienteActivo.isActivo());
            verify(clienteRepository, times(1)).save(clienteActivo);
        }

        @Test
        @DisplayName("deleteCliente - Debe lanzar ClienteConPedidosActivosException si tiene pedidos PENDIENTE/LISTO")
        void deleteCliente_ConPedidosPendientes() {
            // Arrange
            when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteActivo));
            when(pedidoRepository.existsByCliente_PublicIdAndEstadoPedidoIn(eq(publicId), anyList())).thenReturn(true);

            // Act & Assert
            assertThrows(ClienteConPedidosActivosException.class, () -> clienteService.deleteCliente(publicId));
            verify(clienteRepository, never()).save(clienteActivo);
        }

        @Test
        @DisplayName("reactivarCliente - Debe activar un cliente inactivo")
        void reactivarCliente_Exito() {
            // Arrange
            when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteInactivo));
            when(clienteMapper.toResponseDTO(clienteInactivo)).thenReturn(responseDto);

            // Act
            ClienteResponseDto resultado = clienteService.reactivarCliente(publicId);

            // Assert
            assertNotNull(resultado);
            assertTrue(clienteInactivo.isActivo());
        }

        @Test
        @DisplayName("reactivarCliente - Debe lanzar IllegalArgumentException si ya está activo")
        void reactivarCliente_YaActivo() {
            // Arrange
            when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteActivo));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> clienteService.reactivarCliente(publicId));
        }
    }

    @Nested
    @DisplayName("Pruebas de Búsqueda Avanzada")
    class BusquedaTests {

        @Test
        @DisplayName("buscarClientes - Debe filtrar por nombre y apellido correctamente")
        void buscarClientes_ConNombreYApellido() {
            // Arrange
            when(clienteRepository.findAll()).thenReturn(List.of(clienteActivo));
            when(clienteMapper.toResponseDTO(clienteActivo)).thenReturn(responseDto);

            // Act
            List<ClienteResponseDto> resultado = clienteService.buscarClientes("Agustin", "Carricart");

            // Assert
            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("buscarClientes - Debe filtrar solo por nombre si el apellido es null")
        void buscarClientes_SoloNombre() {
            // Arrange
            when(clienteRepository.findAll()).thenReturn(List.of(clienteActivo));
            when(clienteMapper.toResponseDTO(clienteActivo)).thenReturn(responseDto);

            // Act
            List<ClienteResponseDto> resultado = clienteService.buscarClientes("Agustin", null);

            // Assert
            assertEquals(1, resultado.size());
        }
    }
}