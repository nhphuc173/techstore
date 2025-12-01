package com.techstore.techstore.Service;

import com.techstore.techstore.Repository.CartItemRepository;
import com.techstore.techstore.Repository.CartRepository;
import com.techstore.techstore.Repository.ProductRepository;
import com.techstore.techstore.Repository.ProductVariantRepository;
import com.techstore.techstore.entity.Cart;
import com.techstore.techstore.entity.CartItem;
import com.techstore.techstore.entity.Product;
import com.techstore.techstore.entity.ProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;


    // Lấy giỏ của user (có thể rỗng)
    @Transactional(readOnly = true)
    public Optional<Cart> getCartByUserId(Long userId) {
        return cartRepository.findByUser_Id(userId);
    }
    public int getCartItemCount(String username) {
        Cart cart = cartRepository.findByUser_Username(username).orElse(null);
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }


    // Thêm / gộp sản phẩm vào giỏ + chốt giá + tính tổng
    @Transactional
    public Cart addToCart(Cart cart, Product product, ProductVariant variant, int quantity) {
        int addQty = Math.max(1, quantity);

        Optional<CartItem> existingOpt = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId())
                        && ((variant == null && i.getVariant() == null) ||
                        (variant != null && i.getVariant() != null && i.getVariant().getId().equals(variant.getId()))))
                .findFirst();

        CartItem item;
        if (existingOpt.isPresent()) {
            // ✅ Nếu sản phẩm (và variant) đã có → cộng dồn số lượng
            item = existingOpt.get();
            item.setQuantity(item.getQuantity() + addQty);
            item.recalc();
        } else {
            // ✅ Nếu chưa có → thêm mới
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setVariant(variant);
            item.setQuantity(addQty);
            item.setUnitPriceAtAdd(product.getPrice());
            item.recalc();
            cart.getItems().add(item);
        }

        cart.recalcTotals();
        return cartRepository.save(cart);
    }



    // Xoá toàn bộ item trong giỏ + reset tổng
    @Transactional
    public void clearCart(Long cartId) {
        cartItemRepository.deleteAll(cartItemRepository.findByCart_Id(cartId));
        cartRepository.findById(cartId).ifPresent(c -> {
            c.getItems().clear();
            c.recalcTotals();
            cartRepository.save(c);
        });
    }
}
