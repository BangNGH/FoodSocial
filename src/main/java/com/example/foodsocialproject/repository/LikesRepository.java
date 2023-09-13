package com.example.foodsocialproject.repository;

import com.example.foodsocialproject.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LikesRepository  extends JpaRepository<Likes, UUID> {

    @Modifying
    @Query("DELETE FROM Likes a WHERE a.id = :id")
    void deleteById(@Param("id") UUID id);

    @Query("SELECT COUNT(*) FROM Likes")
    Long countById(UUID id);
}