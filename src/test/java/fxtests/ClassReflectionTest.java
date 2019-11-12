package fxtests;

import static fxtests.FXTesting.measureTime;
import static utils.ClassReflectionUtils.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import ml.data.CoverageUtils;
import org.junit.Test;
import utils.BaseEntity;
import utils.ClassReflectionUtils;

public class ClassReflectionTest extends AbstractTestExecution {
    @Test
    public void testBaseEntityMethods() {
        List<Class<? extends BaseEntity>> classes = CoverageUtils.getClasses(BaseEntity.class);
        List<? extends BaseEntity> entities = classes.stream().map(ClassReflectionUtils::getInstance)
            .collect(Collectors.toList());
        for (BaseEntity e : entities) {
            getLogger().info("{} SQL = {}", e.getClass(), e.toSQL());
        }
    }

    @Test
    public void testClassReflectionMethods() {
        List<Class<? extends Node>> classes = CoverageUtils.getClasses(Node.class);
        Collections.shuffle(classes);
        Class<?> cl = classes.remove(0);
        String clName = cl.getSimpleName();
        measureTime("getAllMethodsRecursive(" + clName + ")", () -> getAllMethodsRecursive(cl));
        measureTime("hasPublicConstructor(" + clName + ")", () -> hasPublicConstructor(cl));
        measureTime("getNamedArgs(" + clName + ")", () -> getNamedArgs(cl));
        measureTime("getters(" + clName + ")", () -> getters(cl));
        measureTime("isClassPublic(" + clName + ")", () -> isClassPublic(cl));

        Object ob = measureTime("getInstance(" + clName + ")", () -> getInstance(cl));
        measureTime("getDescription(" + clName + "," + ob + ")", () -> getDescription(ob));
        measureTime("getDescriptionMap(" + ob + ")", () -> getDescriptionMap(ob, new HashMap<>()));
        measureTime("getFieldMap(" + ob + "," + clName + ")", () -> getFieldMap(ob, cl));
        measureTime("properties(" + ob + "," + clName + ")", () -> properties(ob, cl));

        List<Method> methods = measureTime("getGetterMethodsRecursive", () -> getGetterMethodsRecursive(cl));
        measureTime("getFieldNameCase", () -> getFieldNameCase(randomItem(methods)));
        measureTime("invoke", () -> invoke(ob, randomItem(methods)));

        Class<?> newClass = getClassWithFields(cl, classes);
        clName = newClass.getSimpleName();
        List<String> fields = measureTime("getFields(" + clName + ")", () -> getFields(newClass));
        String randomField = randomItem(fields);
        measureTime("getFieldValue", () -> getFieldValue(ob, randomField));
        measureTime("getSetter", () -> getSetter(newClass, randomField));
        measureTime("getSetterType", () -> getSetterType(newClass, randomField));
        measureTime("hasField", () -> hasField(newClass, randomField));
        measureTime("hasSetterMethods", () -> hasSetterMethods(newClass, randomField));
        measureTime("invoke", () -> invoke(newClass, randomField));

        List<Class<? extends Object>> testClasses = Arrays.asList(Double.class, String.class, Long.class, Integer.class,
            Boolean.class, Enum.class);
        measureTime("hasClass(" + clName + ")", () -> hasClass(testClasses, cl));
    }

    private static Class<?> getClassWithFields(Class<?> cl, List<Class<? extends Node>> classes) {
        List<String> fields = measureTime("getFields(" + cl.getSimpleName() + ")", () -> getFields(cl));
        Class<?> newClass = cl;
        while (fields.isEmpty()) {
            newClass = classes.remove(0);
            fields = getFields(newClass);
        }
        return newClass;
    }
}
