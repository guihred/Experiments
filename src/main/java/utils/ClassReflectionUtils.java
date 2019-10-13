package utils;

import static java.util.stream.Collectors.joining;
import static utils.StringSigaUtils.changeCase;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.NamedArg;
import javafx.beans.property.Property;
import org.junit.Ignore;

public final class ClassReflectionUtils {
    private static final String METHOD_REGEX = "is(\\w+)|get(\\w+)";
    private static final String METHOD_REGEX_SETTER = "set(\\w+)";

    private ClassReflectionUtils() {
    }

    public static List<Class<?>> allClasses(Class<?> targetClass) {

        List<Class<?>> classes = new ArrayList<>();

        Class<?> a = targetClass;
        for (int i = 0; i < 10 && a != Object.class; i++, a = a.getSuperclass()) {
            classes.add(a);
            Class<?>[] interfaces = a.getInterfaces();
            classes.addAll(Arrays.asList(interfaces));
        }

        return classes.parallelStream().distinct().collect(Collectors.toList());
    }

    public static List<Method> getAllMethodsRecursive(Class<?> targetClass) {
        Class<?> a = targetClass;
        List<Method> getters = new ArrayList<>();
        for (int i = 0; i < 10; i++, a = a.getSuperclass()) {
            List<Method> getters2 = Arrays.asList(a.getDeclaredMethods());
            getters.addAll(getters2);
            Class<?>[] interfaces = a.getInterfaces();
            for (Class<?> class1 : interfaces) {
                getters.addAll(Arrays.asList(class1.getDeclaredMethods()));
            }
            if (a == Object.class) {
                break;
            }
        }
        return getters;
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

    public static Map<String, String> getDescriptionMap(Object obj,
        Map<Class<?>, FunctionEx<Object, String>> toStringMap) {
        return getDescriptionMap(obj, obj.getClass(), toStringMap, new HashSet<>(), new HashMap<>(), new HashMap<>());

    }

    public static String getFieldNameCase(Member t) {
        return changeCase(getFieldName(t));
    }

    public static List<String> getFields(Class<?> class1) {
        return Stream.of(class1.getDeclaredFields()).filter(m -> !Modifier.isStatic(m.getModifiers()))
            .map(Field::getName).collect(Collectors.toList());
    }

    public static Object getFieldValue(Object ob, String name) {
        return getFieldsRecursive(ob.getClass()).stream().filter(m -> !Modifier.isStatic(m.getModifiers()))
            .filter(e -> e.getName().equals(name)).findFirst().map(field -> BaseEntity.getFieldValue(ob, field))
            .orElse(null);
    }

    public static List<Method> getGetterMethodsRecursive(Class<?> targetClass) {
        return getGetterMethodsRecursive(targetClass, 10);
    }

    public static List<Method> getGetterMethodsRecursive(Class<?> targetClass, int parent) {
        Class<?> a = targetClass;
        List<Method> getters = new ArrayList<>();
        for (int i = 0; a != Object.class && i < parent; i++, a = a.getSuperclass()) {
            List<Method> getters2 = getters(a);
            getters.addAll(getters2);
        }
        return getters;
    }

    public static <T> T getInstance(Class<T> cl) {
        return SupplierEx.remap(cl::newInstance, "ERROR IN INSTANTIATION");
    }

    public static List<String> getNamedArgs(Class<?> targetClass) {
        return getNamedArgsMap(targetClass).keySet().stream().collect(Collectors.toList());
    }

    public static String getSignature(Object parent, String fieldName, Object fieldValue, Collection<String> packages) {
        String signature = "()";
        Class<? extends Object> valueClass = fieldValue.getClass();
        Class<?> class1 = valueClass.getInterfaces()[0];
        TypeVariable<?>[] typeParameters = class1.getTypeParameters();
        if (typeParameters.length > 0) {
            TypeVariable<?> typeVariable = typeParameters[0];
            Type[] bounds = typeVariable.getBounds();
            for (Type type : bounds) {
                String typeName = type.getTypeName();
                String[] split2 = typeName.split("\\.");
                String packageName = Stream.of(split2).limit(split2.length - 1L).collect(joining("."));
                packages.add(packageName);
                signature = "(" + split2[split2.length - 1] + " e)";
            }
        }
        Method setter = getSetter(parent.getClass(), fieldName);
        if (setter != null) {
            for (Parameter class2 : setter.getParameters()) {
                String[] methodSignature = class2.toString().split("[^\\w\\.]");
                String eventType = methodSignature[methodSignature.length - 3];
                String[] split2 = eventType.split("\\.");
                String packageName = Stream.of(split2).limit(split2.length - 1L).collect(joining("."));
                if (!packageName.isEmpty()) {
                    packages.add(packageName);
                    signature = "(" + split2[split2.length - 1] + " e)";
                }
            }
        }
        return signature;
    }

    public static List<Method> getters(Class<?> c) {
        return Stream.of(c.getDeclaredMethods()).filter(m -> !Modifier.isStatic(m.getModifiers()))
            .filter(m -> Modifier.isPublic(m.getModifiers())).filter(m -> m.getName().matches(METHOD_REGEX))
            .filter(m -> m.getParameterCount() == 0)
            .filter(m -> m.getReturnType() != Serializable.class && m.getAnnotationsByType(Ignore.class).length == 0)
            .sorted(Comparator.comparing(ClassReflectionUtils::getFieldName)).collect(Collectors.toList());
    }

    public static boolean hasClass(Collection<Class<?>> newTagClasses, Class<? extends Object> class1) {
        return Modifier.isPublic(class1.getModifiers())
            && newTagClasses.stream().anyMatch(c -> c.isAssignableFrom(class1) || class1.isAssignableFrom(c));
    }

    public static boolean hasField(Class<?> targetClass, String field) {
        return hasSetterMethods(targetClass, field) || hasBuiltArg(targetClass, field);
    }

    public static boolean hasPublicConstructor(Class<? extends Object> class1) {
        return Modifier.isPublic(class1.getModifiers())
            && Stream.of(class1.getConstructors()).anyMatch(e1 -> Modifier.isPublic(e1.getModifiers()));
    }

    public static Object invoke(Object ob, Method method, Object... args) {
        return SupplierEx.get(() -> method.invoke(ob, args));
    }

    public static Object invoke(Object ob, String method, Object... args) {
        if (ob == null) {
            return null;
        }
        return getAllMethodsRecursive(ob.getClass()).stream().filter(e -> getFieldNameCase(e).equals(method))
            .map(FunctionEx.makeFunction(m -> m.invoke(ob, args))).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static boolean isClassPublic(Class<? extends Object> class1) {
        return Modifier.isPublic(class1.getModifiers()) && Stream.of(class1.getConstructors())
            .anyMatch(e -> e.getParameterCount() == 0 && Modifier.isPublic(e.getModifiers()));
    }

    public static boolean isSetterMatches(String fieldName, Object fieldValue, Object parent) {
        Map<String, Class<?>> namedArgsMap = ClassReflectionUtils.getNamedArgsMap(parent.getClass());
        if (namedArgsMap.containsKey(fieldName)) {
            return typesFit(fieldValue, namedArgsMap.get(fieldName));
        }
        return PredicateEx.makeTest((String f) -> ClassReflectionUtils.getAllMethodsRecursive(parent.getClass())
            .stream().filter(m -> m.getParameterCount() == 1)
            .anyMatch(m -> getFieldNameCase(m).equals(f) && parameterTypesMatch(fieldValue, m))).test(fieldName);
    }

    public static Map<String, Property<?>> properties(Object o, Class<?> c) {
        String regex = "(\\w+)Property";
        return Stream.of(c.getDeclaredMethods()).filter(m -> !Modifier.isStatic(m.getModifiers()))
            .filter(m -> Modifier.isPublic(m.getModifiers())).filter(m -> m.getName().matches(regex))
            .filter(m -> m.getParameterCount() == 0)
            .sorted(Comparator.comparing(t -> t.getName().replaceAll(regex, "$1")))
            .collect(Collectors.toMap(t -> t.getName().replaceAll(regex, "$1"), t -> (Property<?>) invoke(o, t)));
    }

    private static <T> String getDescription(T obj, Class<?> class1,
        Map<Class<?>, FunctionEx<Object, String>> toStringMap, Set<Object> invokedObjects,
        Map<Class<?>, List<Method>> getterMethods) {
        if (invokedObjects.contains(obj)) {
            return "";
        }
        invokedObjects.add(obj);
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

    private static <T> Map<String, String> getDescriptionMap(T obj, Class<?> objClass,
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

    private static String getFieldName(Member t) {
        return t.getName().replaceAll(METHOD_REGEX_SETTER + "|" + METHOD_REGEX, "$1$2$3");
    }

    private static List<Field> getFieldsRecursive(Class<?> class1) {
        Class<?> a = class1;
        List<Field> fields = new ArrayList<>();
        for (int i = 0; a != Object.class && i < 10; i++, a = a.getSuperclass()) {
            Field[] declaredFields = a.getDeclaredFields();
            for (Field m : declaredFields) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    fields.add(m);
                }
            }
        }
        return fields;
    }

    private static List<Method> getGetterMethods(Class<?> class1, Map<Class<?>, List<Method>> getterMethods) {
        return getterMethods.computeIfAbsent(class1, ClassReflectionUtils::getters);

    }

    private static Map<String, Class<?>> getNamedArgsMap(Class<?> targetClass) {
        Map<String, Class<?>> args = new HashMap<>();
        Constructor<?>[] constructors = targetClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Parameter[] parameters = constructor.getParameters();
            for (Parameter parameter : parameters) {
                Class<?> type = parameter.getType();
                Annotation[] annotations = parameter.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation instanceof NamedArg) {
                        NamedArg a = (NamedArg) annotation;
                        String value = a.value();
                        args.put(value, type);
                    }
                }
            }
        }
        return args;
    }

    private static Method getSetter(Class<?> cl, String f) {
        return ClassReflectionUtils.getAllMethodsRecursive(cl).stream().filter(m -> m.getParameterCount() == 1)
            .filter(m -> getFieldNameCase(m).equals(f)).findFirst().orElse(null);
    }

    private static boolean hasBuiltArg(Class<?> targetClass, String field) {
        return getNamedArgs(targetClass).contains(field);
    }

    private static boolean hasSetterMethods(Class<?> targetClass, String field) {
        Class<?> a = targetClass;

        for (int i = 0; a != Object.class && i < 10; i++, a = a.getSuperclass()) {
            List<Method> gett = setters(a);
            if (gett.stream().anyMatch(m -> getFieldNameCase(m).equals(field))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRecursiveCall(Class<?> class1, Object invoke) {
        return invoke instanceof Enumeration && invoke.getClass().getGenericInterfaces().length > 0
            && invoke.getClass().getGenericInterfaces()[0].getTypeName().contains(class1.getName());
    }

    private static boolean parameterTypesMatch(Object fieldValue, Executable m) {
        Parameter[] parameters = m.getParameters();
        Class<?> type = parameters[0].getType();
        if (type == Object.class) {
            return false;
        }
        return typesFit(fieldValue, type);
    }

    private static List<Method> setters(Class<?> c) {
        return Stream.of(c.getDeclaredMethods()).filter(m -> Modifier.isPublic(m.getModifiers()))
            .filter(m -> m.getName().matches(METHOD_REGEX_SETTER)).filter(m -> m.getParameterCount() == 1)
            .sorted(Comparator.comparing(ClassReflectionUtils::getFieldName)).collect(Collectors.toList());
    }

    private static boolean typesFit(Object fieldValue, Class<?> type) {
        return type.isAssignableFrom(fieldValue.getClass())
            || type.getSimpleName().equalsIgnoreCase(fieldValue.getClass().getSimpleName())
            || type == int.class && fieldValue.getClass() == Integer.class;
    }
}
