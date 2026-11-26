package cl.duoc.miapi.service;

import cl.duoc.miapi.dto.ReceiptRequest;
import cl.duoc.miapi.dto.ReceiptResponse;
import cl.duoc.miapi.model.Product;
import cl.duoc.miapi.model.Receipt;
import cl.duoc.miapi.model.ReceiptDetail;
import cl.duoc.miapi.model.User;
import cl.duoc.miapi.repository.ProductRepository;
import cl.duoc.miapi.repository.ReceiptRepository;
import cl.duoc.miapi.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReceiptService(ReceiptRepository receiptRepository,
                          ProductRepository productRepository,
                          UserRepository userRepository) {
        this.receiptRepository = receiptRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // Confirma la compra: mismas reglas que checkout() del frontend.
    // Todo ocurre en una transacción: si un producto falla, no se descuenta ningún stock.
    @Transactional
    public ReceiptResponse create(String email, ReceiptRequest request) {
        User user = currentUser(email);
        if (user.isLocked() || !user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu cuenta no puede realizar compras.");
        }

        // Junta líneas repetidas del mismo producto (el frontend también suma cantidades)
        Map<String, Line> lines = new LinkedHashMap<>();
        for (ReceiptRequest.Item item : request.items()) {
            String id = item.productId().trim().toUpperCase();
            Line line = lines.get(id);
            if (line == null) {
                lines.put(id, new Line(item.quantity(), item.price()));
            } else if (line.price != item.price()) {
                throw conflict("El precio cambió. Actualiza el carrito antes de confirmar.");
            } else {
                line.quantity += item.quantity();
            }
        }

        // Bloquea los productos hasta el final de la compra (ver ProductRepository)
        Map<String, Product> products = productRepository.findAllForUpdate(lines.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 1) Validar toda la compra antes de descontar cualquier producto
        for (Map.Entry<String, Line> entry : lines.entrySet()) {
            Product product = products.get(entry.getKey());
            Line line = entry.getValue();
            if (product == null) {
                throw conflict("El producto " + entry.getKey() + " ya no está disponible. Revisa tu carrito.");
            }
            if (product.getPrice() != line.price) {
                throw conflict("El precio cambió. Actualiza el carrito antes de confirmar.");
            }
            if (product.getStock() < line.quantity) {
                throw conflict("Stock insuficiente para " + product.getName() + ". Revisa tu carrito.");
            }
        }

        // 2) Descontar stock y armar la boleta
        Receipt receipt = new Receipt();
        receipt.setUser(user);
        receipt.setCustomerName((user.getName() + " " + user.getLastName()).trim());
        receipt.setCustomerEmail(user.getEmail());
        receipt.setCreatedAt(Instant.now());

        int total = 0;
        for (Map.Entry<String, Line> entry : lines.entrySet()) {
            Product product = products.get(entry.getKey());
            int quantity = entry.getValue().quantity;
            product.setStock(product.getStock() - quantity);

            ReceiptDetail detail = new ReceiptDetail();
            detail.setProductId(product.getId());
            detail.setProductName(product.getName());
            detail.setCategoryName(product.getCategory().getName());
            detail.setUnitPrice(product.getPrice());
            detail.setQuantity(quantity);
            detail.setSubtotal(Math.multiplyExact(product.getPrice(), quantity));
            receipt.addDetail(detail);
            total = Math.addExact(total, detail.getSubtotal());
        }

        // El precio de venta ya incluye IVA: neto = total / 1,19
        int net = (int) Math.round(total / (1 + Receipt.IVA_RATE));
        receipt.setTotal(total);
        receipt.setNetAmount(net);
        receipt.setTaxAmount(total - net);

        return ReceiptResponse.from(receiptRepository.save(receipt));
    }

    // Historial del usuario conectado: solo sus propias boletas
    @Transactional(readOnly = true)
    public List<ReceiptResponse> findMine(String email) {
        return receiptRepository.findByUserIdOrderByCreatedAtDesc(currentUser(email).getId())
                .stream().map(ReceiptResponse::from).toList();
    }

    // Una boleta: el dueño la puede ver; Admin y Vendedor ven cualquiera.
    // A otro usuario se le responde 404, para no revelar que la boleta existe.
    @Transactional(readOnly = true)
    public ReceiptResponse findById(Long id, String email, boolean staff) {
        Receipt receipt = receiptRepository.findWithDetailsById(id)
                .filter(r -> staff || r.getUser().getEmail().equals(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boleta no encontrada"));
        return ReceiptResponse.from(receipt);
    }

    // Todas las ventas de un período (Admin y Vendedor)
    @Transactional(readOnly = true)
    public List<ReceiptResponse> findAll(Instant from, Instant to) {
        return receiptRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to)
                .stream().map(ReceiptResponse::from).toList();
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para acceder a este recurso."));
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private static final class Line {
        int quantity;
        final int price;

        Line(int quantity, int price) {
            this.quantity = quantity;
            this.price = price;
        }
    }
}
