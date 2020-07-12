package ml.data;

import java.util.Arrays;
import utils.ClassReflectionUtils;

public enum QuestionType {
    EQ("==", String.class, Number.class),
    NE("!=", String.class, Number.class),
    CONTAINS("contains", String.class),
    LIKE("like", String.class),
    GE(">=", Number.class),
    LE("<=", Number.class),
    LT("<", Number.class),
    GT(">", Number.class);

	private final String sign;
    private final Class<?>[] classes;

    QuestionType(String sign, Class<?>... clazz) {
		this.sign = sign;
        classes = clazz;
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