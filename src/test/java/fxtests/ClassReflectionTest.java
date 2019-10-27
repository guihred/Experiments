package fxtests;

import static fxtests.FXTesting.measureTime;
import static utils.ClassReflectionUtils.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import org.junit.Test;
import utils.BaseEntity;
import utils.ClassReflectionUtils;

public class ClassReflectionTest extends AbstractTestExecution {
    @Test
    public void testBaseEntityMethods() {
        List<Class<? extends BaseEntity>> classes = FXTesting.getClasses(BaseEntity.class);
        List<? extends BaseEntity> entities = classes.stream().map(ClassReflectionUtils::getInstance)
            .collect(Collectors.toList());
        for (BaseEntity e : entities) {
            getLogger().info("{} SQL = {}", e.getClass(), e.toSQL());
        }
    }

    @Test
    public void testClassReflectionMethods() {
        List<Class<? extends Node>> classes = FXTesting.getClasses(Node.class);
        Class<?> cl = randomItem(classes);
        measureTime("getAllMethodsRecursive", () -> getAllMethodsRecursive(cl));
        measureTime("hasPublicConstructor", () -> hasPublicConstructor(cl));
        measureTime("getNamedArgs", () -> getNamedArgs(cl));
        measureTime("getters", () -> getters(cl));
        measureTime("isClassPublic", () -> isClassPublic(cl));

        Object ob = measureTime("getInstance", () -> getInstance(cl));
        measureTime("getDescription", () -> getDescription(ob));
        measureTime("getDescriptionMap", () -> getDescriptionMap(ob, new HashMap<>()));
        measureTime("getFieldMap", () -> getFieldMap(ob, cl));
        measureTime("properties", () -> properties(ob, cl));

        List<Method> methods = measureTime("getGetterMethodsRecursive", () -> getGetterMethodsRecursive(cl));
        measureTime("getFieldNameCase", () -> getFieldNameCase(randomItem(methods)));
        measureTime("invoke", () -> invoke(ob, randomItem(methods)));

        List<String> fields = measureTime("getFields", () -> getFields(cl));
        measureTime("getFieldValue", () -> getFieldValue(ob, randomItem(fields)));
        measureTime("getSetter", () -> getSetter(cl, randomItem(fields)));
        measureTime("getSetterType", () -> getSetterType(cl, randomItem(fields)));
        measureTime("hasField", () -> hasField(cl, randomItem(fields)));
        measureTime("hasSetterMethods", () -> hasSetterMethods(cl, randomItem(fields)));
        measureTime("invoke", () -> invoke(cl, randomItem(fields)));

        List<Class<? extends Object>> testClasses = Arrays.asList(Double.class, String.class, Long.class, Integer.class,
            Boolean.class, Enum.class);
        measureTime("hasClass", () -> hasClass(testClasses, cl));
    }
}
