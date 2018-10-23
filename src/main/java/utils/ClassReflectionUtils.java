package utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ClassReflectionUtils {
    private static final String METHOD_REGEX = "is(\\w+)|get(\\w+)";

    private ClassReflectionUtils() {
    }

    public static String getDescription(Object i) {
        Set<Object> invoked = new HashSet<>();
        Map<Class<?>, List<Method>> getterMethods = new HashMap<>();
        Map<Class<?>, FunctionEx<Object, String>> toStringMap = new HashMap<>();
        return getDescription(invoked, getterMethods, i.getClass(), i, toStringMap);

    }

    public static String getDescription(Object i, Map<Class<?>, FunctionEx<Object, String>> toStringMap) {
        Set<Object> invoked = new HashSet<>();
        Map<Class<?>, List<Method>> getterMethods = new HashMap<>();
        return getDescription(invoked, getterMethods, i.getClass(), i, toStringMap);

    }

    public static Map<String, String> getDescriptionMap(Object i,
            Map<Class<?>, FunctionEx<Object, String>> toStringMap) {
        Set<Object> invoked = new HashSet<>();
        Map<Class<?>, List<Method>> getterMethods = new HashMap<>();

        Map<String, String> map = new HashMap<>();
        return getDescriptionMap(invoked, getterMethods, i.getClass(), i, toStringMap, map);

    }

    public static <T> String getDescription(Set<Object> invoked, Map<Class<?>, List<Method>> getterMethods,

            Class<?> class1, T i, Map<Class<?>, FunctionEx<Object, String>> toStringMap) {
        if (!invoked.contains(i)) {
            invoked.add(i);
        } else {
            return "";
        }
        List<Method> infoMethod = getGetterMethods(getterMethods, class1);
        StringBuilder description = new StringBuilder("\n");
        infoMethod.forEach(ConsumerEx.makeConsumer((Method o) -> {
            Object invoke = o.invoke(i);
            if (invoke instanceof Enumeration && invoke.getClass().getGenericInterfaces().length > 0) {
                Type type = invoke.getClass().getGenericInterfaces()[0];
                if (type.getTypeName().contains(class1.getName())) {
                    return;
                }
            }
            String fieldName = o.getName().replaceAll(METHOD_REGEX, "$1$2");

            description.append("\t");
            description.append(fieldName);
            description.append(" = ");
            if (invoke != null && toStringMap.containsKey(invoke.getClass())) {
                description.append(FunctionEx.makeFunction(toStringMap.get(invoke.getClass())).apply(invoke));
            } else if (invoke instanceof Enumeration) {
                Enumeration<?> invoke2 = (Enumeration<?>) invoke;
                description.append(getEnumerationDescription(invoked, getterMethods, fieldName, invoke2, toStringMap));
            } else {
                description.append(invoke);
            }
            description.append("\n");
        }));
        return description.toString();
    }

    public static <T> Map<String, String> getDescriptionMap(Set<Object> invoked,
            Map<Class<?>, List<Method>> getterMethods,
            Class<?> class1, T i, Map<Class<?>, FunctionEx<Object, String>> toStringMap, Map<String, String> map) {
        if (!invoked.contains(i)) {
            invoked.add(i);
        } else {
            return map;
        }
        List<Method> infoMethod = getGetterMethods(getterMethods, class1);
        infoMethod.forEach(ConsumerEx.makeConsumer((Method o) -> {
            Object invoke = o.invoke(i);
            if (invoke instanceof Enumeration && invoke.getClass().getGenericInterfaces().length > 0) {
                Type type = invoke.getClass().getGenericInterfaces()[0];
                if (type.getTypeName().contains(class1.getName())) {
                    return;
                }
            }
            String fieldName = o.getName().replaceAll(METHOD_REGEX, "$1$2");
            StringBuilder description = new StringBuilder("\n");
            if (invoke != null && toStringMap.containsKey(invoke.getClass())) {
                description.append(FunctionEx.makeFunction(toStringMap.get(invoke.getClass())).apply(invoke));
            } else if (invoke instanceof Enumeration) {
                description.append(getEnumerationDescription(invoked, getterMethods, fieldName, (Enumeration<?>) invoke, toStringMap));
            } else {
                description.append(invoke);
            }
            map.put(fieldName, description.toString());
        }));
        return map;
    }

    public static List<Method> getGetterMethods(Class<?> class1) {
        Map<Class<?>, List<Method>> getterMethods = new HashMap<>();
        return getGetterMethods(getterMethods, class1);
    }



    private static <T> String getEnumerationDescription(Set<Object> invoked, Map<Class<?>, List<Method>> getterMethods,
            String fieldName, Enumeration<T> ee, Map<Class<?>, FunctionEx<Object, String>> toStringMap) {
        StringBuilder descriptionBuilder = new StringBuilder("{\n");
        int i = 0;
        while (ee.hasMoreElements()) {
            T element = ee.nextElement();
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
            String description = getDescription(invoked, getterMethods, element.getClass(), element, toStringMap);
            if (!description.isEmpty()) {
                descriptionBuilder.append("\t\t\t");
                descriptionBuilder.append(description.replaceAll("\t", "\t\t\t"));
            }
            descriptionBuilder.append("\t\t}\n");
        }
        descriptionBuilder.append("\t}");
        return descriptionBuilder.toString();
    }

    private static List<Method> getGetterMethods(Map<Class<?>, List<Method>> getterMethods, Class<?> class1) {
        if (!getterMethods.containsKey(class1)) {
            getterMethods.put(class1,
                    Stream.of(class1.getDeclaredMethods()).filter(m -> Modifier.isPublic(m.getModifiers()))
                            .filter(m -> m.getName().matches(METHOD_REGEX)).filter(m -> m.getParameterCount() == 0)
                            .sorted(Comparator.comparing(t -> t.getName().replaceAll(METHOD_REGEX, "$1$2")))
                            .collect(Collectors.toList()));
        }
        return getterMethods.get(class1);

    }
}
