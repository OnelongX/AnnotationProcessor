package com.ways2u.java.processor;

import com.google.auto.service.AutoService;
import com.ways2u.java.annotation.Factory;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * Created by huanglong on 2016/12/19.
 */
@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Map<String, FactoryGroupedClasses> factoryClasses =
            new LinkedHashMap<String, FactoryGroupedClasses>();


    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        typeUtils = env.getTypeUtils();
        elementUtils = env.getElementUtils();
        filer = env.getFiler();
        messager = env.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //得到注解类型，如com.ways2u.java.annotation.Factory
        //for (TypeElement typeElement : annotations) {
          //  warn(null, typeElement.toString());
        //}
        //在线程池里面处理的
        //warn(null, Thread.currentThread().getName());
        try {

            for (Element element : roundEnv.getElementsAnnotatedWith(Factory.class)) {
                //标有注解的类，如com.ways2u.java.demo.Pizza
                //warn(element, element.toString());
                //warn(element,element.getQualifiedName().toString());

                if (element.getKind() != ElementKind.CLASS) {
                    throw new ProcessingException(element, "Only classes can be annotated with @%s",
                            Factory.class.getSimpleName());
                }

                TypeElement typeElement = (TypeElement) element;

                FactoryAnnotatedClass annotatedClass = new FactoryAnnotatedClass(typeElement);
                checkValidClass(annotatedClass);

                // Everything is fine, so try to add
                FactoryGroupedClasses factoryClass =
                        factoryClasses.get(annotatedClass.getQualifiedFactoryGroupName());
                if (factoryClass == null) {
                    String qualifiedGroupName = annotatedClass.getQualifiedFactoryGroupName();
                    factoryClass = new FactoryGroupedClasses(qualifiedGroupName);
                    factoryClasses.put(qualifiedGroupName, factoryClass);
                }

                // Checks if id is conflicting with another @Factory annotated class with the same id
                factoryClass.add(annotatedClass);
            }

            // Generate code
            for (FactoryGroupedClasses factoryClass : factoryClasses.values()) {
                factoryClass.generateCode(elementUtils, filer);
            }
            factoryClasses.clear();

        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        } catch (IOException e) {
            error(null, e.getMessage());
        }
        return false;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<String>();
        set.add(Factory.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }



    /**
     * Checks if the annotated element observes our rules
     */
    private void checkValidClass(FactoryAnnotatedClass item) throws ProcessingException {

        // Cast to TypeElement, has more type specific methods
        TypeElement classElement = item.getTypeElement();

        //类要 PUBLIC
        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ProcessingException(classElement, "The class %s is not public.",
                    classElement.getQualifiedName().toString());
        }

        // Check if it's an abstract class
        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new ProcessingException(classElement,
                    "The class %s is abstract. You can't annotate abstract classes with @%",
                    classElement.getQualifiedName().toString(), Factory.class.getSimpleName());
        }

        // Check inheritance: Class must be childclass as specified in @Factory.type();
        TypeElement superClassElement =
                elementUtils.getTypeElement(item.getQualifiedFactoryGroupName());

        if (superClassElement.getKind() == ElementKind.INTERFACE) {

            // Check interface implemented
            if (!classElement.getInterfaces().contains(superClassElement.asType())) {

                //处理继承关系， 例如MyPizza extends Pizza 而 Pizza implements Meal
                TypeElement currentClass = classElement;
                while (true) {
                    TypeMirror superClassType = currentClass.getSuperclass();

                    currentClass = (TypeElement) typeUtils.asElement(superClassType);

                    if (currentClass!=null && currentClass.getInterfaces().contains(superClassElement.asType())) {
                        // Required super class found
                        break;
                    }

                    if (currentClass==null || superClassType.getKind() == TypeKind.NONE) {
                        throw new ProcessingException(classElement,
                                "The class %s annotated with @%s must implement the interface %s",
                                classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                                item.getQualifiedFactoryGroupName());
                    }


                }



            }
        } else {
            // Check subclassing
            TypeElement currentClass = classElement;
            while (true) {
                TypeMirror superClassType = currentClass.getSuperclass();

                if (superClassType.getKind() == TypeKind.NONE) {
                    // Basis class (java.lang.Object) reached, so exit
                    throw new ProcessingException(classElement,
                            "The class %s annotated with @%s must inherit from %s",
                            classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                            item.getQualifiedFactoryGroupName());
                }

                if (superClassType.toString().equals(item.getQualifiedFactoryGroupName())) {
                    // Required super class found
                    break;
                }

                // Moving up in inheritance tree
                currentClass = (TypeElement) typeUtils.asElement(superClassType);
            }
        }

        // Check if an empty public constructor is given
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers()
                        .contains(Modifier.PUBLIC)) {
                    // Found an empty constructor
                    return;
                }
            }
        }

        // No empty constructor found
        throw new ProcessingException(classElement,
                "The class %s must provide an public empty default constructor",
                classElement.getQualifiedName().toString());
    }


    /**
     * Prints an error message  只能用这用方式打印
     *
     * @param e   The element which has caused the error. Can be null
     * @param msg The error message
     */
    public void error(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    public void warn(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.WARNING, msg, e);
    }

}
