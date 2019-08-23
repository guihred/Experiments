package others;

import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.Parent;
import utils.FunctionEx;
import utils.HasLogging;

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

    public List<TreeElement<T>> getMissingItem(String left,TreeElement<T> otherElement) {
        TreeElement<T> other = otherElement;
        Collection<TreeElement<T>> children2 = other.children;
        List<TreeElement<T>> collect = extracted(children, children2);
        for (TreeElement<T> treeElement : collect) {
            if (children2.stream().noneMatch(e -> e.hashCode() == treeElement.hashCode())) {
                HasLogging.log(1).info("MISSING {}", left + treeElement);
            }
            Optional<TreeElement<T>> findFirst = children2.stream()
                .filter(e -> e.element.getClass() == treeElement.element.getClass()).findFirst();
            if (findFirst.isPresent()) {
                treeElement.getMissingItem(left + "-", findFirst.get());
            }
        }
        return collect;

    }

    public List<TreeElement<T>> getMissingItem(TreeElement<T> otherElement) {
        return getMissingItem("",otherElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, element.getClass());
    }

    @Override
    public String toString() {
        return element.getClass().getName();
    }

    private List<TreeElement<T>> extracted(Collection<TreeElement<T>> children1, Collection<TreeElement<T>> children2) {
        return children1.stream()
            .filter(e -> children2 == null || !children2.contains(e))
            .sorted(Comparator.comparing(TreeElement<T>::hashCode))
            .collect(Collectors.toList());
    }

    public static <E> TreeElement<E> buildTree(E first, FunctionEx<E, Collection<E>> func) {
        return new TreeElement<>(first, func);
    }

    public static boolean compareTree(Parent root, Parent root2) {
        FunctionEx<Node, Collection<Node>> f = e -> !(e instanceof Parent) ? null
            : ((Parent) e).getChildrenUnmodifiable();
        TreeElement<Node> original = TreeElement.buildTree(root, f);
        TreeElement<Node> generated = TreeElement.buildTree(root2, f);
        return Objects.equals(original, generated);
    }

    public static void displayMissingElement(Parent root, Parent root2) {
        FunctionEx<Node, Collection<Node>> f = e -> !(e instanceof Parent) ? null
            : ((Parent) e).getChildrenUnmodifiable();
        TreeElement<Node> original = TreeElement.buildTree(root, f);
        TreeElement<Node> generated = TreeElement.buildTree(root2, f);
        original.getMissingItem(generated);
    }

}
