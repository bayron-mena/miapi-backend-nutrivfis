package cl.duoc.miapi.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// Boleta: una compra confirmada. Guarda una copia de los datos del comprador y de
// cada producto (en ReceiptDetail) para que la boleta no cambie si después se
// edita o elimina el producto o el usuario.
@Setter
@Getter
@Entity
@Table(name = "receipts", indexes = {
        @Index(name = "idx_receipt_user", columnList = "user_id"),
        @Index(name = "idx_receipt_created", columnList = "createdAt")
})
public class Receipt {

    // Boleta afecta: el total incluye IVA (19% en Chile)
    public static final double IVA_RATE = 0.19;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Copia del comprador al momento de la compra
    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private Instant createdAt;

    // Total pagado (IVA incluido), en pesos
    @Column(nullable = false)
    private int total;

    // Desglose del total: neto + IVA = total
    @Column(nullable = false)
    private int netAmount;

    @Column(nullable = false)
    private int taxAmount;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceiptDetail> details = new ArrayList<>();

    public void addDetail(ReceiptDetail detail) {
        detail.setReceipt(this);
        details.add(detail);
    }
}
