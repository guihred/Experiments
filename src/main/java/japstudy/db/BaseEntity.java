package japstudy.db;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import utils.HasLogging;

public abstract class BaseEntity implements Serializable, HasLogging {

    protected abstract Serializable getKey();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BaseEntity other = (BaseEntity) obj;
        if (!Objects.equals(other.getKey(), getKey())) {
            return false;
        }
        return obj == this;
    }

    @Override
    public int hashCode() {
        if (getKey() == null) {
            return super.hashCode();
        }

        return Objects.hash(getKey());
    }

    public String toSQL() {

        List<Field> collect = Stream.of(getClass().getDeclaredFields()).filter(BaseEntity::isFieldOk)
                .collect(Collectors.toList());
        String fields = collect.stream().map(Field::getName).collect(Collectors.joining(","));
        String fieldsValues = collect.stream().map(this::getFieldValue).map(BaseEntity::type)
                .collect(Collectors.joining(","));

        return String.format("INSERT INTO %s(%s) VALUES (%s);", getClass().getSimpleName(), fields, fieldsValues);
    }

    private static boolean isFieldOk(Field e) {
        return e.getType() != List.class && !Modifier.isStatic(e.getModifiers());
    }

    private Object getFieldValue(Field e) {
        try {
            e.setAccessible(true);
            return e.get(this);
        } catch (Exception e1) {
            getLogger().error("", e1);
        }
        return null;
    }

    private static String type(Object e) {
        if (e == null) {
            return "null";
        }
        if (e instanceof String || e.getClass().isEnum()) {
            return "'" + e.toString().replaceAll("\\s+", " ") + "'";
        }
        if (e instanceof Number) {
            return "" + e;
        }
        if (e instanceof BaseEntity) {
            return type(((BaseEntity) e).getKey());
        }

        return "" + e;
    }
}
