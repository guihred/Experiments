package others;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import utils.FunctionEx;

public class TreeElement<T> {

    private Collection<TreeElement<T>> children;
    private T element;

    public TreeElement(T e, FunctionEx<T, Collection<T>> func) {
        element = e;
        Collection<T> apply = FunctionEx.makeFunction(func).apply(e);
        if (apply != null) {
            children = new HashSet<>();
            for (T t : apply) {
                if (t != null) {
                    children.add(new TreeElement<>(t, func));
                }
            }
        }
    }

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
        TreeElement<?> other = (TreeElement<?>) obj;
        if (!Objects.equals(children, other.children)) {
            return false;
        }
        return Objects.equals(element.getClass(), other.element.getClass());
    }

    @Override
    public int hashCode() {
		return Objects.hash(children, element.getClass());
    }

    public static <E> TreeElement<E> buildTree(E first, FunctionEx<E, Collection<E>> func) {
        return new TreeElement<>(first, func);
    }

}
