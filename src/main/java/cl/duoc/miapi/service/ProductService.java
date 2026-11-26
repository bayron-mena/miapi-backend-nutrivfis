package cl.duoc.miapi.service;

import cl.duoc.miapi.config.FieldValidationException;
import cl.duoc.miapi.model.Category;
import cl.duoc.miapi.model.Product;
import cl.duoc.miapi.repository.CategoryRepository;
import cl.duoc.miapi.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> findAll(Long categoryId) {
        if (categoryId == null) {
            return productRepository.findAll();
        }
        return productRepository.findByCategoryId(categoryId);
    }

    public Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public Product create(Product product) {
        if (product.getId() == null || product.getId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El código (id) del producto es obligatorio");
        }
        product.setId(product.getId().trim().toUpperCase());
        // Con id de texto, save() sobrescribiría un producto existente; por eso se revisa antes
        if (productRepository.existsById(product.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un producto con ese código");
        }
        if (product.getStock() < 1) {
            throw new FieldValidationException(Map.of("stock",
                    "Un producto nuevo debe tener al menos 1 unidad de stock."));
        }
        product.setName(product.getName().trim());
        product.setDistributor(product.getDistributor().trim());
        product.setCategory(resolveCategory(product.getCategory()));
        return productRepository.save(product);
    }

    // El código (id) no se cambia: se usa siempre el de la URL
    public Product update(String id, Product data) {
        Product existing = findById(id);
        existing.setName(data.getName().trim());
        existing.setDistributor(data.getDistributor().trim());
        existing.setDescription(data.getDescription());
        existing.setPrice(data.getPrice());
        existing.setStock(data.getStock());
        existing.setImage(data.getImage());
        existing.setActiveIngredient(data.getActiveIngredient());
        existing.setSpecies(data.getSpecies());
        existing.setCategory(resolveCategory(data.getCategory()));
        return productRepository.save(existing);
    }

    public Product updateStock(String id, int stock) {
        Product existing = findById(id);
        existing.setStock(stock);
        return productRepository.save(existing);
    }

    public void delete(String id) {
        Product existing = findById(id);
        productRepository.delete(existing);
    }

    // El cliente envía solo el id de la categoría; aquí se valida que exista
    private Category resolveCategory(Category category) {
        if (category == null || category.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Selecciona una categoría válida.");
        }
        return categoryRepository.findById(category.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Selecciona una categoría válida."));
    }
}
