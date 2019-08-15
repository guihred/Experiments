package schema.sngpc;

import static java.util.stream.Collectors.toMap;
import static utils.ClassReflectionUtils.hasClass;
import static utils.ClassReflectionUtils.hasField;
import static utils.ClassReflectionUtils.mapProperty;

import com.google.common.collect.ImmutableMap;
import gaming.ex21.CatanApp;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.ConstraintsBase;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.assertj.core.api.exception.RuntimeIOException;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import utils.ClassReflectionUtils;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public final class FXMLCreator {
    private static final Logger LOG = HasLogging.log();
    private static final List<String> IGNORE = Arrays.asList("needsLayout", "layoutBounds", "baselineOffset",
        "localToParentTransform", "eventDispatcher", "skin", "background", "controlCssMetaData", "pseudoClassStates",
        "localToSceneTransform", "parentPopup", "cssMetaData", "classCssMetaData", "boundsInParent", "boundsInLocal",
        "scene", "childrenUnmodifiable", "styleableParent", "parent", "labelPadding");
    private static final List<Class<?>> ATTRIBUTE_CLASSES = Arrays.asList(Double.class, String.class, Color.class,
        Integer.class, Boolean.class, Pos.class, Orientation.class, TextAlignment.class, KeyCode.class, Enum.class,
        StrokeLineJoin.class, KeyCombination.class, KeyCodeCombination.class);
    private static final List<Class<?>> REFERENCE_CLASSES = Arrays.asList(ToggleGroup.class);
    private static final List<Class<?>> NEW_TAG_CLASSES = Arrays.asList(ConstraintsBase.class, EventTarget.class);
    private static final List<Class<?>> CONDITIONAL_TAG_CLASSES = Arrays.asList(Insets.class, Font.class, Point3D.class,
        Material.class, PropertyValueFactory.class, ConstraintsBase.class, EventTarget.class, Effect.class,
        StringConverter.class, SelectionModel.class);
    private static final Map<String, Function<Collection<?>, String>> FORMAT_LIST = ImmutableMap
        .<String, Function<Collection<?>, String>>builder()
        .put("styleClass", l -> l.stream().map(Object::toString).collect(Collectors.joining(" "))).build();
    private static final Map<String, String> PROPERTY_REMAP = ImmutableMap.<String, String>builder()
        .put("gridpane-column", "GridPane.columnIndex").put("gridpane-row", "GridPane.rowIndex").build();

    private FXMLCreator() {
    }

    public static void createXMLFile(Parent node, File file) {
        try {

            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Map<Object, org.w3c.dom.Node> nodeMap = new LinkedHashMap<>();
            Set<String> packages = new LinkedHashSet<>();
            List<Object> allNode = new ArrayList<>();
            Map<Object, String> referencedNodes = new HashMap<>();
            allNode.add(node);
            for (int i = 0; i < allNode.size(); i++) {
                Object node2 = allNode.get(i);
                org.w3c.dom.Node parent = nodeMap.getOrDefault(node2, document);
                String name = node2.getClass().getSimpleName().replaceAll("\\$", "_");
                String name2 = node2.getClass().getPackage().getName();
                if (node2.getClass().getEnclosingClass() != null) {
                    name2 = node2.getClass().getEnclosingClass().getName();
                }
                packages.add(name2);
                Element createElement = document.createElement(name);
                parent.appendChild(createElement);

                Map<String, Object> fields = differences(node2);
                fields.forEach((s, fieldValue) -> {
                    if (fieldValue != null) {
                        processField(document, nodeMap, allNode, createElement, s, fieldValue, node2, packages,
                            referencedNodes);
                    }
                });
                if (referencedNodes.containsKey(node2)) {
                    createElement.setAttribute("fx:id", referencedNodes.get(node2));
                }
                if (parent == document) {
                    createElement.setAttribute("xmlns:fx", "http://javafx.com/fxml");
                }
            }
            Node firstChild = document.getFirstChild();
            document.removeChild(firstChild);
            packages.forEach(p -> document.appendChild(document.createProcessingInstruction("import", p + ".*")));
            document.appendChild(firstChild);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
            transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
            transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(file);
            transformer.transform(domSource, streamResult);

        } catch (Exception e) {
            LOG.error("", e);
            throw new RuntimeIOException("ERROR in file " + file.getName(), e);
        }
    }

    public static Map<String, Object> differences(Object ob1) {
        Map<String, Object> diffFields = new LinkedHashMap<>();
        Class<?> cl = ob1.getClass();
        try {
            List<Method> fields = ClassReflectionUtils.getGetterMethodsRecursive(cl);
            Object ob2 = getInstance(cl);
            for (Method f : fields) {
                Object fieldValue = ClassReflectionUtils.invoke(ob1, f);
                Object fieldValue2 = ClassReflectionUtils.invoke(ob2, f);
                if (!Objects.equals(fieldValue, fieldValue2)) {
                    String fieldName = ClassReflectionUtils.getFieldNameCase(f);
                    LOG.trace("{} {}!={} ", fieldName, fieldValue, fieldValue2);
                    diffFields.put(fieldName, fieldValue);
                }
            }
        } catch (Exception e) {
            LOG.trace("", e);
            List<String> fields = ClassReflectionUtils.getNamedArgs(cl);
            Map<String, Object> collect = fields.stream().filter(m -> ClassReflectionUtils.invoke(ob1, m) != null)
                .collect(toMap(m -> m, m -> ClassReflectionUtils.invoke(ob1, m), (a, b) -> a != null ? a : b));
            diffFields.putAll(collect);
        }
        return diffFields;
    }

    public static void duplicate(String out) {
        if (Platform.isFxApplicationThread()) {
            duplicateStage(ResourceFXUtils.getOutFile(out));
        } else {
            ResourceFXUtils.initializeFX();
            Platform.runLater(() -> duplicateStage(ResourceFXUtils.getOutFile(out)));
        }
    }

    public static void duplicateStage(File file) {
        duplicateStage(file, file.getName());
    }

    public static Stage duplicateStage(File file, String title) {
        Stage primaryStage = new Stage();
        try {
            Parent content = FXMLLoader.load(file.toURI().toURL());
            Scene scene = new Scene(content);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            LOG.error("", e);
            throw new RuntimeIOException("ERROR in file " + file, e);
        }
        return primaryStage;
    }

    public static void main(String[] argv) {
        List<Class<? extends Application>> asList = Arrays.asList(CatanApp.class);
        testApplications(asList, true);
        for (Class<? extends Application> class1 : asList) {
            duplicate(class1.getSimpleName() + ".fxml");
        }
    }

    @SafeVarargs
    public static List<Class<?>> testApplications(Class<? extends Application>... asList) {
        return testApplications(Arrays.asList(asList));
    }

    public static List<Class<?>> testApplications(List<Class<? extends Application>> asList) {
        return testApplications(asList, true);

    }

    public static List<Class<?>> testApplications(List<Class<? extends Application>> asList, boolean close) {
        ResourceFXUtils.initializeFX();
        List<Class<?>> errorClasses = new ArrayList<>();
        for (Class<? extends Application> class1 : asList) {
            Platform.runLater(RunnableEx.make(() -> {
                Application a = class1.newInstance();
                Stage primaryStage = new Stage();
                primaryStage.setTitle(class1.getSimpleName());
                a.start(primaryStage);
                primaryStage.toBack();
                File outFile = ResourceFXUtils.getOutFile(class1.getSimpleName() + ".fxml");
                createXMLFile(primaryStage.getScene().getRoot(), outFile);
                Stage duplicateStage = duplicateStage(outFile, primaryStage.getTitle());
                if (close) {
                    primaryStage.close();
                    duplicateStage.close();
                }
                LOG.info("{} successfull", class1.getSimpleName());
            }, error -> {
                LOG.error("ERROR IN " + class1, error);
                errorClasses.add(class1);
            }));

        }
        return errorClasses;
    }

    private static Object getInstance(Class<?> cl) {
        try {
            return cl.newInstance();
        } catch (Exception e) {
            LOG.trace("", e);
        }
        try {
            return cl.getConstructors()[0].newInstance(100);
        } catch (Exception e) {
            LOG.trace("", e);
        }
        try {
            return cl.getConstructors()[0].newInstance("");
        } catch (Exception e) {
            LOG.trace("", e);
        }
        try {
            return cl.getConstructors()[0].newInstance((Object[]) null);
        } catch (Exception e) {
            LOG.trace("", e);
            throw new RuntimeIOException("ERROR IN INSTANTIATION", e);
        }
    }

    private static void processField(Document document, Map<Object, org.w3c.dom.Node> nodeMap, List<Object> allNode,
        Element element, String fieldName, Object fieldValue, Object parent, Set<String> packages,
        Map<Object, String> referencedNodes) {

        if (IGNORE.contains(fieldName)) {
            return;
        } else if (allNode.stream().anyMatch(ob -> ob == fieldValue)) {
            List<String> namedArgs = ClassReflectionUtils.getNamedArgs(parent.getClass());
            if (namedArgs.contains(fieldName)) {
                Node node = nodeMap.get(fieldValue);
                List<Object> collect = nodeMap.entrySet().stream().filter(e -> e.getValue() == node)
                    .map(e -> e.getKey()).collect(Collectors.toList());
                if (!referencedNodes.containsKey(fieldValue)) {
                    for (int i = 0; i < collect.size(); i++) {
                        Object object = collect.get(i);
                        referencedNodes.put(object, object.getClass().getSimpleName() + referencedNodes.size());
                    }
                }
            }
            return;
        }

        if (fieldValue instanceof Collection) {
            Collection<?> list = (Collection<?>) fieldValue;
            if (list.isEmpty()) {
                return;
            }
            if (FORMAT_LIST.containsKey(fieldName)) {
                String apply = FORMAT_LIST.get(fieldName).apply(list);
                element.setAttribute(fieldName, apply);
            } else if (list.stream().filter(e -> e != null).anyMatch(o -> hasClass(NEW_TAG_CLASSES, o.getClass()))) {
                if (list.stream().anyMatch(o -> !allNode.contains(o))) {
                    Element createElement2 = document.createElement(fieldName);
                    element.appendChild(createElement2);
                    for (Object object : list) {
                        if (object != null && !allNode.contains(object)) {
                            nodeMap.put(object, createElement2);
                            allNode.add(object);
                        }
                    }
                }
            } else if (list.stream().filter(e -> e != null).anyMatch(o -> hasClass(ATTRIBUTE_CLASSES, o.getClass()))) {
                Element createElement2 = document.createElement(fieldName);
                element.appendChild(createElement2);
                if (hasField(parent.getClass(), fieldName)) {
                    Element createElement = document.createElement("FXCollections");
                    packages.add("javafx.collections");
                    createElement2.appendChild(createElement);
                    createElement.setAttribute("fx:factory", "observableArrayList");
                    createElement2 = createElement;
                }

                for (Object object : list) {
                    if (object != null) {
                        packages.add(object.getClass().getPackage().getName());
                        Element createElement3 = document.createElement(object.getClass().getSimpleName());
                        createElement3.setAttribute("fx:value", object + "");
                        createElement2.appendChild(createElement3);
                    }
                }
            } else {
                String classes = list.stream().findFirst().map(Object::getClass).map(Class::getName).orElse("");
                Class<? extends Object> parentClass = parent.getClass();
                LOG.info("attribute {} type {} of {} not set", fieldName, classes, parentClass);
                LOG.info("value {}", list);
            }
        } else if (hasClass(CONDITIONAL_TAG_CLASSES, fieldValue.getClass()) && hasField(parent.getClass(), fieldName)) {
            Element createElement2 = document.createElement(fieldName);
            element.appendChild(createElement2);
            nodeMap.put(fieldValue, createElement2);
            allNode.add(fieldValue);
        } else if (hasClass(ATTRIBUTE_CLASSES, fieldValue.getClass()) && hasField(parent.getClass(), fieldName)) {
            Object mapProperty2 = mapProperty(fieldValue);
            element.setAttribute(fieldName, mapProperty2 + "");
        } else if (fieldValue instanceof Map) {
            Map<?, ?> properties = (Map<?, ?>) fieldValue;
            properties.forEach((k, v) -> {
                String string = Objects.toString(k);
                if (PROPERTY_REMAP.containsKey(string)) {
                    String key = PROPERTY_REMAP.get(string);
                    String value = Objects.toString(v);
                    element.setAttribute(key, value);
                }
            });
        } else if (hasClass(REFERENCE_CLASSES, fieldValue.getClass())) {
            int count = referencedNodes.size();
            String simpleName = fieldValue.getClass().getSimpleName();
            if (!referencedNodes.containsKey(fieldValue)) {
                Map<String, Object> differences = differences(fieldValue);
                Element defineElement = document.createElement("fx:define");
                element.getParentNode().appendChild(defineElement);
                Element createElement = document.createElement(simpleName);
                defineElement.appendChild(createElement);
                differences.forEach((k, v) -> {
                    if (hasClass(ATTRIBUTE_CLASSES, v.getClass())) {
                        Object mapProperty2 = mapProperty(v);
                        createElement.setAttribute(k, mapProperty2 + "");
                    }
                });
                referencedNodes.put(fieldValue, simpleName + count);
                createElement.setAttribute("fx:id", simpleName + count);
            }

            element.setAttribute(fieldName, "$" + referencedNodes.get(fieldValue));
        } else {
            if (!hasClass(ATTRIBUTE_CLASSES, fieldValue.getClass())) {
                Class<? extends Object> class1 = fieldValue.getClass();
                List<Class<?>> allClasses = ClassReflectionUtils.allClasses(class1);
                LOG.info(" {} not in ATTRIBUTE_CLASSES", allClasses, parent.getClass());
            }
            if (hasField(parent.getClass(), fieldName)) {
                LOG.info("{} does have {}", parent.getClass(), fieldName);
            } else {
                LOG.info("{} does not have {}", parent.getClass(), fieldName);
            }

        }

    }

}
