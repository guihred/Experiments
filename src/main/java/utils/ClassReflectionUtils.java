package utils;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;

public final class ClassReflectionUtils {
    private static final Logger LOG = HasLogging.log();
    private static final String METHOD_REGEX = "is(\\w+)|get(\\w+)";

    private ClassReflectionUtils() {
        LOG.error("");
    }

    public static void displayCSSStyler(Scene scene, String pathname) {
        ClassReflectionUtils.displayStyleClass(scene.getRoot());
        Stage stage2 = new Stage();
        File file = new File("src/main/resources/" + pathname);
        TextArea textArea = new TextArea(getText(file));
        if (file.exists()) {
            try {
                scene.getStylesheets().add(file.toURI().toURL().toString());
            } catch (MalformedURLException e2) {
                LOG.error("", e2);
            }
        }
        stage2.setScene(new Scene(new VBox(textArea, CommonsFX.newButton("_Save", e -> {
            try (PrintStream fileOutputStream = new PrintStream(file, StandardCharsets.UTF_8.name())) {
                fileOutputStream.print(textArea.getText());
                fileOutputStream.flush();
                scene.getStylesheets().clear();
                scene.getStylesheets().add(file.toURI().toURL().toString());
                textArea.requestFocus();
            } catch (Exception e1) {
                LOG.error("", e1);
            }

        }))));
        textArea.prefHeightProperty().bind(stage2.heightProperty().subtract(10));
        stage2.setHeight(500);
        stage2.show();
    }

    public static void displayStyleClass(Node node) {
        displayStyleClass("", node);
    }

    public static String getDescription(Object i) {
        return getDescription(i, i.getClass(), new HashMap<>(), new HashSet<>(), new HashMap<>());

    }

    public static String getDescription(Object obj, Map<Class<?>, FunctionEx<Object, String>> toStringMap) {
        return getDescription(obj, obj.getClass(), toStringMap, new HashSet<>(), new HashMap<>());

    }

    public static <T> String getDescription(T obj, Class<?> class1, Map<Class<?>, FunctionEx<Object, String>> toStringMap,
            Set<Object> invokedObjects, Map<Class<?>, List<Method>> getterMethods) {
        if (!invokedObjects.contains(obj)) {
            invokedObjects.add(obj);
        } else {
            return "";
        }
        List<Method> infoMethod = getGetterMethods(class1, getterMethods);
        StringBuilder description = new StringBuilder("\n");
        infoMethod.forEach(ConsumerEx.makeConsumer((Method method) -> {
            Object invoke = method.invoke(obj);
            // To Avoid infinite loop
            if (isRecursiveCall(class1, invoke)) {
                return;
            }
            String fieldName = method.getName().replaceAll(METHOD_REGEX, "$1$2");
            description.append("\t");
            description.append(fieldName);
            description.append(" = ");
            if (invoke != null && toStringMap.containsKey(invoke.getClass())) {
                description.append(FunctionEx.makeFunction(toStringMap.get(invoke.getClass())).apply(invoke));
            } else if (invoke instanceof Enumeration) {
                Enumeration<?> invoke2 = (Enumeration<?>) invoke;
                description.append(getEnumerationDescription(fieldName, invoke2, toStringMap, invokedObjects, getterMethods));
            } else {
                description.append(invoke);
            }
            description.append("\n");
        }));
        return description.toString();
    }

    public static Map<String, String> getDescriptionMap(Object obj,
            Map<Class<?>, FunctionEx<Object, String>> toStringMap) {
        return getDescriptionMap(obj, obj.getClass(), toStringMap, new HashSet<>(), new HashMap<>(), new HashMap<>());

    }

    public static <T> Map<String, String> getDescriptionMap(T obj, Class<?> objClass,
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
            String fieldName = o.getName().replaceAll(METHOD_REGEX, "$1$2");
            StringBuilder description = new StringBuilder("\n");
            if (invoke != null && toStringMap.containsKey(invoke.getClass())) {
                description.append(FunctionEx.makeFunction(toStringMap.get(invoke.getClass())).apply(invoke));
            } else if (invoke instanceof Enumeration) {
                description.append(getEnumerationDescription(fieldName, (Enumeration<?>) invoke, toStringMap, invokedObjects, getterMethods));
            } else {
                description.append(invoke);
            }
            descriptionMap.put(fieldName, description.toString());
        }));
        return descriptionMap;
    }

    public static List<Method> getGetterMethods(Class<?> targetClass) {
        return getGetterMethods(targetClass, new HashMap<>());
    }

    private static void displayStyleClass(String n,Node node) {
        String arg1 = n + node.getClass().getSimpleName();
        if (node instanceof Labeled) {
            HasLogging.log(1).info("{} = {} = \"{}\"", arg1, node.getStyleClass(), ((Labeled) node).getText());
        }

        String id = node.getId();
        if (id != null) {
            HasLogging.log(1).info("{} = #{}.{}", arg1, id, node.getStyleClass());
        } else {
            HasLogging.log(1).info("{} = .{}", arg1, node.getStyleClass());
        }
        if(node instanceof Parent) {
            ObservableList<Node> childrenUnmodifiable = ((Parent) node).getChildrenUnmodifiable();
            childrenUnmodifiable.forEach(t -> ClassReflectionUtils.displayStyleClass(n+"-",t));
        }
    }

    private static <T> String getEnumerationDescription(String fieldName, Enumeration<T> enumeration,
            Map<Class<?>, FunctionEx<Object, String>> toStringMap, Set<Object> invoked, Map<Class<?>, List<Method>> getterMethods) {
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

    private static List<Method> getGetterMethods(Class<?> class1, Map<Class<?>, List<Method>> getterMethods) {
        if (!getterMethods.containsKey(class1)) {
            getterMethods.put(class1,
                    Stream.of(class1.getDeclaredMethods()).filter(m -> Modifier.isPublic(m.getModifiers()))
                            .filter(m -> m.getName().matches(METHOD_REGEX)).filter(m -> m.getParameterCount() == 0)
                            .sorted(Comparator.comparing(t -> t.getName().replaceAll(METHOD_REGEX, "$1$2")))
                            .collect(Collectors.toList()));
        }
        return getterMethods.get(class1);

    }



    private static String getText(File file) {
        try {
            if (file.exists()) {
                return Files.toString(file, StandardCharsets.UTF_8);
            }
        } catch (IOException e2) {
            LOG.error("", e2);
        }
        return "";
    }

    private static boolean isRecursiveCall(Class<?> class1, Object invoke) {
        return invoke instanceof Enumeration && invoke.getClass().getGenericInterfaces().length > 0 && invoke.getClass().getGenericInterfaces()[0].getTypeName().contains(class1.getName());
    }
}
