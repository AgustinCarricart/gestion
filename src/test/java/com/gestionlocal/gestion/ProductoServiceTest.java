package com.gestionlocal.gestion;

import com.gestionlocal.gestion.pedido.EstadoPedido;
import com.gestionlocal.gestion.pedido.PedidoRepository;
import com.gestionlocal.gestion.producto.ProductoEntity;
import com.gestionlocal.gestion.producto.ProductoRepository;
import com.gestionlocal.gestion.producto.ProductoService;
import com.gestionlocal.gestion.producto.TipoProducto;
import com.gestionlocal.gestion.producto.dto.ProductoRequestDto;
import com.gestionlocal.gestion.producto.dto.ProductoResponseDto;
import com.gestionlocal.gestion.producto.exception.ProductoAlreadyExists;
import com.gestionlocal.gestion.producto.exception.ProductoConPedidosActivosException;
import com.gestionlocal.gestion.producto.exception.ProductoInactivoException;
import com.gestionlocal.gestion.producto.exception.ProductoNotFoundException;
import com.gestionlocal.gestion.producto.mapper.ProductoMapper;
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
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoMapper productoMapper;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private ProductoService productoService;

    private ProductoEntity productoActivo;
    private ProductoEntity productoInactivo;
    private ProductoRequestDto requestDto;
    private ProductoResponseDto responseDto;
    private UUID publicId;

    @BeforeEach
    void setUp() {
        publicId = UUID.randomUUID();

        productoActivo = new ProductoEntity();
        productoActivo.setPublicId(publicId);
        productoActivo.setNombreProducto("Torta Sacher");
        productoActivo.setActivo(true);

        productoInactivo = new ProductoEntity();
        productoInactivo.setPublicId(UUID.randomUUID());
        productoInactivo.setNombreProducto("Tartaleta de Frutilla");
        productoInactivo.setActivo(false);

        requestDto = new ProductoRequestDto();
        requestDto.setNombre("Torta Sacher");

        responseDto = new ProductoResponseDto();
    }

    @Nested
    @DisplayName("Pruebas de Lectura y Consultas")
    class LecturaTests {

        @Test
        @DisplayName("getAllProductos - Debe retornar solo los productos activos")
        void getAllProductos_Exito() {
            // Arrange
            when(productoRepository.findAll()).thenReturn(List.of(productoActivo, productoInactivo));
            when(productoMapper.toDto(productoActivo)).thenReturn(responseDto);

            // Act
            List<ProductoResponseDto> resultado = productoService.getAllProductos();

            // Assert
            assertEquals(1, resultado.size());
            verify(productoRepository, times(1)).findAll();
            verify(productoMapper, times(1)).toDto(productoActivo);
            verify(productoMapper, never()).toDto(productoInactivo);
        }

        @Test
        @DisplayName("getProductoByPublicId - Debe retornar el DTO cuando el producto existe y está activo")
        void getProductoByPublicId_Exito() {
            // Arrange
            when(productoRepository.findByPublicId(publicId)).thenReturn(Optional.of(productoActivo));
            when(productoMapper.toDto(productoActivo)).thenReturn(responseDto);

            // Act
            ProductoResponseDto resultado = productoService.getProductoByPublicId(publicId);

            // Assert
            assertNotNull(resultado);
            verify(productoRepository, times(1)).findByPublicId(publicId);
        }

        @Test
        @DisplayName("getProductoByPublicId - Debe lanzar ProductoNotFoundException si el id no existe")
        void getProductoByPublicId_NotFound() {
            // Arrange
            when(productoRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProductoNotFoundException.class, () -> productoService.getProductoByPublicId(publicId));
        }

        @Test
        @DisplayName("getProductoByPublicId - Debe lanzar ProductoInactivoException si el producto está inactivo")
        void getProductoByPublicId_Inactivo() {
            // Arrange
            when(productoRepository.findByPublicId(publicId)).thenReturn(Optional.of(productoInactivo));

            // Act & Assert
            assertThrows(ProductoInactivoException.class, () -> productoService.getProductoByPublicId(publicId));
        }

        @Test
        @DisplayName("getProductosByTipo - Debe filtrar por tipo y retornar solo los productos activos")
        void getProductosByTipo_Exito() {
            // Arrange
            TipoProducto tipo = TipoProducto.TORTA_COMUN;
            when(productoRepository.findByTipoProducto(tipo)).thenReturn(List.of(productoActivo, productoInactivo));
            when(productoMapper.toDto(productoActivo)).thenReturn(responseDto);

            // Act
            List<ProductoResponseDto> resultado = productoService.getProductosByTipo(tipo);

            // Assert
            assertEquals(1, resultado.size());
            verify(productoRepository, times(1)).findByTipoProducto(tipo);
            verify(productoMapper, times(1)).toDto(productoActivo);
        }
    }

    @Nested
    @DisplayName("Pruebas de Creación y Edición")
    class EscrituraTests {

        @Test
        @DisplayName("createProducto - Debe crear un producto correctamente y marcarlo como activo")
        void createProducto_Exito() {
            // Arrange
            when(productoRepository.existsByNombreProducto(requestDto.getNombre())).thenReturn(false);
            when(productoMapper.toEntity(requestDto)).thenReturn(productoActivo);
            when(productoRepository.save(productoActivo)).thenReturn(productoActivo);
            when(productoMapper.toDto(productoActivo)).thenReturn(responseDto);

            // Act
            ProductoResponseDto resultado = productoService.createProducto(requestDto);

            // Assert
            assertNotNull(resultado);
            assertTrue(productoActivo.isActivo());
            verify(productoRepository, times(1)).save(productoActivo);
        }

        @Test
        @DisplayName("createProducto - Debe lanzar ProductoAlreadyExists si el nombre ya está registrado")
        void createProducto_NombreDuplicado() {
            // Arrange
            when(productoRepository.existsByNombreProducto(requestDto.getNombre())).thenReturn(true);

            // Act & Assert
            assertThrows(ProductoAlreadyExists.class, () -> productoService.createProducto(requestDto));
            verify(productoRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateProducto - Debe actualizar el producto si está activo")
        void updateProducto_Exito() {
            // Arrange
            when(productoRepository.findByPublicId(publicId)).thenReturn(Optional.of(productoActivo));
            when(productoMapper.toDto(productoActivo)).thenReturn(responseDto);

            // Act
            ProductoResponseDto resultado = productoService.updateProducto(publicId, requestDto);

            // Assert
            assertNotNull(resultado);
            verify(productoMapper, times(1)).updateEntity(requestDto, productoActivo);
        }

        @Test
        @DisplayName("updateProducto - Debe lanzar ProductoInactivoException si se intenta actualizar un producto inactivo")
        void updateProducto_Inactivo() {
            // Arrange
            when(productoRepository.findByPublicId(publicId)).thenReturn(Optional.of(productoInactivo));

            // Act & Assert
            assertThrows(ProductoInactivoException.class, () -> productoService.updateProducto(publicId, requestDto));
            verify(productoMapper, never()).updateEntity(any(), any());
        }
    }

    @Nested
    @DisplayName("Pruebas de Borrado Lógico y Reactivación")
    class EstadoProductoTests {

        @Test
        @DisplayName("deleteProducto - Debe desactivar el producto si no tiene pedidos pendientes ni listos")
        void deleteProducto_Exito() {
            // Arrange
            when(productoRepository.findByPublicId(publicId)).thenReturn(Optional.of(productoActivo));
            when(pedidoRepository.existsByDetalles_Producto_PublicIdAndEstadoPedidoIn(
                    eq(publicId),
                    eq(List.of(EstadoPedido.PENDIENTE, EstadoPedido.LISTO))
            )).thenReturn(false);

            // Act
            productoService.deleteProducto(publicId);

            // Assert
            assertFalse(productoActivo.isActivo());
        }

        @Test
        @DisplayName("deleteProducto - Debe lanzar ProductoConPedidosActivosException si hay pedidos pendientes/listos")
        void deleteProducto_ConPedidosPendientes() {
            // Arrange
            when(productoRepository.findByPublicId(publicId)).thenReturn(Optional.of(productoActivo));
            when(pedidoRepository.existsByDetalles_Producto_PublicIdAndEstadoPedidoIn(
                    eq(publicId),
                    eq(List.of(EstadoPedido.PENDIENTE, EstadoPedido.LISTO))
            )).thenReturn(true);

            // Act & Assert
            assertThrows(ProductoConPedidosActivosException.class, () -> productoService.deleteProducto(publicId));
            assertTrue(productoActivo.isActivo()); // Verifica que NO se haya cambiado a false
        }

        @Test
        @DisplayName("reactivarProducto - Debe marcar como activo un producto inactivo")
        void reactivarProducto_Exito() {
            // Arrange
            when(productoRepository.findByPublicId(publicId)).thenReturn(Optional.of(productoInactivo));
            when(productoMapper.toDto(productoInactivo)).thenReturn(responseDto);

            // Act
            ProductoResponseDto resultado = productoService.reactivarProducto(publicId);

            // Assert
            assertNotNull(resultado);
            assertTrue(productoInactivo.isActivo());
        }

        @Test
        @DisplayName("reactivarProducto - Debe lanzar IllegalArgumentException si el producto ya está activo")
        void reactivarProducto_YaActivo() {
            // Arrange
            when(productoRepository.findByPublicId(publicId)).thenReturn(Optional.of(productoActivo));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> productoService.reactivarProducto(publicId));
        }
    }
}