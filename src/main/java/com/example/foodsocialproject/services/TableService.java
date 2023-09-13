package com.example.foodsocialproject.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TableService {

    List<?> getList();


   void delete(UUID id);

   Optional get(UUID id);

}
