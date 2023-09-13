package com.example.foodsocialproject.repository;

import com.example.foodsocialproject.entity.Ingredients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface IngredientRepository extends JpaRepository<Ingredients, UUID> {

    Long countById(UUID id);
}
