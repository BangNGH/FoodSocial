package com.example.foodsocialproject.services;

import com.example.foodsocialproject.entity.event_ingredient;
import com.example.foodsocialproject.repository.event_ingredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class event_ingredientService {
    private final event_ingredientRepository eventIngredientRepository;

    public void save(event_ingredient newRela) {
        eventIngredientRepository.save(newRela);
    }
}
