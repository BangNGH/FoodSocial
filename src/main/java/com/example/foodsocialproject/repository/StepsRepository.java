package com.example.foodsocialproject.repository;

import com.example.foodsocialproject.entity.Steps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface StepsRepository extends JpaRepository<Steps, UUID> {

    Long countById(UUID id);
}
