package com.techstore.techstore.Controller;

import com.techstore.techstore.Service.OrderService;
import com.techstore.techstore.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    /** 🔹 Danh sách tất cả đơn hàng */
    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<Order> pageData = orderService.getPagedOrders(page, size);

        model.addAttribute("orders", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("pageSize", size);

        model.addAttribute("searchMode", false);  // 👈 chế độ phân trang
        model.addAttribute("active", "orders");

        return "admin/orders";
    }

    /** 🔹 Cập nhật trạng thái đơn hàng */
    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateStatus(id, status);
        return "redirect:/admin/orders?updated=true";
    }

    /** 🔹 Xem chi tiết đơn hàng */
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id).orElse(null);
        if (order == null) return "redirect:/admin/orders?error=notfound";

        model.addAttribute("order", order);
        model.addAttribute("items", order.getOrderItems());
        model.addAttribute("pageTitle", "Chi tiết đơn hàng #" + id);
        return "admin/order_detail";
    }

    /** 🔹 Xóa đơn hàng */
    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return "redirect:/admin/orders";
    }

    @GetMapping("/search")
    public String searchOrders(
            @RequestParam String q,
            Model model
    ) {
        List<Order> results;

        if (q.matches("\\d+")) { // tìm theo ID
            orderService.getOrderById(Long.parseLong(q))
                    .ifPresentOrElse(
                            order -> model.addAttribute("orders", List.of(order)),
                            () -> model.addAttribute("orders", List.of())
                    );
        } else {
            model.addAttribute("orders", List.of());
        }

        model.addAttribute("q", q);
        model.addAttribute("searchMode", true); // 👈 tắt phân trang
        model.addAttribute("active", "orders");

        return "admin/orders";
    }

    @GetMapping("/filter-date")
    public String filterOrdersByDate(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
            Model model
    ) {
        List<Order> orders = orderService.findOrdersBetweenDates(
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay()
        );

        model.addAttribute("orders", orders);
        model.addAttribute("searchMode", true);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("active", "orders");  // ⭐ thêm để sidebar highlight đúng

        return "admin/orders"; // ⭐ sửa lại đúng view
    }



}
