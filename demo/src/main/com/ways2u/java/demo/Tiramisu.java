package com.ways2u.java.demo;

import com.ways2u.java.annotation.Factory;

/**
 * Created by huanglong on 2016/12/19.
 */
@Factory(
        type = Meal.class,
        id = "Tiramisu"
)
public class Tiramisu implements Meal {
    @Override
    public float getPrice() {
        return 23f;
    }
}
