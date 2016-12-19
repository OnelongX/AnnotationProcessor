package com.ways2u.java.demo;

import com.ways2u.java.annotation.Factory;

/**
 * Created by huanglong on 2016/12/19.
 */
@Factory(
        type = Meal.class,
        id = "MyPizza"
)
//优化过的MyPizza 贵一点30
//修改了，要重新build，不然有时候不能全量编译
public class MyPizza extends Pizza {

    @Override
    public float getPrice() {
        return 30;
    }
}
