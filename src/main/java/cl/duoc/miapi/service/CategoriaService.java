package cl.duoc.miapi.service;

import cl.duoc.miapi.model.Categoria;
import cl.duoc.miapi.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    public Categoria crear(Categoria categoria) {
        if (categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
        // El id lo genera la base de datos
        categoria.setId(null);
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Long id, Categoria datos) {
        Categoria existente = buscarPorId(id);
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(datos.getNombre(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
        existente.setNombre(datos.getNombre());
        return categoriaRepository.save(existente);
    }

    public void eliminar(Long id) {
        Categoria existente = buscarPorId(id);
        categoriaRepository.delete(existente);
    }
}
