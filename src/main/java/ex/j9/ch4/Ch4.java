package ex.j9.ch4;

import static utils.ex.FunctionEx.makeFunction;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public class Ch4 {
    /**
     * 8. The Class class has six methods that yield a string representation of the
     * type represented by the Class object. How do they differ when applied to
     * arrays, generic types, inner classes, and primitive types?
     */
    private static final Logger LOGGER = HasLogging.log();

    public static void classRepresentations() {
        for (Class<?> class1 : Arrays.asList(int[].class, List.class, Map.Entry.class, int.class)) {
            LOGGER.info("{}", class1.getCanonicalName());
            LOGGER.info("{}", class1.getName());
            LOGGER.info("{}", class1.getSimpleName());
            LOGGER.info("{}", class1.getTypeName());
            LOGGER.info("{}", class1);
            String genericString = class1.toGenericString();
            LOGGER.info("{}", genericString);
        }
    }

    /**
     * 9. Write a “universal” toString method that uses reflection to yield a string
     * with all instance variables of an object. Extra credit if you can handle
     * cyclic references.
     */
    public static String cyclicToString(Object s) {
        return cyclicToString(s, new ArrayList<>());
    }

    public static void main(String[] args) {
        String cyclicToString = cyclicToString(new LineCh4(new PointCh4(2, 3), new LabeledPoint("a", 3, 3)));
        LOGGER.info("{}", cyclicToString);
    }

    private static String cyclicToStr(Object s, List<Class<?>> classes, Field e) throws IllegalAccessException {
        if (isClassUsed(classes, e)) {
            return Objects.toString(e.get(s));
        }
        List<Class<?>> classes2 = new ArrayList<>(classes);
        classes2.add(e.getType());
        return cyclicToString(e.get(s), classes2);
    }

    private static String cyclicToString(Object s, List<Class<?>> classes) {
        if (s == null) {
            return "null";
        }
        Class<? extends Object> class1 = s.getClass();
        classes.add(class1);
        List<Field> fields = Stream
            .concat(Stream.of(class1.getDeclaredFields()), Stream.of(class1.getSuperclass().getDeclaredFields()))
            .filter(e -> isAccessLegal(s, e)).collect(Collectors.toList());

        String pad = fields.size() <= 1 ? "" : classes.stream().map(e -> "  ").collect(Collectors.joining(""));
        return class1.getSimpleName() + "{" + extracted(fields)
            + fields.stream().map(makeFunction(e -> e.getName() + "=" + cyclicToStr(s, classes, e)))
                .filter(StringUtils::isNotBlank).collect(Collectors.joining(",\n" + pad, pad,
                    extracted(fields) + StringUtils.repeat("  ", classes.size() - 1)))
            + "}";
    }

    private static String extracted(List<Field> fields) {
        return fields.size() <= 1 ? "" : "\n";
    }

    private static boolean isAccessLegal(Object s, Field e) {
        return SupplierEx.get(() -> {
            e.setAccessible(true);
            e.get(s);
            return !Modifier.isStatic(e.getModifiers());
        }, false);
    }

    private static boolean isClassUsed(List<Class<?>> classes, Field e) {
        return classes.contains(e.getType()) || e.getType().isPrimitive() || e.getType().isAssignableFrom(String.class);
    }

    /**
     * 10. Use the MethodPrinter program in Section 4.5.1, "Enumerating Class
     * Members" (page 168) to enumerate all methods of the int[] class. Extra credit
     * if you can identify the one method (discussed in this chapter) that is
     * wrongly described.
     */

    /**
     * 11. Write the “Hello, World” program, using reflection to look up the out
     * field of java.lang.System and using invoke to call the println method.
     * 
     * 12. Measure the performance difference between a regular method call and a
     * method call via reflection.
     * 
     * 13. Write a method that prints a table of values for any Method representing
     * a static method with a parameter of type double or Double. Besides the Method
     * object, accept a lower bound, upper bound, and step size. Demonstrate your
     * method by printing tables for Math.sqrt and Double.toHexString. Repeat, using
     * a DoubleFunction<Object> instead of a Method (see Section 3.6.2, “Choosing a
     * Functional Interface,” page 120). Contrast the safety, efficiency, and
     * convenience of both approaches.
     */
}
