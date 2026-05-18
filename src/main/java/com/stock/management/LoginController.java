package com.stock.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class LoginController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password, Model model) {
        
        // ডাটাবেজে আপনার এডমিন ইউজার না থাকলে তৈরি হবে
        if (userRepository.findByUsername("admin") == null) {
            userRepository.save(new User("admin", "admin123", "Tasriful Islam", "Admin"));
        }

        if (productRepository.count() == 0) {
            productRepository.save(new Product("#INV-101", "HP ProBook Laptop", 5, 2750.00, "Paid"));
            productRepository.save(new Product("#INV-102", "Logitech Wireless Mouse", 12, 240.00, "Paid"));
            productRepository.save(new Product("#INV-103", "Asus Gaming Monitor", 3, 750.00, "Pending"));
            productRepository.save(new Product("#INV-104", "Anker PowerBank 20k", 20, 800.00, "Paid"));
        }

        User user = userRepository.findByUsername(username);
        
        // NullPointerException প্রতিরোধ করার জন্য নিখুঁত কন্ডিশন
        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
            return "redirect:/dashboard?user=" + username;
        }

        return "redirect:/login?error";
    }

    @GetMapping("/dashboard")
    public String showDashboardPage(@RequestParam(value = "user", defaultValue = "admin") String username, Model model) {
        List<Product> productList = productRepository.findAll();
        
        double totalValuation = 0;
        int totalQuantity = 0;
        for (Product p : productList) {
            totalValuation += p.getTotalPrice();
            totalQuantity += p.getQuantity();
        }
        double estimatedProfit = totalValuation * 0.20;

        User user = userRepository.findByUsername(username);
        if (user == null) {
            user = new User("admin", "admin123", "Tasriful Islam", "Admin");
        }

        // 🎯 [অটোমেটিক ইনভয়েস আইডি জেনারেশন লজিক শুরু]
        String nextInvoiceId = "#INV-101"; // ডাটাবেজ ফাঁকা থাকলে প্রথম ডিফল্ট আইডি
        if (productList != null && !productList.isEmpty()) {
            // ডাটাবেজের একদম শেষ প্রোডাক্টটি খুঁজে বের করা
            Product lastProduct = productList.get(productList.size() - 1);
            String lastInvoiceId = lastProduct.getInvoiceId();

            if (lastInvoiceId != null && lastInvoiceId.startsWith("#INV-")) {
                try {
                    // "#INV-" অংশটুকু বাদ দিয়ে শুধু সংখ্যাটি (যেমন: 104) আলাদা করা
                    String numericPart = lastInvoiceId.replace("#INV-", "").trim();
                    int nextNumber = Integer.parseInt(numericPart) + 1; // ১০৪ + ১ = ১০৫
                    
                    // নতুন আইডি ফরম্যাট করা
                    nextInvoiceId = "#INV-" + nextNumber;
                } catch (NumberFormatException e) {
                    // কোনো কারণে পার্স করতে ব্যর্থ হলে সেফটি হিসেবে লিস্টের সাইজের ওপর ভিত্তি করে জেনারেট করা
                    nextInvoiceId = "#INV-" + (100 + productList.size() + 1);
                }
            }
        }
        // 🎯 [অটোমেটিক ইনভয়েস আইডি জেনারেশন লজিক শেষ]

        model.addAttribute("products", productList);
        model.addAttribute("totalValuation", totalValuation);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("estimatedProfit", estimatedProfit);
        model.addAttribute("currentUser", user);
        model.addAttribute("nextInvoiceId", nextInvoiceId); // থাইমলিফ ফ্রন্টএন্ডে পাঠানো হলো
        
        return "dashboard";
    }

    @PostMapping("/add-product")
    public String addNewProduct(@RequestParam String invoiceId,
                                @RequestParam String name,
                                @RequestParam int quantity,
                                @RequestParam double totalPrice,
                                @RequestParam String status,
                                @RequestParam(value = "user", defaultValue = "admin") String username) {
                                        
        Product newProduct = new Product(invoiceId, name, quantity, totalPrice, status);
        productRepository.save(newProduct);
        // রিডাইরেক্টের সময় কারেন্ট ইউজারকে ধরে রাখা
        return "redirect:/dashboard?user=" + username;
    }

    @GetMapping("/products")
    public String showProductsPage(Model model) {
        List<Product> productList = productRepository.findAll();
        model.addAttribute("products", productList);
        return "products";
    }

    @GetMapping("/sales")
    public String showSalesPage(Model model) {
        List<Product> productList = productRepository.findAll();
        double totalValuation = 0;
        for (Product p : productList) {
            totalValuation += p.getTotalPrice();
        }
        double estimatedProfit = totalValuation * 0.20;

        model.addAttribute("products", productList);
        model.addAttribute("totalValuation", totalValuation);
        model.addAttribute("estimatedProfit", estimatedProfit);
        return "sales";
    }

    @GetMapping("/delete-product")
    public String deleteProduct(@RequestParam Long id, @RequestParam(value = "user", defaultValue = "admin") String username) {
        productRepository.deleteById(id);
        return "redirect:/dashboard?user=" + username;
    }

    @GetMapping("/edit-product")
    public String showEditPage(@RequestParam Long id, Model model) {
        Product product = productRepository.findById(id).orElse(null);
        model.addAttribute("product", product);
        return "edit-product";
    }

    @PostMapping("/update-product")
    public String updateProduct(@RequestParam Long id,
                                @RequestParam String invoiceId,
                                @RequestParam String name,
                                @RequestParam int quantity,
                                @RequestParam double totalPrice,
                                @RequestParam String status,
                                @RequestParam(value = "user", defaultValue = "admin") String username) {
                                        
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            product.setInvoiceId(invoiceId);
            product.setName(name);
            product.setQuantity(quantity);
            product.setTotalPrice(totalPrice);
            product.setStatus(status);
            productRepository.save(product);
        }
        return "redirect:/dashboard?user=" + username;
    }

    @GetMapping("/register")
    public String showRegistrationPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerNewUser(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam String fullName,
                                  @RequestParam String role,
                                  Model model) {
                                  
        if (userRepository.findByUsername(username) != null) {
            return "redirect:/register?error=exists";
        }
        
        User newUser = new User(username, password, fullName, role);
        userRepository.save(newUser);
        
        return "redirect:/dashboard?success=user_added";
    }
}