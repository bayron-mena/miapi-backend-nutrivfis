package cl.duoc.miapi.repository;

import cl.duoc.miapi.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByCategoryId(Long categoryId);

    boolean existsByCategoryId(Long categoryId);

    // Bloquea las filas hasta que termine la compra (SELECT ... FOR UPDATE), así dos
    // compras simultáneas no pueden vender la misma última unidad.
    // Ordenado por id para que dos compras bloqueen en el mismo orden y no se trancen.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id")
    List<Product> findAllForUpdate(@Param("ids") Collection<String> ids);

    // Para el dashboard: productos con poco stock
    List<Product> findByStockLessThanEqualOrderByStockAsc(int stock);
}
