package com.bhavya.ecommerce.service;

import com.bhavya.ecommerce.model.Order;
import com.bhavya.ecommerce.model.OrderItem;
import com.bhavya.ecommerce.model.User;
import com.bhavya.ecommerce.model.CartItem;
import com.bhavya.ecommerce.repository.OrderRepository;
import com.bhavya.ecommerce.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    public List<Order> getAllOrders() {
        return orderRepository.findAllOrdersOrderByDateDesc();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order createOrderFromCart(User user, String shippingAddress) {
        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Calculate total amount
        BigDecimal totalAmount = cartItems.stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order(user, totalAmount, shippingAddress);
        order = orderRepository.save(order);

        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(order, cartItem.getProduct(), 
                    cartItem.getQuantity(), cartItem.getProduct().getPrice());
            orderItems.add(orderItemRepository.save(orderItem));
            
            // Update product stock
            productService.updateStock(cartItem.getProduct().getId(), cartItem.getQuantity());
        }

        order.setOrderItems(orderItems);
        
        // Clear cart after order
        cartService.clearCart(user);

        return order;
    }

    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found");
    }

    public void cancelOrder(Long orderId) {
        updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
    }

    public Long getOrderCountByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }
}
