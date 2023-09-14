package com.example.foodsocialproject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;
    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 1000)
    private String description;
    @Positive(message = "Giá sản phẩm phải lớn hơn 0")
    private double price;
    @Positive(message = "Số lượng kho phải lớn hơn 0")
    private float quantity_in_stock;
    private int discount;
    @Column(columnDefinition = "BIT(1)")
    private Boolean active;
    private String img;
    public String getImagesPath(){
        if(img == null || id == null) return null;
        return "/product-images/" + id + "/" + img;
    }
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @OneToOne(mappedBy = "productid", cascade = CascadeType.ALL)
    private event_ingredient event_ingredient;

}
