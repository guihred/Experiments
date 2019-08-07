package utils;

import japstudy.db.BaseEntity;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public final class ClassReflectionUtils {
    private static final Logger LOG = HasLogging.log();
    private static final String METHOD_REGEX = "is(\\w+)|get(\\w+)";

    private ClassReflectionUtils() {
        LOG.error("");
    }

    public static void displayCSSStyler(Scene scene, String pathname) {
        ClassReflectionUtils.displayStyleClass(scene.getRoot());
        StageHelper.displayCSSStyler(scene, pathname);
    }

    public static void displayStyleClass(Node node) {
        displayStyleClass("", node);
    }

    public static String getDescription(Object i) {
        if (i == null) {
            return null;
        }
        return getDescription(i, i.getClass(), new HashMap<>(), new HashSet<>(), new HashMap<>());

    }

    public static String getDescription(Object obj, Map<Class<?>, FunctionEx<Object, String>> toStringMap) {
        return getDescription(obj, obj.getClass(), toStringMap, new HashSet<>(), new HashMap<>());

    }

    public static <T> String getDescription(T obj, Class<?> class1,
        Map<Class<?>, FunctionEx<Object, String>> toStringMap, Set<Object> invokedObjects,
        Map<Class<?>, List<Method>> getterMethods) {
        if (!invokedObjects.contains(obj)) {
            invokedObjects.add(obj);
        } else {
            return "";
        }
        List<Method> infoMethod = getGetterMethods(class1, getterMethods);
        StringBuilder description = new StringBuilder("\n");
        infoMethod.forEach(ConsumerEx.makeConsumer((Method method) -> {
            Object invoke = method.invoke(obj);
            // To Avoid infinite loop
            if (isRecursiveCall(class1, invoke)) {
                return;
            }
            String fieldName = getFieldName(method);
            description.append("\t");
            description.append(fieldName);
            description.append(" = ");
            if (invoke != null && toStringMap.containsKey(invoke.getClass())) {
                description.append(FunctionEx.makeFunction(toStringMap.get(invoke.getClass())).apply(invoke));
            } else if (invoke instanceof Enumeration) {
                Enumeration<?> invoke2 = (Enumeration<?>) invoke;
                description
                    .append(getEnumerationDescription(fieldName, invoke2, toStringMap, invokedObjects, getterMethods));
            } else {
                description.append(invoke);
            }
            description.append("\n");
        }));
        return description.toString();
    }

    public static Map<String, String> getDescriptionMap(Object obj,
        Map<Class<?>, FunctionEx<Object, String>> toStringMap) {
        return getDescriptionMap(obj, obj.getClass(), toStringMap, new HashSet<>(), new HashMap<>(), new HashMap<>());

    }

    public static <T> Map<String, String> getDescriptionMap(T obj, Class<?> objClass,
        Map<Class<?>, FunctionEx<Object, String>> toStringMap, Set<Object> invokedObjects,
        Map<Class<?>, List<Method>> getterMethods, Map<String, String> descriptionMap) {
        if (invokedObjects.contains(obj)) {
            return descriptionMap;
        }
        invokedObjects.add(obj);
        List<Method> infoMethod = getGetterMethods(objClass, getterMethods);
        infoMethod.forEach(ConsumerEx.makeConsumer((Method o) -> {
            Object invoke = o.invoke(obj);
            if (invoke instanceof Enumeration && invoke.getClass().getGenericInterfaces().length > 0) {
                Type type = invoke.getClass().getGenericInterfaces()[0];
                if (type.getTypeName().contains(objClass.getName())) {
                    return;
                }
            }
            String fieldName = getFieldName(o);
            StringBuilder description = new StringBuilder("\n");
            if (invoke != null && toStringMap.containsKey(invoke.getClass())) {
                description.append(FunctionEx.makeFunction(toStringMap.get(invoke.getClass())).apply(invoke));
            } else if (invoke instanceof Enumeration) {
                description.append(getEnumerationDescription(fieldName, (Enumeration<?>) invoke, toStringMap,
                    invokedObjects, getterMethods));
            } else {
                description.append(invoke);
            }
            descriptionMap.put(fieldName, description.toString());
        }));
        return descriptionMap;
    }

    public static String getFieldName(Member t) {
        return t.getName().replaceAll(METHOD_REGEX, "$1$2");
    }

    public static String getFieldNameCase(Member t) {
        String fieldName = getFieldName(t);
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }

    public static List<String> getFields(Class<?> class1) {
        return Stream.of(class1.getDeclaredFields()).filter(m -> !Modifier.isStatic(m.getModifiers()))
            .map(Field::getName).collect(Collectors.toList());
    }

    public static List<String> getFieldsRecursive(Class<?> class1) {
        Class<?> a = class1;

        List<String> fields = getFields(a);

        for (int i = 0; i < 4; i++) {
            fields.addAll(getFields(a.getSuperclass()));
            a = a.getSuperclass();
            if (a == Object.class) {
                break;
            }
        }

        return fields;
    }

    public static Object getFieldValue(Object ob, String name) {
        return Stream.of(ob.getClass().getDeclaredFields()).filter(m -> !Modifier.isStatic(m.getModifiers()))
            .filter(e -> e.getName().equals(name)).findFirst()
            .map(field -> BaseEntity.getFieldValue(ob, field)).orElse(null);
    }

    public static List<Method> getGetterMethods(Class<?> targetClass) {
        return getGetterMethods(targetClass, new HashMap<>());
    }

    public static Object invoke(Object ob, Method method) {
        try {
            return method.invoke(ob);
        } catch (Exception e) {
            LOG.trace("", e);
            return null;
        }
    }

    public static Object invoke(Object ob, String method) {
        try {

            return getGetterMethods(ob.getClass()).stream()
                .filter(e -> StringUtils.containsIgnoreCase(e.getName(), method)).findFirst()
                .map(FunctionEx.makeFunction(m -> m.invoke(ob))).orElse(null);
        } catch (Exception e) {
            LOG.trace("", e);
            return null;
        }
    }

    public static Object mapProperty(Object e) {
        if (e instanceof Property<?>) {
            return ((Property<?>) e).getValue();
        }
        return e;
    }


    private static void displayStyleClass(String n, Node node) {
        String arg1 = n + node.getClass().getSimpleName();
        if (node instanceof Labeled) {
            HasLogging.log(1).info("{} = {} = \"{}\"", arg1, node.getStyleClass(), ((Labeled) node).getText());
        }

        String id = node.getId();
        if (id != null) {
            HasLogging.log(1).info("{} = #{}.{}", arg1, id, node.getStyleClass());
        } else {
            HasLogging.log(1).info("{} = .{}", arg1, node.getStyleClass());
        }
        if (node instanceof Parent) {
            ObservableList<Node> childrenUnmodifiable = ((Parent) node).getChildrenUnmodifiable();
            childrenUnmodifiable.forEach(t -> ClassReflectionUtils.displayStyleClass(n + "-", t));
        }
    }

    private static <T> String getEnumerationDescription(String fieldName, Enumeration<T> enumeration,
        Map<Class<?>, FunctionEx<Object, String>> toStringMap, Set<Object> invoked,
        Map<Class<?>, List<Method>> getterMethods) {
        StringBuilder descriptionBuilder = new StringBuilder("{\n");
        int i = 0;
        while (enumeration.hasMoreElements()) {
            T element = enumeration.nextElement();
            if (toStringMap.containsKey(element.getClass())
                || toStringMap.keySet().stream().anyMatch(e -> e.isAssignableFrom(element.getClass()))) {
                Class<?> orElse = toStringMap.keySet().stream().filter(e -> e.isAssignableFrom(element.getClass()))
                    .findAny().orElse(element.getClass());

                String apply = FunctionEx.makeFunction(toStringMap.get(orElse)).apply(element);
                descriptionBuilder.append(apply);
                descriptionBuilder.append("\n");
                continue;
            }

            descriptionBuilder.append("\t\t");
            descriptionBuilder.append(fieldName);
            descriptionBuilder.append(" ");
            descriptionBuilder.append(i++);
            descriptionBuilder.append(" = {");
            String description = getDescription(element, element.getClass(), toStringMap, invoked, getterMethods);
            if (!description.isEmpty()) {
                descriptionBuilder.append("\t\t\t");
                descriptionBuilder.append(description.replaceAll("\t", "\t\t\t"));
            }
            descriptionBuilder.append("\t\t}\n");
        }
        descriptionBuilder.append("\t}");
        return descriptionBuilder.toString();
    }

    private static List<Method> getGetterMethods(Class<?> class1, Map<Class<?>, List<Method>> getterMethods) {
        return getterMethods.computeIfAbsent(class1, ClassReflectionUtils::getters);

    }

    private static List<Method> getters(Class<?> c) {
        return Stream.of(c.getDeclaredMethods()).filter(m -> Modifier.isPublic(m.getModifiers()))
            .filter(m -> m.getName().matches(METHOD_REGEX)).filter(m -> m.getParameterCount() == 0)
            .sorted(Comparator.comparing(ClassReflectionUtils::getFieldName)).collect(Collectors.toList());
    }

    private static boolean isRecursiveCall(Class<?> class1, Object invoke) {
        return invoke instanceof Enumeration && invoke.getClass().getGenericInterfaces().length > 0
            && invoke.getClass().getGenericInterfaces()[0].getTypeName().contains(class1.getName());
    }
}
