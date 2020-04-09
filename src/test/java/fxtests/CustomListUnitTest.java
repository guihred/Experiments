package fxtests;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;
import org.slf4j.Logger;
import utils.ClassReflectionUtils;
import utils.CustomList;
import utils.HasLogging;
import utils.SupplierEx;

public class CustomListUnitTest {

    private static final Logger LOG = HasLogging.log();
    Map<Class<?>, Object> parameter = new HashMap<>();

    @Test
    public void testCustomListImplementation() throws Exception {
        testImplementation(Collection.class, new ArrayList<>(), new CustomList<>());
    }

    @Test
    public void testCustomSetImplementation() throws Exception {
        testImplementation(Set.class, new HashSet<>(), new CustomList<>());
    }

    private Object getParameterByType(Parameter e) {
        Class<?> type = e.getType();
        return parameter.computeIfAbsent(type, m -> getNewIntance(m));
    }

    private void testImplementation(Class<?> cl, Collection<Object> arrayList, List<Object> arrayList0)
            throws ArrayComparisonFailure {
        List<Method> allMethodsRecursive = ClassReflectionUtils.getAllMethodsRecursive(cl);
        for (Method method : allMethodsRecursive) {
            if (method.getName().equals("remove")) {
                continue;
            }
            LOG.info("Method {}", method);
            Parameter[] parameters = method.getParameters();
            Object[] array = Stream.of(parameters).map(this::getParameterByType).toArray();
            LOG.info("Params {}", Arrays.toString(array));
            LOG.info("Invoking arrayList");
            Object object = SupplierEx.get(() -> method.invoke(arrayList, array));
            LOG.info("Invoking customList");
            Object object2 = SupplierEx.get(() -> method.invoke(arrayList0, array));
            if (Objects.equals(object, object2)) {
                LOG.info("Results equals {}", object);
                Assert.assertEquals("Lists should be equal", arrayList, arrayList0);
            } else {
                if (object instanceof Object[] && object2 instanceof Object[]) {
                    Assert.assertArrayEquals("Arrays should be equal", (Object[]) object, (Object[]) object2);
                } else if (object instanceof Iterator && object2 instanceof Iterator) {
                    assertIterators(object, object2);
                } else if (object instanceof Spliterator && object2 instanceof Spliterator) {
                    assertSpliterator(object, object2);
                } else if (object instanceof Stream && object2 instanceof Stream) {
                    assertStream(object, object2);
                } else if (!"hashCode".equals(method.getName())) {
                    Assert.fail(String.format("Method %s return not equal (%s!=%s)", method, object, object2));
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    private static void assertIterators(Object object, Object object2) {
        Iterator<Object> a = (Iterator<Object>) object;
        Iterator<Object> b = (Iterator<Object>) object2;
        while (a.hasNext()) {
            Assert.assertTrue("Should have next", b.hasNext());
            Assert.assertEquals("Objs should be equal", a.next(), b.next());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void assertSpliterator(Object object, Object object2) {
        Spliterator<Object> a = (Spliterator) object;
        Spliterator<Object> b = (Spliterator) object2;

        a.forEachRemaining(Objects::toString);
        b.forEachRemaining(Objects::toString);
    }

    @SuppressWarnings({ "unchecked" })
    private static void assertStream(Object object, Object object2) {
        Stream<Object> a = (Stream<Object>) object;
        Stream<Object> b = (Stream<Object>) object2;
        Assert.assertEquals("Lists should be equal", a.collect(Collectors.toList()), b.collect(Collectors.toList()));
    }

    private static Object getNewIntance(Class<?> m) {
        try {
            if (m == Object.class) {
                return "obj";
            }
            if (m == UnaryOperator.class) {
                return (UnaryOperator<Object>) t -> t + " 1";
            }
            if (m == int.class) {
                return 0;
            }
            if (m == Collection.class) {
                return Arrays.asList("obj", "obj2");
            }
            if (m == Object[].class) {
                return new Object[] {};
            }
            if (m == Consumer.class) {
                return (Consumer<Object>) t -> {
                    // DOES NOTHING
                };
            }
            if (m == Predicate.class) {
                return (Predicate<Object>) t -> t != null;
            }
            if (m == Comparator.class) {
                return Comparator.comparing(Objects::toString);
            }
            return m.newInstance();
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }
    }
}