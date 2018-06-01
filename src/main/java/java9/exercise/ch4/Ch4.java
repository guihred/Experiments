package java9.exercise.ch4;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class Ch4 {
    /**
     * 8. The Class class has six methods that yield a string representation of the
     * type represented by the Class object. How do they differ when applied to
     * arrays, generic types, inner classes, and primitive types?
     */

    public static void classRepresentations() {
        for (Class<?> class1 : Arrays.asList(int[].class, List.class, Map.Entry.class, int.class)) {
            System.out.println(class1.getCanonicalName());
            System.out.println(class1.getName());
            System.out.println(class1.getSimpleName());
            System.out.println(class1.getTypeName());
            System.out.println(class1.toString());
            System.out.println(class1.toGenericString());
        }
    }

    public static void main(String[] args) {
        System.out.println(cyclicToString(new Line(new Point(2, 3), new LabeledPoint("a", 3, 3))));
    }

    /**
     * 9. Write a “universal” toString method that uses reflection to yield a string
     * with all instance variables of an object. Extra credit if you can handle
     * cyclic references.
     */
    public static String cyclicToString(Object s) {
        return cyclicToString(s, new ArrayList<>());
    }

    private static String cyclicToString(Object s, List<Class<?>> classes) {
        if (s == null) {
            return "null";
        }
        Class<? extends Object> class1 = s.getClass();
        classes.add(class1);
        List<Field> fields = Stream
                .concat(Stream.of(class1.getDeclaredFields()), Stream.of(class1.getSuperclass().getDeclaredFields()))
                .filter(e -> isAccessLegal(s, e))
                .collect(Collectors.toList());

        String pad = fields.size() <= 1 ? "" : classes.stream().map(e -> "  ").collect(Collectors.joining(""));
        return class1.getSimpleName() + "{" + extracted(fields) + fields.stream().map(e -> {
            try {
                String cyclicToString;
                if (isClassUsed(classes, e)) {
                    cyclicToString = Objects.toString(e.get(s));
                } else {
                    List<Class<?>> classes2 = new ArrayList<>(classes);
                    classes2.add(e.getType());
                    cyclicToString = cyclicToString(e.get(s), classes2);
                }
                return e.getName() + "=" + cyclicToString;
            } catch (IllegalAccessException e1) {
                return "";
            }
        }).filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(",\n" + pad, pad,
                        extracted(fields) + StringUtils.repeat("  ", classes.size() - 1)))
                + "}";
    }

    private static boolean isClassUsed(List<Class<?>> classes, Field e) {
        return classes.contains(e.getType()) || e.getType().isPrimitive() || e.getType().isAssignableFrom(String.class);
    }

    private static String extracted(List<Field> fields) {
        return fields.size() <= 1 ? "" : "\n";
    }

    private static boolean isAccessLegal(Object s, Field e) {
        try {
            e.setAccessible(true);
            e.get(s);
        } catch (Exception ex) {
            return false;
        }
        return !Modifier.isStatic(e.getModifiers());
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