package com.example.foodsocialproject.repository;

import com.example.foodsocialproject.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p from Product p WHERE p.category.id = ?1")
    List<Product> getAllProductById(Long id);
    @Query("SELECT p from Product p where p.name like %?1%")
    List<Product> searchProductByName(String keyword);
}
