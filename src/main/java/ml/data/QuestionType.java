package ml.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;
import org.apache.commons.lang3.StringUtils;
import utils.ClassReflectionUtils;
import utils.ex.PredicateEx;

public enum QuestionType {
    EQ("==", (ob1, ob2) -> Objects.equals(ob1, ob2) || Objects.equals(ob1, Objects.toString(ob2)), String.class,
            Number.class),
    NE("!=", (ob1, ob2) -> !Objects.equals(ob1, ob2) && !Objects.equals(ob1, Objects.toString(ob2)), String.class,
            Number.class),
    CONTAINS("contains",
            (ob1, ob2) -> StringUtils.containsIgnoreCase(Objects.toString(ob1), Objects.toString(ob2)),
            String.class),
    IN("in", (ob1, ob2) -> ((Collection<?>) ob2).contains(ob1), String.class, Number.class),
    LIKE("like", (ob1, ob) -> PredicateEx.test(s -> s.matches(Objects.toString(ob)), Objects.toString(ob1)),
            String.class),
    GE(">=", (ob1, ob) -> ob1 >= ob),
    LE("<=", (ob1, ob) -> ob1 <= ob),
    LT("<", (ob1, ob) -> ob1 < ob),
    GT(">", (ob1, ob) -> ob1 > ob);

    private final String sign;
    private final Class<?>[] classes;
    private BiPredicate<Object, Object> test;

    QuestionType(String sign, BiPredicate<Double, Double> biPredicate) {
        this.sign = sign;
        test = (ob1, ob2) -> ob1 instanceof Number && ob2 instanceof Number
                && biPredicate.test(((Number) ob1).doubleValue(), ((Number) ob2).doubleValue());
        classes = new Class<?>[] { Number.class };
    }

    QuestionType(String sign, BiPredicate<Object, Object> biPredicate, Class<?>... clazz) {
        this.sign = sign;
        test = biPredicate;
        classes = clazz;
    }


    public boolean execute(Object t, Object u) {
        return test.test(t, u);
    }

    public Class<?>[] getClasses() {
        return classes;
    }

    public String getSign() {
        return sign;
    }

    public boolean matchesClass(Class<?> a) {
        return ClassReflectionUtils.hasClass(Arrays.asList(classes), a);
    }

}