package com.stock.management;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // ডাটাবেজে কোনো ইউজার অলরেডি আছে কিনা চেক করা হচ্ছে
        long userCount = userRepository.count();
        
        if (userCount == 0) {
            // আপনার User এনটিটি ক্লাসের কন্সট্রাক্টর বা সেটার অনুযায়ী অবজেক্ট তৈরি
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // স্প্রিং সিকিউরিটি থাকলে এনক্রিপ্ট করতে হবে, না থাকলে এভাবে ঠিক আছে
            admin.setFullName("Tasrif");
            admin.setRole("ADMIN");
            
            userRepository.save(admin);
            System.out.println(">>>>> [SUCCESS] Default admin user created! Username: admin, Password: admin123 <<<<<");
        } else {
            System.out.println(">>>>> Users already exist in database. Skipping initialization. <<<<<");
        }
    }
}