package com.gestionlocal.gestion.config;

import com.gestionlocal.gestion.producto.ProductoEntity;
import com.gestionlocal.gestion.producto.ProductoRepository;
import com.gestionlocal.gestion.producto.TipoProducto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CatalogoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogoDataInitializer.class);
    private final ProductoRepository productoRepository;

    public CatalogoDataInitializer(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    private record ItemCatalogo(String nombre, TipoProducto tipo, Double precio) {}

    @Override
    public void run(String... args) {
        List<ItemCatalogo> catalogo = List.of(
            new ItemCatalogo("Torta olivia", TipoProducto.TORTA_COMUN, 58000.0),
            new ItemCatalogo("Tarta de Manzana", TipoProducto.TORTA_COMUN, 45000.0),
            new ItemCatalogo("Lemon cheese", TipoProducto.TORTA_COMUN, 48000.0),
            new ItemCatalogo("Br Mousse", TipoProducto.TORTA_COMUN, 49000.0),
            new ItemCatalogo("Brownie", TipoProducto.TORTA_COMUN, 48000.0),
            new ItemCatalogo("Merengada", TipoProducto.TORTA_COMUN, 55000.0),
            new ItemCatalogo("Rogel", TipoProducto.TORTA_COMUN, 55000.0),
            new ItemCatalogo("Tarta de frutillas", TipoProducto.TORTA_COMUN, 65000.0),
            new ItemCatalogo("Lemon pie", TipoProducto.TORTA_COMUN, 45000.0),
            new ItemCatalogo("Africano", TipoProducto.TORTA_COMUN, 48000.0),
            new ItemCatalogo("Años locos", TipoProducto.TORTA_COMUN, 48000.0),
            new ItemCatalogo("Gustavo", TipoProducto.TORTA_COMUN, 50000.0),
            new ItemCatalogo("Marquise", TipoProducto.TORTA_COMUN, 55000.0),
            new ItemCatalogo("Merengada con frutillas", TipoProducto.TORTA_COMUN, 65000.0),
            new ItemCatalogo("Delicia", TipoProducto.TORTA_COMUN, 60000.0),
            new ItemCatalogo("Quemado", TipoProducto.TORTA_COMUN, 52000.0),
            new ItemCatalogo("Shots", TipoProducto.INDIVIDUAL, 3800.0),
            new ItemCatalogo("Mini profiteroles", TipoProducto.MINI, 2500.0),
            new ItemCatalogo("Mini pavlova", TipoProducto.MINI, 3000.0),
            new ItemCatalogo("Mini tarteletas", TipoProducto.MINI, 3000.0),
            new ItemCatalogo("Alfajores", TipoProducto.INDIVIDUAL, 3500.0),
            new ItemCatalogo("Mesa dulce", TipoProducto.TORTA_COMUN, 50000.0),
            new ItemCatalogo("Porcion", TipoProducto.INDIVIDUAL, 10000.0),
            new ItemCatalogo("Mini torta", TipoProducto.MINI, 28000.0),
            new ItemCatalogo("Postre mousse", TipoProducto.INDIVIDUAL, 20000.0),
            new ItemCatalogo("Postre tiramisu", TipoProducto.INDIVIDUAL, 20000.0),
            new ItemCatalogo("Postre cheesecake", TipoProducto.INDIVIDUAL, 20000.0),
            new ItemCatalogo("Torta vasca", TipoProducto.TORTA_COMUN, 50000.0),
            new ItemCatalogo("Milhojas", TipoProducto.TORTA_COMUN, 50000.0),
            new ItemCatalogo("Brownie frutal", TipoProducto.TORTA_COMUN, 60000.0),
            new ItemCatalogo("Chipa", TipoProducto.INDIVIDUAL, 12000.0),
            new ItemCatalogo("Scon de queso", TipoProducto.INDIVIDUAL, 12000.0),
            new ItemCatalogo("Caja variada dulce", TipoProducto.INDIVIDUAL, 25000.0),
            new ItemCatalogo("Caja variada salada", TipoProducto.INDIVIDUAL, 25000.0),
            new ItemCatalogo("Mini alfajores", TipoProducto.MINI, 2000.0),
            new ItemCatalogo("Carrot cake", TipoProducto.TORTA_COMUN, 63000.0),
            new ItemCatalogo("Postre chocotorta", TipoProducto.INDIVIDUAL, 25000.0),
            new ItemCatalogo("Tarta de coco", TipoProducto.TORTA_COMUN, 38000.0),
            new ItemCatalogo("Valeria", TipoProducto.TORTA_COMUN, 45000.0),
            new ItemCatalogo("Seña", TipoProducto.INDIVIDUAL, 20000.0),
            new ItemCatalogo("Tarta ddl", TipoProducto.TORTA_COMUN, 38000.0),
            new ItemCatalogo("Pasta frola", TipoProducto.TORTA_COMUN, 30000.0),
            new ItemCatalogo("Manzana con crumble", TipoProducto.TORTA_COMUN, 30000.0)
        );

        int insertados = 0;
        for (ItemCatalogo item : catalogo) {
            if (!productoRepository.existsByNombreProducto(item.nombre())) {
                ProductoEntity p = new ProductoEntity();
                p.setPublicId(UUID.randomUUID());
                p.setNombreProducto(item.nombre());
                p.setTipoProducto(item.tipo());
                p.setPrecio(item.precio());
                p.setActivo(true);
                productoRepository.save(p);
                insertados++;
            }
        }
        if (insertados > 0) {
            log.info("Catálogo inicializado con éxito: {} nuevos productos agregados.", insertados);
        } else {
            log.info("Catálogo verificado: todos los productos ya existen en la base de datos.");
        }
    }
}
