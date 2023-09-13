package com.example.foodsocialproject.repository;

import com.example.foodsocialproject.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

//@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    @Query("SELECT p FROM VerificationToken p WHERE p.token = ?1 ")
    VerificationToken findByToken(String token);
}
