package com.example.foodsocialproject.repository;

import com.example.foodsocialproject.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {
    @Query("SELECT COUNT(*) FROM Users")
    Long countById(UUID id);

    Optional<Users> findByEmail(String email);

    @Query("SELECT p FROM Users p WHERE p.email LIKE %?1% or p.fullName LIKE %?1%")
    List<Users> search(String q);
}