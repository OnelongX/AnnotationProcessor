package com.ways2u.java.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
//无论是不是CLASS，都不影响FactoryProcessor处理，
// 测试过RUNTIME和SOURCE，一样能生成代码。
@Retention(RetentionPolicy.CLASS)
public @interface Factory {

  /**
   * 工厂的名字
   */
  Class type();

  /**
   * 用来表示生成哪个对象的唯一id
   */
  String id();
}