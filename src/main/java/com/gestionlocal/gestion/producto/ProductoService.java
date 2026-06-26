package com.gestionlocal.gestion.producto;

import com.gestionlocal.gestion.producto.dto.ProductoRequestDto;
import com.gestionlocal.gestion.producto.dto.ProductoResponseDto;
import com.gestionlocal.gestion.producto.exception.ProductoAlreadyExists;
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

    public ProductoService(ProductoRepository productoRepository, ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
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

    @Transactional
    public ProductoResponseDto createProducto(ProductoRequestDto productoRequestDto)
    {
        if(productoRepository.existsByNombre(productoRequestDto.getNombre()))
        {
            throw new ProductoAlreadyExists("Ya existe producto con ese id");
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

        ///Habria que poner tambien que si el producto existe en un pedido actual , no se pueda eliminar

        producto.setActivo(false);
    }
}
