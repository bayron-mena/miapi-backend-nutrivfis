package cl.duoc.miapi.service;

import cl.duoc.miapi.model.Category;
import cl.duoc.miapi.repository.CategoryRepository;
import cl.duoc.miapi.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    public Category create(Category category) {
        category.setName(normalize(category.getName()));
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
        // El id lo genera la base de datos
        category.setId(null);
        return categoryRepository.save(category);
    }

    public Category update(Long id, Category data) {
        Category existing = findById(id);
        data.setName(normalize(data.getName()));
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(data.getName(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
        existing.setName(data.getName());
        return categoryRepository.save(existing);
    }

    public void delete(Long id) {
        Category existing = findById(id);
        // Sin esta revisión MySQL rechaza el borrado por la llave foránea
        if (productRepository.existsByCategoryId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se puede eliminar: hay productos en esta categoría");
        }
        categoryRepository.delete(existing);
    }

    // Igual que normalizeCategory del frontend: sin espacios sobrantes y en MAYÚSCULAS
    private String normalize(String name) {
        return name.trim().toUpperCase();
    }
}
