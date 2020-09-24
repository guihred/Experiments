
package ml.data;

import static utils.ex.FunctionEx.makeFunction;
import static utils.ex.RunnableEx.runNewThread;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleDialogBuilder;
import utils.ClassReflectionUtils;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.ConsumerEx;
import utils.ex.SupplierEx;

/**
 * @author guigu
 *
 */
public class Mapping {
    private static List<Method> METHODS;

    public static List<Method> getMethods() {
        List<Class<?>> allowedTypes =
                Arrays.asList(Double.class, String.class, Integer.class, Long.class, int.class, Object.class);
        return SupplierEx.orElse(METHODS,
                () -> METHODS = JavaFileDependency.getAllFileDependencies().stream()
                        .map(JavaFileDependency::getFullName).filter(s -> !s.startsWith("fxtests"))
                        .filter(s -> !s.contains("HibernateUtil")).map(makeFunction(Class::forName))
                        .flatMap(e -> ClassReflectionUtils.getAllMethodsRecursive(e, 2).stream()
                                .filter(m -> Modifier.isStatic(m.getModifiers()))
                                .filter(m -> Modifier.isPublic(m.getModifiers())).filter(m -> m.getParameterCount() > 0)
                                .filter(m -> Stream.of(m.getParameterTypes()).allMatch(allowedTypes::contains))
                                .filter(m -> allowedTypes.contains(m.getReturnType())))
                        .distinct().sorted(Comparator.comparing((Method m) -> m.getDeclaringClass().getSimpleName())
                                .thenComparing(Method::getName))
                        .collect(Collectors.toList()));
    }

    public static void showDialog(Node barChart, String[] dependencies, DataframeML dataframe, ConsumerEx<File> run) {
        List<Class<?>> allowedTypes = Stream.of(dependencies).map(dataframe::getFormat).collect(Collectors.toList());
        ObservableList<Method> methods2 = FXCollections.observableArrayList(Mapping.getMethods())
                .filtered(m -> Stream.of(m.getParameterTypes()).anyMatch(allowedTypes::contains));
        VBox vBox = new VBox();

        ComboBox<Method> build = new SimpleComboBoxBuilder<>(methods2).select(0).converter(Mapping::methodName)
                .onChange((old, method) -> adjustToMethod(dependencies, vBox, method)).build();

        TextField button = new TextField(Stream.of(dependencies).collect(Collectors.joining("_")) + 1);
        SimpleDialogBuilder dialog = new SimpleDialogBuilder().bindWindow(barChart).node(button);
        for (String string : dependencies) {
            dialog.text(string + " (" + dataframe.getFormat(string).getSimpleName() + ")");
        }

        dialog.node(build).node(vBox).button("Add", () -> {
            Method method = build.getSelectionModel().getSelectedItem();
            SimpleDoubleProperty progress = new SimpleDoubleProperty(0);
            Object[] ob = new Object[method.getParameterCount()];
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                if (i >= dependencies.length) {
                    TextField node = (TextField) vBox.getChildren().get(i - dependencies.length);
                    Object tryAsNumber =
                            StringSigaUtils.formatHierarchy().getOrDefault(parameterTypes[i], e -> e)
                                    .apply(node.getText());
                    ob[i] = tryAsNumber;
                }
            }
            runNewThread(() -> DataframeUtils.crossFeatureObject(dataframe, button.getText(), progress, o -> {
                for (int i = 0; i < o.length; i++) {
                    ob[i] = o[i];
                }
                return method.invoke(null, ob);
            }, dependencies));
            return progress;
        }, () -> {
            File outFile = ResourceFXUtils.getOutFile("csv/" + dataframe.getFile().getName());
            DataframeUtils.save(dataframe, outFile);
            ConsumerEx.accept(run, outFile);
        }).displayDialog();
    }

    private static void adjustToMethod(String[] dependencies, VBox vBox, Method n) {
        vBox.getChildren().clear();
        Class<?>[] parameterTypes = n.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i >= dependencies.length) {
                TextField textField = new TextField();
                vBox.getChildren().add(textField);
            }
        }
    }

    private static String methodName(Method m) {
        return String.format("%s %s.%s(%s)", m.getReturnType().getSimpleName(), m.getDeclaringClass().getSimpleName(),
                m.getName(),
                Stream.of(m.getParameterTypes()).map(e -> e.getSimpleName()).collect(Collectors.joining(",")));
    }

}