package com.stock.management;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // ইউজারনেম দিয়ে ডাটাবেজে ইউজার খোঁজার মেথড
    User findByUsername(String username);
}