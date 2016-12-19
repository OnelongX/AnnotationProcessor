package com.ways2u.java.demo;

import com.ways2u.java.annotation.Factory;

/**
 * Created by huanglong on 2016/12/19.
 */
//普通pizza 25
@Factory(
        type = Meal.class,
        id = "Pizza"
)
public  class Pizza implements Meal {
    @Override
    public float getPrice() {
        return 25f;
    }
}
