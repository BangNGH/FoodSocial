package com.example.foodsocialproject.repository;

import com.example.foodsocialproject.entity.UserRela;
import com.example.foodsocialproject.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRelaRepository extends JpaRepository<UserRela, UUID> {

    @Query("SELECT COUNT(*) FROM UserRela")
    Long countById(UUID id);

    UserRela findByFollowerAndUser(Users follower, Users user);
}
