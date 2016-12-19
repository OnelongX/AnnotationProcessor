package com.ways2u.java.processor;


import com.ways2u.java.annotation.Factory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;


public class FactoryAnnotatedClass {
    /*添加了注解的类，@Factory的类*/
    private TypeElement annotatedClassElement;
    /*type接口的类名，通常是抽象类或者接口类*/
    private String qualifiedGroupClassName;
    /*简单的名字*/
    private String simpleFactoryGroupName;
    /*bean id*/
    private String id;

    /**
     * @throws ProcessingException if id() from annotation is null
     */
    public FactoryAnnotatedClass(TypeElement classElement) throws ProcessingException {
        this.annotatedClassElement = classElement;
        Factory annotation = classElement.getAnnotation(Factory.class);
        id = annotation.id();

        if (id == null || id.trim().length() == 0) {
            throw new ProcessingException(classElement,
                    "id() in @%s for class %s is null or empty! that's not allowed",
                    Factory.class.getSimpleName(), classElement.getQualifiedName().toString());
        }




        // Get the full QualifiedTypeName

        try {
            Class<?> clazz = annotation.type();
            //类的全名
            qualifiedGroupClassName = clazz.getCanonicalName();
            simpleFactoryGroupName = clazz.getSimpleName();
        } catch (MirroredTypeException mte) { //这里不懂,好像是类继承会有这个异常
            //例如 MyPizza extends Pizza
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedGroupClassName = classTypeElement.getQualifiedName().toString();
            simpleFactoryGroupName = classTypeElement.getSimpleName().toString();
        }

    }

    /**
     * Get the id as specified in {@link Factory#id()}.
     * return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the full qualified name of the type specified in  {@link Factory#type()}.
     *
     * @return qualified name
     */
    public String getQualifiedFactoryGroupName() {
        return qualifiedGroupClassName;
    }

    /**
     * Get the simple name of the type specified in  {@link Factory#type()}.
     *
     * @return qualified name
     */
    public String getSimpleFactoryGroupName() {
        return simpleFactoryGroupName;
    }

    /**
     * The original element that was annotated with @Factory
     */
    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }
}
