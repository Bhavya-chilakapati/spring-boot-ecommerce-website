package com.bhavya.ecommerce.repository;

import com.bhavya.ecommerce.model.CartItem;
import com.bhavya.ecommerce.model.User;
import com.bhavya.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    void deleteByUser(User user);
    void deleteByUserAndProduct(User user, Product product);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.user.id = ?1")
    List<CartItem> findByUserId(Long userId);
    
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.user.id = ?1")
    Integer getCartItemCountByUserId(Long userId);
    
    @Query("SELECT SUM(ci.quantity * ci.product.price) FROM CartItem ci WHERE ci.user.id = ?1")
    Double getCartTotalByUserId(Long userId);
}
