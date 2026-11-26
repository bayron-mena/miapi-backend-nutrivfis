package cl.duoc.miapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

// Detalle de boleta: una línea por producto. El producto se guarda como copia
// (código, nombre, categoría y precio de ese momento) y no como llave foránea,
// así un producto se puede eliminar sin romper las boletas antiguas.
@Setter
@Getter
@Entity
@Table(name = "receipt_details")
public class ReceiptDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    @Column(nullable = false, length = 20)
    private String productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String categoryName;

    @Column(nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int quantity;

    // unitPrice * quantity
    @Column(nullable = false)
    private int subtotal;
}
