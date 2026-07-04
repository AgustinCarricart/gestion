package com.gestionlocal.gestion.producto;

import com.gestionlocal.gestion.pedido.EstadoPedido;
import com.gestionlocal.gestion.pedido.PedidoRepository;
import com.gestionlocal.gestion.producto.dto.ProductoRequestDto;
import com.gestionlocal.gestion.producto.dto.ProductoResponseDto;
import com.gestionlocal.gestion.producto.exception.ProductoAlreadyExists;
import com.gestionlocal.gestion.producto.exception.ProductoConPedidosActivosException;
import com.gestionlocal.gestion.producto.exception.ProductoInactivoException;
import com.gestionlocal.gestion.producto.exception.ProductoNotFoundException;
import com.gestionlocal.gestion.producto.mapper.ProductoMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductoService
{
    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final PedidoRepository pedidoRepository;

    public ProductoService(ProductoRepository productoRepository, ProductoMapper productoMapper, PedidoRepository pedidoRepository) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
        this.pedidoRepository = pedidoRepository;
    }

    public List<ProductoResponseDto> getAllProductos()
    {
        List<ProductoResponseDto> productos = productoRepository.findAll()
                .stream()
                .filter(c-> c.isActivo())
                .map(productoMapper:: toDto)
                .toList();
        return productos;
    }

   public ProductoResponseDto getProductoByPublicId(UUID publicId)
    {
        ProductoEntity producto = productoRepository.findByPublicId(publicId)
                .orElseThrow(()-> new ProductoNotFoundException("No se encontro porducto con ese id"));

        if(!producto.isActivo())
        {
            throw new ProductoInactivoException("El producto no esta activo");
        }
        return productoMapper.toDto(producto);
    }
    public List<ProductoResponseDto> getProductosByTipo(TipoProducto tipo)
    {
        return productoRepository.findByTipoProducto(tipo)
                .stream()
                .filter(ProductoEntity::isActivo)
                .map(productoMapper::toDto)
                .toList();
    }

    @Transactional
    public ProductoResponseDto createProducto(ProductoRequestDto productoRequestDto)
    {
        if(productoRepository.existsByNombreProducto(productoRequestDto.getNombre()))
        {
            throw new ProductoAlreadyExists("Ya existe un producto con ese nombre");
        }
        ProductoEntity producto = productoMapper.toEntity(productoRequestDto);
        producto.setActivo(true);
        productoRepository.save(producto);
        return productoMapper.toDto(producto);
    }

    @Transactional
    public void deleteProducto(UUID publicId)
    {
        ProductoEntity producto = productoRepository.findByPublicId(publicId)
                .orElseThrow(()-> new ProductoNotFoundException("No se encontro el producto con ese id"));

        boolean tienePedidosActivos = pedidoRepository.existsByDetalles_Producto_PublicIdAndEstadoPedidoIn(
                publicId,
                List.of(EstadoPedido.PENDIENTE, EstadoPedido.LISTO)
        );

        if (tienePedidosActivos)
        {
            throw new ProductoConPedidosActivosException(
                    "No se puede eliminar un producto con pedidos pendientes o listos");
        }

        producto.setActivo(false);
    }

    @Transactional
    public ProductoResponseDto updateProducto(UUID publicId, ProductoRequestDto dto)
    {
        ProductoEntity producto = productoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ProductoNotFoundException("No se encontró producto con ese id"));

        if (!producto.isActivo())
        {
            throw new ProductoInactivoException("El producto no está activo");
        }

        productoMapper.updateEntity(dto, producto);
        return productoMapper.toDto(producto);
    }
}
