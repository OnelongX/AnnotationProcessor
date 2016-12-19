package com.ways2u.java.demo;

import com.ways2u.java.annotation.Factory;

/**
 * Created by huanglong on 2016/12/19.
 */

public class AppMain {

    Meal meal;

    void onPrint(Meal meal)
    {
        System.out.println(meal.getPrice());
    }


    public static void main(String[] agvs){
        //工厂类是注解处理器生成的
        //MealFactory factory = new MealFactory();

        //System.out.println(factory.create("MyPizza").getPrice());
        //System.out.println(factory.create("Pizza").getPrice());
        //System.out.println(factory.create("Tiramisu").getPrice());

        Binder.bind(new AppMain());
    }
}
