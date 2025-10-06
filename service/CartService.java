package com.bhavya.ecommerce.service;

import com.bhavya.ecommerce.model.CartItem;
import com.bhavya.ecommerce.model.Product;
import com.bhavya.ecommerce.model.User;
import com.bhavya.ecommerce.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartItemRepository cartItemRepository;

    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    public CartItem addToCart(User user, Product product, Integer quantity) {
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(user, product);
        
        if (existingItem.isPresent()) {
            // Update quantity if item already exists
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            return cartItemRepository.save(cartItem);
        } else {
            // Create new cart item
            CartItem cartItem = new CartItem(user, product, quantity);
            return cartItemRepository.save(cartItem);
        }
    }

    public CartItem updateCartItemQuantity(Long cartItemId, Integer quantity) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if (cartItemOpt.isPresent()) {
            CartItem cartItem = cartItemOpt.get();
            if (quantity <= 0) {
                cartItemRepository.delete(cartItem);
                return null;
            } else {
                cartItem.setQuantity(quantity);
                return cartItemRepository.save(cartItem);
            }
        }
        throw new RuntimeException("Cart item not found");
    }

    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public void removeFromCart(User user, Product product) {
        cartItemRepository.deleteByUserAndProduct(user, product);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    public Integer getCartItemCount(User user) {
        return cartItemRepository.getCartItemCountByUserId(user.getId());
    }

    public BigDecimal getCartTotal(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        return cartItems.stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isCartEmpty(User user) {
        return cartItemRepository.findByUser(user).isEmpty();
    }
}
