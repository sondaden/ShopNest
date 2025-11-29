package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.CartResponse;
import com.sonnhuynhh.shopnest.dto.CheckoutRequest;
import com.sonnhuynhh.shopnest.dto.OrderResponse;
import com.sonnhuynhh.shopnest.model.PaymentMethod;
import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import com.sonnhuynhh.shopnest.service.CartService;
import com.sonnhuynhh.shopnest.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Map;

/**
 * Controller xử lý trang giỏ hàng (Thymeleaf views)
 */
@Controller
@RequiredArgsConstructor
public class CartViewController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    /**
     * Trang giỏ hàng
     */
    @GetMapping("/cart")
    public String cart(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        
        try {
            CartResponse cart = cartService.getCart();
            model.addAttribute("cart", cart);
            model.addAttribute("cartItems", cart.items());
            
            // Tính toán các giá trị cho order summary
            java.math.BigDecimal subtotal = cart.totalPrice();
            java.math.BigDecimal shippingFee = subtotal.compareTo(java.math.BigDecimal.valueOf(500000)) >= 0 
                    ? java.math.BigDecimal.ZERO 
                    : java.math.BigDecimal.valueOf(30000);
            java.math.BigDecimal discount = java.math.BigDecimal.ZERO;
            java.math.BigDecimal total = subtotal.add(shippingFee).subtract(discount);
            
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("discount", discount);
            model.addAttribute("total", total);
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải giỏ hàng: " + e.getMessage());
            model.addAttribute("cartItems", Collections.emptyList());
            model.addAttribute("subtotal", java.math.BigDecimal.ZERO);
            model.addAttribute("shippingFee", java.math.BigDecimal.ZERO);
            model.addAttribute("discount", java.math.BigDecimal.ZERO);
            model.addAttribute("total", java.math.BigDecimal.ZERO);
        }
        return "cart";
    }

    /**
     * Thêm sản phẩm vào giỏ hàng (form submit)
     */
    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<?> addToCartForm(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập"));
        }
        
        try {
            var request = new com.sonnhuynhh.shopnest.dto.AddToCartRequest(productId, quantity);
            CartResponse cart = cartService.addToCart(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã thêm vào giỏ hàng",
                "cartCount", cart.totalItems()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cập nhật số lượng sản phẩm
     */
    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<?> updateCartItem(
            @RequestParam Long itemId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập"));
        }
        
        try {
            CartResponse cart = cartService.updateQuantity(itemId, quantity);
            return ResponseEntity.ok(Map.of("success", true, "cartCount", cart.totalItems()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(
            @RequestParam Long itemId,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập"));
        }
        
        try {
            cartService.removeByItemId(itemId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa sản phẩm"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Trang checkout
     */
    @GetMapping("/checkout")
    public String checkout(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        
        try {
            CartResponse cart = cartService.getCart();
            if (cart.items() == null || cart.items().isEmpty()) {
                return "redirect:/cart";
            }
            
            model.addAttribute("cart", cart);
            model.addAttribute("cartItems", cart.items());
            
            // Lấy thông tin user hiện tại để điền sẵn form
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElse(null);
            model.addAttribute("currentUser", currentUser);
            
            // Tính toán các giá trị cho order summary
            java.math.BigDecimal subtotal = cart.totalPrice();
            java.math.BigDecimal shippingFee = java.math.BigDecimal.valueOf(30000);
            java.math.BigDecimal discount = java.math.BigDecimal.ZERO;
            java.math.BigDecimal total = subtotal.add(shippingFee).subtract(discount);
            
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("discount", discount);
            model.addAttribute("total", total);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
        return "checkout";
    }

    /**
     * Xử lý đặt hàng (form submit)
     */
    @PostMapping("/checkout")
    public String processCheckout(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String province,
            @RequestParam String district,
            @RequestParam String ward,
            @RequestParam String address,
            @RequestParam(required = false) String note,
            @RequestParam String paymentMethod,
            @RequestParam(required = false, defaultValue = "STANDARD") String shippingMethod,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        
        try {
            // Build full address
            String fullAddress = address + ", " + ward + ", " + district + ", " + province;
            
            // Map payment method string to enum
            PaymentMethod paymentMethodEnum = switch (paymentMethod) {
                case "BANKING" -> PaymentMethod.BANK_TRANSFER;
                case "MOMO" -> PaymentMethod.MOMO;
                case "VNPAY" -> PaymentMethod.VNPAY;
                default -> PaymentMethod.COD;
            };
            
            // Create checkout request
            CheckoutRequest checkoutRequest = new CheckoutRequest(
                    fullAddress,
                    phone,
                    fullName,
                    note,
                    paymentMethodEnum
            );
            
            // Process checkout
            OrderResponse order = orderService.checkout(checkoutRequest);
            
            // Redirect to success page with order info
            redirectAttributes.addFlashAttribute("order", order);
            redirectAttributes.addFlashAttribute("orderCode", order.orderCode());
            return "redirect:/order-success";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đặt hàng thất bại: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    /**
     * Trang đặt hàng thành công
     */
    @GetMapping("/order-success")
    public String orderSuccess(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return "order-success";
    }
}
