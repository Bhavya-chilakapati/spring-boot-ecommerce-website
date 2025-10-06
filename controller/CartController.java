package com.bhavya.ecommerce.controller;

import com.bhavya.ecommerce.model.CartItem;
import com.bhavya.ecommerce.model.Product;
import com.bhavya.ecommerce.model.User;
import com.bhavya.ecommerce.service.CartService;
import com.bhavya.ecommerce.service.ProductService;
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
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartService.getCartItems(user);
        BigDecimal cartTotal = cartService.getCartTotal(user);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartTotal);
        return "cart";
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                           @RequestParam(defaultValue = "1") Integer quantity,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isPresent()) {
            cartService.addToCart(user, productOpt.get(), quantity);
            redirectAttributes.addFlashAttribute("success", "Product added to cart!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Product not found!");
        }

        return "redirect:/products";
    }

    @PostMapping("/update/{cartItemId}")
    public String updateCartItem(@PathVariable Long cartItemId,
                                @RequestParam Integer quantity,
                                RedirectAttributes redirectAttributes) {
        
        try {
            cartService.updateCartItemQuantity(cartItemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating cart!");
        }

        return "redirect:/cart";
    }

    @PostMapping("/remove/{cartItemId}")
    public String removeFromCart(@PathVariable Long cartItemId,
                                RedirectAttributes redirectAttributes) {
        
        cartService.removeFromCart(cartItemId);
        redirectAttributes.addFlashAttribute("success", "Item removed from cart!");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        cartService.clearCart(user);
        redirectAttributes.addFlashAttribute("success", "Cart cleared!");
        return "redirect:/cart";
    }
}
