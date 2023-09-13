package com.example.foodsocialproject.repository;

import com.example.foodsocialproject.entity.Ingredients;
import com.example.foodsocialproject.entity.Posts;
import com.example.foodsocialproject.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostsRepository extends JpaRepository<Posts, UUID> {

    @Query("SELECT COUNT(*) FROM Posts")
    Long countPostsById(UUID userId);

    @Query("SELECT p FROM Posts p WHERE p.id =:id")
    Posts findByID(UUID id);

    @Query("SELECT p FROM Ingredients p WHERE p.ingredientName LIKE %?1%")
    List<Ingredients> searchByIngredients(String q);
      @Query("SELECT p FROM Posts p WHERE p.foodName LIKE %?1%")
    List<Posts> search(String q);

    @Query("SELECT p FROM Posts p order by p.likeCount desc limit 3")
    List<Posts> get3PostsTopLike();

    @Query("SELECT u FROM Users u left join Posts p on u.id = p.user.id group by u.id, u.email order by SUM(p.likeCount) desc limit 5")
    List<Users> getTop5User();
}
