package com.example.foodsocialproject.repository;

import com.example.foodsocialproject.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, UUID> {
    @Query("SELECT COUNT(*) FROM UserInfo")
    Long countById(UUID id);
}