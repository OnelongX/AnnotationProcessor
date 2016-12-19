package com.ways2u.java.demo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by huanglong on 2016/12/19.
 */
public class Binder {

    public static void bind(AppMain context) {
        try {
            //定义一个固定类名的规则，如AppMain_Binder,AppMain$$Binder
            //然后通过反射加载类
            Class cls = Class.forName(context.getClass().getPackage().getName()+".MealFactory", true, context.getClass().getClassLoader());
            Object object = cls.newInstance();

            Method method = cls.getDeclaredMethod("create", String.class);

            Meal meal = (Meal) method.invoke(object, "MyPizza");

            //代码注入，butterknife view注入就是这样
            //不过它是用代码生成的类型，例如 target.T = (Type)findViewById(id);
            context.meal = meal;
            /*
            xx.setListener(new Listener(){
                void onPrint(Meal meal){
                    context.onPrint(meal)
                }
            });
        */

            System.out.println(meal.getPrice());

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
