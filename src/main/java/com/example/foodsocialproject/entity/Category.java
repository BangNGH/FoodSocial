package com.example.foodsocialproject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên loại không được để trống")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @OneToMany(mappedBy = "category")
    private List<Product> products;
}
