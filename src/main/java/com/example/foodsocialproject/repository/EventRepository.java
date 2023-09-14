package com.example.foodsocialproject.repository;

import com.example.foodsocialproject.entity.Comment;
import com.example.foodsocialproject.entity.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface EventRepository extends JpaRepository<Events, UUID> {

    Long countById(UUID id);
}