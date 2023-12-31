package com.example.foodsocialproject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
    @JsonIgnore
    private List<Product> products;
}
