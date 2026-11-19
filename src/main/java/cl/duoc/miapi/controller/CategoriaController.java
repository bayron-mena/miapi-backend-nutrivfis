package cl.duoc.miapi.controller;

import cl.duoc.miapi.model.Categoria;
import cl.duoc.miapi.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@CrossOrigin(origins = "http://localhost:5173")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<Categoria> listar() {
        return categoriaService.listarTodas();
    }

    @GetMapping("/{id}")
    public Categoria obtenerPorId(@PathVariable Long id) {
        return categoriaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria crear(@Valid @RequestBody Categoria categoria) {
        return categoriaService.crear(categoria);
    }

    @PutMapping("/{id}")
    public Categoria actualizar(@PathVariable Long id,
                                @Valid @RequestBody Categoria categoria) {
        return categoriaService.actualizar(id, categoria);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
    }
}
