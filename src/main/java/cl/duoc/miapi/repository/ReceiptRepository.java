package cl.duoc.miapi.repository;

import cl.duoc.miapi.model.Receipt;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    // "details" se carga en la misma consulta para no hacer una consulta por boleta
    @EntityGraph(attributePaths = "details")
    List<Receipt> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "details")
    List<Receipt> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to);

    @EntityGraph(attributePaths = {"details", "user"})
    Optional<Receipt> findWithDetailsById(Long id);
}
