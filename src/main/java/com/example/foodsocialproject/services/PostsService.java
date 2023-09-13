package com.example.foodsocialproject.services;

import com.example.foodsocialproject.entity.Ingredients;
import com.example.foodsocialproject.entity.Posts;
import com.example.foodsocialproject.entity.Users;
import com.example.foodsocialproject.exception.ResourceNotFoundException;
import com.example.foodsocialproject.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostsService implements TableService{
    private final PostsRepository postsRepository;

    @Override
    public List<Posts> getList() {
        return postsRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        Long count = postsRepository.countPostsById(id);
        if (count == null || count == 0) {
            throw new ResourceNotFoundException("Không tìm thấy ID: " + id);
        }
        postsRepository.deleteById(id);
    }

    @Override
    public Optional get(UUID id) {
        Optional<Posts> result = postsRepository.findById(id);
        if (result.isPresent()){
            return result;
        }
        else
            throw new ResourceNotFoundException("Không tìm ID: " + id + "!");
    }

    public Posts save(Posts recipe) {
        postsRepository.save(recipe);
        return recipe;
    }

    public Posts findByID(UUID id) {
        return postsRepository.findByID(id);
    }

    public List<Posts> search(String q) {
        if (q != null) {
            List<Ingredients> ingredientsList = postsRepository.searchByIngredients(q);
            List<Posts> postFound = postsRepository.search(q);

            List<Posts> posts= new ArrayList<>();
            List<Posts> totalFound= new ArrayList<>();
            for (Ingredients ingredients : ingredientsList
                 ) {
               if (!posts.contains(ingredients.getRecipe())){
                   posts.add(ingredients.getRecipe());
               }
            }
            for (int i = 0; i < postFound.size(); i++) {
                if (!totalFound.contains(postFound.get(i))){
                    totalFound.add(postFound.get(i));
                }
            }
            for (int i = 0; i < posts.size(); i++) {
                if (!totalFound.contains(posts.get(i))){
                    totalFound.add(posts.get(i));
                }
            }
            return totalFound;
        }
        List<Posts> posts = this.getList();
        List<Posts> randomPosts = getRandomPosts(posts, 3);


        return randomPosts;
    }
    private List<Posts> getRandomPosts(List<Posts> sourceList, int n) {
        List<Posts> randomList = new ArrayList<>(sourceList);
        Collections.shuffle(randomList, new Random());

        // Lấy n phần tử đầu tiên sau khi xáo trộn
        if (n <= randomList.size()) {
            return randomList.subList(0, n);
        } else {
            // Tránh trường hợp n lớn hơn kích thước danh sách
            return randomList;
        }
    }
    public List<Posts> get5PostsTopLike() {
        return this.postsRepository.get3PostsTopLike();
    }

    public List<Users> getTop5User() {
        return this.postsRepository.getTop5User();
    }
}
