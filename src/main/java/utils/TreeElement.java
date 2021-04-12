package utils;

import static utils.ClassReflectionUtils.getFieldNameCase;
import static utils.ClassReflectionUtils.getGetterMethodsRecursive;
import static utils.ClassReflectionUtils.invoke;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import utils.ex.FunctionEx;

public final class TreeElement<T> {

    private static final String CHILDREN_FIELD = "children";
    private Collection<TreeElement<T>> children;

    private final T element;

    private TreeElement(T e, FunctionEx<T, Collection<T>> func) {
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

    @Override
    public String toString() {
        return element.getClass().getName();
    }

    private List<String> getMissingItem(String left, TreeElement<T> otherElement) {
        TreeElement<T> other = otherElement;
        Collection<TreeElement<T>> children2 = other.children;
        List<TreeElement<T>> notContainedElements = notContained(children, children2);
        List<String> missing = new ArrayList<>();
        for (TreeElement<T> treeElement : notContainedElements) {
            if (children2.stream().noneMatch(e -> e.hashCode() == treeElement.hashCode())) {
                missing.add(left + treeElement);
            }
            Optional<TreeElement<T>> findFirst = children2.stream()
                .filter(e -> e.element.getClass() == treeElement.element.getClass()).findFirst();
            if (findFirst.isPresent()) {
                List<String> missingItem = treeElement.getMissingItem(left + "-", findFirst.get());
                missing.addAll(missingItem);
            }
        }
        return missing;

    }

    private List<String> getMissingItem(TreeElement<T> otherElement) {
        return getMissingItem("", otherElement);
    }

    private List<TreeElement<T>> notContained(Collection<TreeElement<T>> children1,
        Collection<TreeElement<T>> children2) {
        return children1.stream().filter(e -> children2 == null || !children2.contains(e))
            .sorted(Comparator.comparing(TreeElement<T>::hashCode)).collect(Collectors.toList());
    }

    public static boolean compareTree(Parent root, Parent root2) {
        FunctionEx<Node, Collection<Node>> f = e -> !(e instanceof Parent) ? null
            : ((Parent) e).getChildrenUnmodifiable();
        TreeElement<Node> original = TreeElement.buildTree(root, f);
        TreeElement<Node> generated = TreeElement.buildTree(root2, f);
        return Objects.equals(original, generated);
    }

    public static List<String> displayMissingElement(Parent root, Parent root2) {
        FunctionEx<Node, Collection<Node>> f = e -> !(e instanceof Parent) ? null
            : ((Parent) e).getChildrenUnmodifiable();
        TreeElement<Node> original = TreeElement.buildTree(root, f);
        TreeElement<Node> generated = TreeElement.buildTree(root2, f);
        return original.getMissingItem(generated);
    }

    public static String displayStyleClass(Node node) {
        StringBuilder str = new StringBuilder("\n");
        displayStyleClass("", node, str);
        return str.toString();
    }

    public static List<String> getDifferences(Class<?> cl, Object ob1, Object ob2) {
        List<String> diffFields = new ArrayList<>();
        if (ob2 == null) {
            return diffFields;
        }

        List<Method> fields = getGetterMethodsRecursive(cl);
        if (equalParents(ob1, ob2)) {
            fields.removeIf(e -> CHILDREN_FIELD.equals(getFieldNameCase(e)));
        }
        for (Method f : fields) {
            Object fieldValue = invoke(ob1, f);
            Object fieldValue2 = invoke(ob2, f);
            if (fieldValue != null && !Objects.equals(fieldValue, fieldValue2)) {
                if (equalParents(fieldValue, fieldValue2)) {
                    continue;
                }
                String fieldName = getFieldNameCase(f);
                diffFields.add(fieldName);
            }
        }

        return diffFields;
    }

    private static <E> TreeElement<E> buildTree(E first, FunctionEx<E, Collection<E>> func) {
        return new TreeElement<>(first, func);
    }

    private static void displayStyleClass(String left, Node node, StringBuilder str) {
        String arg1 = left + node.getClass().getSimpleName();
        if (node instanceof Labeled) {
            str.append(String.format("%s = .%s = \"%s\"%n", arg1, node.getStyleClass(), ((Labeled) node).getText()));
        }

        String id = node.getId();
        if (id != null) {
            str.append(String.format("%s = #%s.%s%n", arg1, id, node.getStyleClass()));
        } else {
            str.append(String.format("%s = .%s%n", arg1, node.getStyleClass()));
        }
        if (node instanceof Parent) {
            ObservableList<Node> childrenUnmodifiable = ((Parent) node).getChildrenUnmodifiable();
            childrenUnmodifiable.forEach(t -> displayStyleClass(left + "-", t, str));
        }
    }

    private static boolean equalParents(Object ob1, Object ob2) {
        return ob1 instanceof Parent && ob2 instanceof Parent && compareTree((Parent) ob1, (Parent) ob2);
    }

}
