package com.bhavya.ecommerce.repository;

import com.bhavya.ecommerce.model.Order;
import com.bhavya.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByUserOrderByOrderDateDesc(User user);
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = ?1 ORDER BY o.orderDate DESC")
    List<Order> findOrdersByUserId(Long userId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = ?1")
    Long countByStatus(Order.OrderStatus status);
    
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findAllOrdersOrderByDateDesc();
}
