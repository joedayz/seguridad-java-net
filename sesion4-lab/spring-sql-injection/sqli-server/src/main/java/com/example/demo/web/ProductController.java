package com.example.demo.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repo.ResultadoBusquedaProducto;
import com.example.demo.service.ProductSearchService;

/**
 * Expone la MISMA busqueda de productos por nombre en dos variantes para la
 * demo del "antes y despues":
 *
 *  GET /api/productos/vulnerable?q=...   -> concatenacion en LIKE (explotable)
 *  GET /api/productos/seguro?q=...       -> PreparedStatement (a prueba de SQLi)
 *
 * La respuesta incluye el SQL ejecutado para que, en clase, se vea como la
 * inyeccion reescribe la consulta.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductController {

    private final ProductSearchService productSearchService;

    public ProductController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @GetMapping("/vulnerable")
    public Map<String, Object> vulnerable(@RequestParam String q) {
        return aRespuesta(
                "VULNERABLE (concatenacion de String en LIKE)",
                q,
                productSearchService.searchProductsVulnerable(q));
    }

    @GetMapping("/seguro")
    public Map<String, Object> seguro(@RequestParam String q) {
        return aRespuesta(
                "SEGURO (PreparedStatement con LIKE parametrizado)",
                q,
                productSearchService.searchProductsSeguro(q));
    }

    private Map<String, Object> aRespuesta(String modo, String q, ResultadoBusquedaProducto resultado) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("modo", modo);
        respuesta.put("terminoRecibido", q);
        respuesta.put("sqlEjecutado", resultado.sqlEjecutado());
        respuesta.put("totalFilas", resultado.total());
        respuesta.put("productos", resultado.productos());
        return respuesta;
    }
}
