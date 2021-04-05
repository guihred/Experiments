
package ml.data;

import static utils.ex.FunctionEx.makeFunction;
import static utils.ex.PredicateEx.makeTest;
import static utils.ex.RunnableEx.runNewThread;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import simplebuilder.ListHelper;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleDialogBuilder;
import utils.ClassReflectionUtils;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.ConsumerEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;
import utils.fx.AutocompleteField;

/**
 * @author guigu
 *
 */
public final class Mapping {
    private static List<Method> methods;

    private static final Logger LOG = HasLogging.log();

    private Mapping() {
    }

    public static DoubleExpression addMapping(DataframeML dataframe, Method method, String crossFeatureName,
            String[] dependencies, List<Object> otherParams) {
        SimpleDoubleProperty progress = new SimpleDoubleProperty(0);
        runNewThread(() -> addMapping(dataframe, method, crossFeatureName, dependencies, otherParams, progress));
        return progress;
    }

    public static void addMapping(DataframeML dataframe, Method method, String crossFeatureName, String[] dependencies,
            List<Object> otherParams, SimpleDoubleProperty progress) {
        Object[] ob = new Object[method.getParameterCount()];
        for (int i = 0; i < otherParams.size(); i++) {
            ob[i + dependencies.length] = otherParams.get(i);
        }
        String strDepen = Arrays.toString(dependencies);
        LOG.info("RUNNING {} {} {}", method, strDepen, otherParams);
        if (!dataframe.isLoaded()) {
            DataframeML build2 =
                    DataframeBuilder.builder(dataframe.getFile()).addCrossFeature(crossFeatureName, dependencies, o -> {
                        for (int i = 0; i < o.length; i++) {
                            ob[i] = o[i];
                        }
                        return method.invoke(null, ob);
                    }).build(progress);
            dataframe.getDataframe().putAll(build2.getDataframe());
            dataframe.getFormatMap().putAll(build2.getFormatMap());
            return;
        }
        DataframeUtils.crossFeatureObject(dataframe, crossFeatureName, progress, o -> {
            for (int i = 0; i < o.length; i++) {
                ob[i] = o[i];
            }
            return method.invoke(null, ob);
        }, dependencies);
    }

    public static Method getMethod(String name) {
        return getMethods().stream().filter(m -> Mapping.methodName(m).equals(name)).findFirst()
                .orElseThrow(() -> RunnableEx.newException(name + " DOES NOT EXIST", null));
    }

    public static synchronized List<Method> getMethods() {
        List<Class<?>> allowedTypes = Arrays.asList(Double.class, String.class, Integer.class, Long.class, int.class,
                Object.class, long.class, double.class, Number.class);
        List<Class<?>> returnTypes = Arrays.asList(Double.class, String.class, Integer.class, Long.class, boolean.class,
                int.class, Object.class, long.class, double.class, Map.class, List.class, Collection.class,
                String[].class);
        return SupplierEx.orElse(methods, () -> methods = loadMethods(returnTypes, allowedTypes));
    }

    public static void showDialog(Node barChart, String[] dependencies, DataframeML dataframe, ConsumerEx<File> run) {
        List<Class<?>> allowedTypes = Stream.of(dependencies).map(dataframe::getFormat).collect(Collectors.toList());
        ObservableList<Method> methods2 = FXCollections.observableArrayList(Mapping.getMethods())
                .filtered(m -> ClassReflectionUtils.isAllowed(allowedTypes, m.getParameterTypes()));
        VBox vBox = new VBox();
        ComboBox<Method> methodsCombo =
                new SimpleComboBoxBuilder<>(methods2).id("methodCombo").select(0).converter(Mapping::methodName)
                        .onChange((old, method) -> adjustToMethod(dependencies, vBox, method)).build();
        AutocompleteField autocompleteField = new AutocompleteField();
        autocompleteField.setOnTextSelected(result -> {
            Method byName = methods2.stream().filter(m -> Mapping.methodName(m).equals(result)).findFirst()
                    .orElse(methodsCombo.getSelectionModel().getSelectedItem());
            methodsCombo.getSelectionModel().select(byName);
            return "";
        });
        autocompleteField.setEntries(ListHelper.mapping(methods2, Mapping::methodName));
        TextField crossFeature = new TextField(Stream.of(dependencies).collect(Collectors.joining("_")) + 1);
        SimpleDialogBuilder dialog = new SimpleDialogBuilder().bindWindow(barChart).node(crossFeature);
        for (String string : dependencies) {
            dialog.text(string + " (" + dataframe.getFormat(string).getSimpleName() + ")");
        }
        dialog.node(autocompleteField, methodsCombo).node(vBox).button("Add", () -> {
            Method method = methodsCombo.getSelectionModel().getSelectedItem();
            List<Object> otherParams = otherParams(dependencies, vBox, method);
            return addMapping(dataframe, method, crossFeature.getText(), dependencies, otherParams);
        }, () -> {
            File outFile = ResourceFXUtils.getOutFile("csv/" + dataframe.getFile().getName());
            DataframeUtils.save(dataframe, outFile);
            ConsumerEx.accept(run, outFile);
        }).title("Map (" + StringUtils.join(dependencies, ",") + ")").displayDialog();
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

    private static List<Method> loadMethods(List<Class<?>> returnTypes, List<Class<?>> allowedTypes) {
        List<Method> collect =
                JavaFileDependency.getAllFileDependencies().stream()
                        .filter(makeTest(e -> !e.getPublicStaticMethods().isEmpty()))
                        .map(JavaFileDependency::getFullName)
                        .filter(s -> !s.startsWith("fxtests"))
                        .filter(s -> !s.contains("HibernateUtil"))
                        .map(makeFunction(Class::forName))
                        .flatMap(e -> ClassReflectionUtils.getAllMethodsRecursive(e, 2).stream()
                                .filter(m -> Modifier.isStatic(m.getModifiers()))
                                .filter(m -> Modifier.isPublic(m.getModifiers()))
                                .filter(m -> Stream.of(m.getParameterTypes()).allMatch(allowedTypes::contains))
                                .filter(m -> returnTypes.contains(m.getReturnType())))
                        .distinct().sorted(Comparator.comparing((Method m) -> m.getDeclaringClass().getSimpleName())
                        .thenComparing(Method::getName))
                .collect(Collectors.toList());
        LOG.info("Methods Loaded");
        return collect;
    }

    private static String methodName(Method m) {
        return String.format("%s %s.%s(%s)", m.getReturnType().getSimpleName(), m.getDeclaringClass().getSimpleName(),
                m.getName(),
                Stream.of(m.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(",")));
    }

    private static List<Object> otherParams(String[] dependencies, VBox vBox, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        List<Object> otherParams = new ArrayList<>();
        for (int i = dependencies.length; i < parameterTypes.length; i++) {
            String text = ((TextField) vBox.getChildren().get(i - dependencies.length)).getText();
            Object tryAsNumber =
                    StringSigaUtils.FORMAT_HIERARCHY_MAP.getOrDefault(parameterTypes[i], e -> e).apply(text);
            otherParams.add(tryAsNumber);
        }
        return otherParams;
    }

}