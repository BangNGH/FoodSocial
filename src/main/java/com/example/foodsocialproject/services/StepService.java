package com.example.foodsocialproject.services;

import com.example.foodsocialproject.entity.Steps;
import com.example.foodsocialproject.exception.ResourceNotFoundException;
import com.example.foodsocialproject.repository.StepsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StepService implements TableService{
    private final StepsRepository stepsRepository;

    @Override
    public List<Steps> getList() {
        return stepsRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        Long count = stepsRepository.countById(id);
        if (count == null || count == 0) {
            throw new ResourceNotFoundException("Không tìm thấy ID: " + id);
        }
        stepsRepository.deleteById(id);
    }

    @Override
    public Optional get(UUID id) {
        Optional<Steps> result = stepsRepository.findById(id);
        if (result.isPresent()){
            return result;
        }
        else
            throw new ResourceNotFoundException("Không tìm ID: " + id + "!");
    }
}
