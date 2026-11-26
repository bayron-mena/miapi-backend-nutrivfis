package cl.duoc.miapi.controller;

import cl.duoc.miapi.dto.StockRequest;
import cl.duoc.miapi.model.Product;
import cl.duoc.miapi.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Orígenes permitidos en application.properties (app.cors.allowed-origins)
@CrossOrigin(originPatterns = "${app.cors.allowed-origins}")
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Opcional: /api/v1/products?categoryId=3 filtra por categoría
    @GetMapping
    public List<Product> findAll(@RequestParam(required = false) Long categoryId) {
        return productService.findAll(categoryId);
    }

    @GetMapping("/{id}")
    public Product findById(@PathVariable String id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody Product product) {
        return productService.create(product);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable String id,
                          @Valid @RequestBody Product product) {
        return productService.update(id, product);
    }

    // Cambia solo el stock, sin volver a validar el resto del producto
    @PatchMapping("/{id}/stock")
    public Product updateStock(@PathVariable String id,
                               @Valid @RequestBody StockRequest request) {
        return productService.updateStock(id, request.getStock());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        productService.delete(id);
    }
}
