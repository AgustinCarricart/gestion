package com.gestionlocal.gestion.producto;

import com.gestionlocal.gestion.producto.dto.ProductoRequestDto;
import com.gestionlocal.gestion.producto.dto.ProductoResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/productos/")
public class ProductoController
{

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService)
    {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDto>> getAllProductos()
    {
        return ResponseEntity.ok(productoService.getAllProductos());
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ProductoResponseDto> getProductoByPublicId(@PathVariable UUID publicId)
    {
        return ResponseEntity.ok(productoService.getProductoByPublicId(publicId));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ProductoResponseDto>> getProductosByTipo(@PathVariable TipoProducto tipo)
    {
        return ResponseEntity.ok(productoService.getProductosByTipo(tipo));
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDto> createProducto(@Valid @RequestBody ProductoRequestDto dto)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.createProducto(dto));
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<ProductoResponseDto> updateProducto(@PathVariable UUID publicId,
                                                              @Valid @RequestBody ProductoRequestDto dto)
    {
        return ResponseEntity.ok(productoService.updateProducto(publicId, dto));
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteProducto(@PathVariable UUID publicId)
    {
        productoService.deleteProducto(publicId);
        return ResponseEntity.noContent().build();
    }
}
