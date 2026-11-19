package cl.duoc.miapi.service;

import cl.duoc.miapi.model.Categoria;
import cl.duoc.miapi.model.Producto;
import cl.duoc.miapi.repository.CategoriaRepository;
import cl.duoc.miapi.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Producto> listar(Long categoriaId) {
        if (categoriaId == null) {
            return productoRepository.findAll();
        }
        return productoRepository.findByCategoriaId(categoriaId);
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public Producto crear(Producto producto) {
        if (productoRepository.existsByCodigoIgnoreCase(producto.getCodigo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un producto con ese código");
        }
        producto.setCategoria(resolverCategoria(producto.getCategoria()));
        // El id lo genera la base de datos
        producto.setId(null);
        return productoRepository.save(producto);
    }

    public Producto actualizar(Long id, Producto datos) {
        Producto existente = buscarPorId(id);
        if (productoRepository.existsByCodigoIgnoreCaseAndIdNot(datos.getCodigo(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un producto con ese código");
        }
        existente.setCodigo(datos.getCodigo());
        existente.setNombre(datos.getNombre());
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        existente.setImagen(datos.getImagen());
        existente.setPrincipioActivo(datos.getPrincipioActivo());
        existente.setEspecie(datos.getEspecie());
        existente.setCategoria(resolverCategoria(datos.getCategoria()));
        return productoRepository.save(existente);
    }

    public void eliminar(Long id) {
        Producto existente = buscarPorId(id);
        productoRepository.delete(existente);
    }

    // El cliente envía solo el id de la categoría; aquí se valida que exista
    private Categoria resolverCategoria(Categoria categoria) {
        if (categoria == null || categoria.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Debe indicar el id de la categoría");
        }
        return categoriaRepository.findById(categoria.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "La categoría indicada no existe"));
    }
}
