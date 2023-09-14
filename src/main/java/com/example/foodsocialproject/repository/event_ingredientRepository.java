package com.example.foodsocialproject.repository;

import com.example.foodsocialproject.entity.event_ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface event_ingredientRepository extends JpaRepository<event_ingredient, UUID> {
}
