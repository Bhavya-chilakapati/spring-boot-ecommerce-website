package com.bhavya.ecommerce.controller;

import com.bhavya.ecommerce.model.Order;
import com.bhavya.ecommerce.model.User;
import com.bhavya.ecommerce.service.CartService;
import com.bhavya.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (cartService.isCartEmpty(user)) {
            return "redirect:/cart";
        }

        BigDecimal cartTotal = cartService.getCartTotal(user);
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("user", user);
        return "checkout";
    }

    @PostMapping("/place")
    public String placeOrder(@RequestParam String shippingAddress,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.createOrderFromCart(user, shippingAddress);
            redirectAttributes.addFlashAttribute("success", 
                "Order placed successfully! Order ID: " + order.getId());
            return "redirect:/orders/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error placing order: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping
    public String myOrders(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderService.getOrdersByUser(user);
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/{orderId}")
    public String orderDetails(@PathVariable Long orderId,
                              HttpSession session,
                              Model model) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Check if order belongs to current user
            if (!order.getUser().getId().equals(user.getId())) {
                return "redirect:/orders";
            }
            model.addAttribute("order", order);
            return "order-details";
        }

        return "redirect:/orders";
    }
}
