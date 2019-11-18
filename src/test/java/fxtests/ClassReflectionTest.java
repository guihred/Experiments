package fxtests;

import static fxtests.FXTesting.measureTime;

import graphs.entities.Linha;
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
import utils.SupplierEx;

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
        measureTime("getAllMethodsRecursive(" + clName + ")", () -> ClassReflectionUtils.getAllMethodsRecursive(cl));
        measureTime("hasPublicConstructor(" + clName + ")", () -> ClassReflectionUtils.hasPublicConstructor(cl));
        measureTime("getNamedArgs(" + clName + ")", () -> ClassReflectionUtils.getNamedArgs(cl));
        measureTime("getters(" + clName + ")", () -> ClassReflectionUtils.getters(cl));
        measureTime("isClassPublic(" + clName + ")", () -> ClassReflectionUtils.isClassPublic(cl));
        List<Class<? extends Object>> testClasses = Arrays.asList(Double.class, String.class, Long.class, Integer.class,
            Boolean.class, Enum.class);
        measureTime("hasClass(" + clName + ")", () -> ClassReflectionUtils.hasClass(testClasses, cl));
        Class<?> newClass2 = getClassInstance(cl, classes);
        clName = newClass2.getSimpleName();
        Object ob = measureTime("getInstance(" + clName + ")", () -> ClassReflectionUtils.getInstance(newClass2));
        measureTime("getDescription(" + clName + "," + ob + ")", () -> ClassReflectionUtils.getDescription(ob));
        measureTime("getDescriptionMap(" + ob + ")", () -> ClassReflectionUtils.getDescriptionMap(ob, new HashMap<>()));
        measureTime("getFieldMap(" + ob + "," + clName + ")", () -> ClassReflectionUtils.getFieldMap(ob, newClass2));
        measureTime("properties(" + ob + "," + clName + ")", () -> ClassReflectionUtils.properties(ob, newClass2));

        List<Method> methods = measureTime("getGetterMethodsRecursive",
            () -> ClassReflectionUtils.getGetterMethodsRecursive(newClass2));
        measureTime("getFieldNameCase", () -> ClassReflectionUtils.getFieldNameCase(randomItem(methods)));
        measureTime("invoke", () -> ClassReflectionUtils.invoke(ob, randomItem(methods)));

        Class<?> newClass = getClassWithFields(newClass2, classes);
        clName = newClass.getSimpleName();
        List<String> fields = measureTime("getFields(" + clName + ")", () -> ClassReflectionUtils.getFields(newClass));
        String randomField = randomItem(fields);
        measureTime("getFieldValue", () -> ClassReflectionUtils.getFieldValue(ob, randomField));
        measureTime("getSetter", () -> ClassReflectionUtils.getSetter(newClass, randomField));
        measureTime("getSetterType", () -> ClassReflectionUtils.getSetterType(newClass, randomField));
        measureTime("hasField", () -> ClassReflectionUtils.hasField(newClass, randomField));
        measureTime("hasSetterMethods", () -> ClassReflectionUtils.hasSetterMethods(newClass, randomField));
        measureTime("invoke", () -> ClassReflectionUtils.invoke(newClass, randomField));

    }

    @Test
    @SuppressWarnings("static-method")
    public void testInvokeClass() {
        List<Class<? >> classes = Arrays.asList(Linha.class);
        List<? > entities = classes.stream().map(ClassReflectionUtils::getInstanceNull)
            .collect(Collectors.toList());
        for (Object e : entities) {
            List<Method> setters = ClassReflectionUtils.setters(e.getClass());
            setters.forEach(s -> ClassReflectionUtils.invoke(e, s, new Object[] { null }));
        }
    }

    private static Class<?> getClassInstance(Class<?> cl, List<Class<? extends Node>> classes) {
        Object fields = SupplierEx.getIgnore(() -> ClassReflectionUtils.getInstance(cl));
        Class<?> newClass = cl;
        while (fields == null) {
            Class<? extends Node> remove = classes.remove(0);
            newClass = remove;
            fields = SupplierEx.getIgnore(() -> ClassReflectionUtils.getInstance(remove));
        }
        return newClass;
    }

    private static Class<?> getClassWithFields(Class<?> cl, List<Class<? extends Node>> classes) {
        List<String> fields = measureTime("getFields(" + cl.getSimpleName() + ")",
            () -> ClassReflectionUtils.getFields(cl));
        Class<?> newClass = cl;
        while (fields.isEmpty()) {
            newClass = classes.remove(0);
            fields = ClassReflectionUtils.getFields(newClass);
        }
        return newClass;
    }
}
