package com.example.foodsocialproject.services;

import com.example.foodsocialproject.entity.Likes;
import com.example.foodsocialproject.exception.ResourceNotFoundException;
import com.example.foodsocialproject.repository.LikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikesService implements TableService{
    private final LikesRepository likesRepository;
    @Override
    public List<Likes> getList() {
        return likesRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        likesRepository.deleteById(id);
    }

    @Override
    public Optional get(UUID id) {
        Optional<Likes> result = likesRepository.findById(id);
        if (result.isPresent()){
            return result;
        }
        else
            throw new ResourceNotFoundException("Không tìm ID: " + id + "!");
    }

    public void save(Likes postLike) {
        likesRepository.save(postLike);
    }
}
