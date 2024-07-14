package com.petshop.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petshop.common.constant.Calories;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Date;
import java.util.List;


@Entity
@Table(name = "Food")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long food_id;

    private String food_name;
    private Integer date_expired;
    private Integer calories_per_unit;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Calories unit;
    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "food",fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<FoodItem> foodItem;
    @JsonIgnore
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "food_category_id")
    private FoodCategory foodCategory;
    @Transient
    public Long getFood_category_id(){
        return foodCategory != null ? foodCategory.getFood_category_id() : null;
    }
}
