package com.example.foodsocialproject.repository;


import com.example.foodsocialproject.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    @Query("SELECT o from Orders o WHERE o.user.id = ?1")
    List<Orders> findAllOrderByUserId(Long userId);

    @Query("SELECT p FROM Orders p WHERE p.user.id = ?1 AND p.is_paid = true")
    Page<Orders> getOrdersByUserId(UUID id, Pageable pageable);

}
