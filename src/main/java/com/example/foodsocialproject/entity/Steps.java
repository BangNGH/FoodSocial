package com.example.foodsocialproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "Steps")
@AllArgsConstructor
@NoArgsConstructor
public class Steps {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "step_number")
    private int stepNumber;

    @Column(name = "step_description")
    private String stepDescription;

    @Column(name = "image")
    private String stepImg;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipe_id", referencedColumnName = "id")
    private Posts recipe;

    @Override
    public String toString() {
        return "Steps{" +
                "id=" + id +
                ", stepNumber=" + stepNumber +
                ", stepDescription='" + stepDescription + '\'' +
                ", stepImg='" + stepImg + '\'' +
                ", recipe=" + recipe +
                '}';
    }

    @Transient
    public String getStepImagePath() {
        if (id == null||stepImg==null) {
            return null;
        }
        return stepImg;
    }
}
