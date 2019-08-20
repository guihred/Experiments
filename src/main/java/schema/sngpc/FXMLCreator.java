package schema.sngpc;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static utils.ClassReflectionUtils.*;

import com.google.common.collect.ImmutableMap;
import contest.db.ContestApplication;
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
import javafx.geometry.Point3D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.ConstraintsBase;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
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
import org.w3c.dom.NodeList;
import utils.ClassReflectionUtils;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public final class FXMLCreator {
    private static final String FX_REFERENCE = "fx:reference";

    private static final String FX_FACTORY = "fx:factory";
    private static final String FX_ID = "fx:id";
    private static final String FX_VALUE = "fx:value";
    private static final String FX_DEFINE = "fx:define";
    private static final Logger LOG = HasLogging.log();
    private static final List<String> IGNORE = Arrays.asList("needsLayout", "layoutBounds", "baselineOffset",
        "localToParentTransform", "eventDispatcher", "skin", "background", "controlCssMetaData", "pseudoClassStates",
        "localToSceneTransform", "parentPopup", "cssMetaData", "classCssMetaData", "boundsInParent", "boundsInLocal",
        "scene", "childrenUnmodifiable", "styleableParent", "parent", "labelPadding");
    private static final List<Class<?>> ATTRIBUTE_CLASSES = Arrays.asList(Double.class, String.class, Color.class,
        Long.class, Integer.class, Boolean.class, Enum.class, KeyCombination.class);
    private static final List<Class<?>> REFERENCE_CLASSES = Arrays.asList(ToggleGroup.class);
    private static final List<Class<?>> NEW_TAG_CLASSES = Arrays.asList(ConstraintsBase.class, EventTarget.class);
    private static final List<Class<?>> CONDITIONAL_TAG_CLASSES = Arrays.asList(Insets.class, Font.class, Point3D.class,
        Material.class, PropertyValueFactory.class, ConstraintsBase.class, EventTarget.class, Effect.class, Path.class,
        StringConverter.class, SelectionModel.class, Color.class, Enum.class);
    private static final Map<String, Function<Collection<?>, String>> FORMAT_LIST = ImmutableMap
        .<String, Function<Collection<?>, String>>builder()
        .put("styleClass", l -> l.stream().map(Object::toString).collect(Collectors.joining(" "))).build();
    private static final Map<String, String> PROPERTY_REMAP = ImmutableMap.<String, String>builder()
        .put("gridpane-column", "GridPane.columnIndex").put("gridpane-row", "GridPane.rowIndex")
        .put("hbox-hgrow", "HBox.hgrow").put("vbox-vgrow", "VBox.vgrow").put("tilepane-alignment", "TilePane.alignment")
        .put("stackpane-alignment", "StackPane.alignment").put("pane-bottom-anchor", "AnchorPane.bottomAnchor")
        .put("pane-right-anchor", "AnchorPane.rightAnchor").put("pane-left-anchor", "AnchorPane.leftAnchor")
        .put("pane-top-anchor", "AnchorPane.topAnchor").put("borderpane-alignment", "BorderPane.alignment")
        .put("gridpane-halignment", "GridPane.halignment").put("gridpane-valignment", "GridPane.valignment")
        .put("gridpane-column-span", "GridPane.columnSpan").put("gridpane-row-span", "GridPane.rowSpan").build();
    private Document document;
    private Map<Object, org.w3c.dom.Node> nodeMap = new IdentityHashMap<>();

    private List<Object> allNode = new ArrayList<>();

    private Set<String> packages = new LinkedHashSet<>();

    private Map<Object, String> referencedNodes = new IdentityHashMap<>();
    private FXMLCreator() {
    }

    public void createFXMLFile(Parent node, File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute("http://javax.xml.XMLConstants/feature/secure-processing", true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            document = documentBuilder.newDocument();
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
                if (hasClass(ATTRIBUTE_CLASSES, node2.getClass())) {
                    createElement.setAttribute(FX_VALUE, Objects.toString(node2, ""));
                    fields.clear();
                }

                fields.forEach((s, fieldValue) -> {
                    if (fieldValue != null) {
                        processField(createElement, s, fieldValue, node2);
                    }
                });
                referencedNodes.computeIfAbsent(node2, this::newName);
                if (referencedNodes.containsKey(node2)) {
                    createElement.setAttribute(FX_ID, referencedNodes.get(node2));
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
            throw new RuntimeIOException("ERROR in file " + file.getName(), e);
        }
    }

    private void createReferenceNode(Element element, String fieldName, Object fieldValue) {
        int count = referencedNodes.size();
        String simpleName = fieldValue.getClass().getSimpleName();
        if (!referencedNodes.containsKey(fieldValue)) {
            Map<String, Object> differences = differences(fieldValue);
            Element defineElement = document.createElement(FX_DEFINE);
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
            createElement.setAttribute(FX_ID, simpleName + count);
        }

        element.setAttribute(fieldName, "$" + referencedNodes.get(fieldValue));
    }

    private String newName(Object f) {
        return f.getClass().getSimpleName() + referencedNodes.size();
    }

    private void processField(Element element, String fieldName, Object fieldValue, Object parent) {
        if (IGNORE.contains(fieldName)) {
            return;
        }
        if (containsSame(allNode, fieldValue)) {
            processNamedArgs(element, fieldName, fieldValue, parent);
            return;
        }

        if (fieldValue instanceof Collection) {
            processList(element, fieldName, parent, (Collection<?>) fieldValue);
            return;
        }
        if (hasClass(ATTRIBUTE_CLASSES, fieldValue.getClass()) && hasField(parent.getClass(), fieldName)
            && isSetterMatches(fieldName, fieldValue, parent)) {
            Object mapProperty2 = mapProperty(fieldValue);
            element.setAttribute(fieldName, mapProperty2 + "");
            return;
        }
        if (hasClass(CONDITIONAL_TAG_CLASSES, fieldValue.getClass()) && hasField(parent.getClass(), fieldName)) {
            Element createElement2 = document.createElement(fieldName);
            element.appendChild(createElement2);
            nodeMap.put(fieldValue, createElement2);
            allNode.add(fieldValue);
            return;
        }
        if (fieldValue instanceof Map) {
            processMap(element, fieldValue);
            return;
        }
        if (hasClass(REFERENCE_CLASSES, fieldValue.getClass())) {
            createReferenceNode(element, fieldName, fieldValue);
            return;
        }
        if (!hasClass(ATTRIBUTE_CLASSES, fieldValue.getClass())) {
            Class<? extends Object> class1 = fieldValue.getClass();
            List<Class<?>> allClasses = allClasses(class1);
            LOG.info(" {} not in ATTRIBUTE_CLASSES", allClasses);
        }
        if (hasField(parent.getClass(), fieldName)) {
            LOG.info("{} does have {}", parent.getClass(), fieldName);
        }

    }

    private void processList(Element element, String fieldName, Object parent, Collection<?> list) {
        if (list.isEmpty()) {
            return;
        }
        if (FORMAT_LIST.containsKey(fieldName)) {
            String apply = FORMAT_LIST.get(fieldName).apply(list);
            element.setAttribute(fieldName, apply);
            return;
        }
        if (list.stream().filter(Objects::nonNull).anyMatch(o -> hasClass(ATTRIBUTE_CLASSES, o.getClass()))) {
            Element originalElement = document.createElement(fieldName);
            element.appendChild(originalElement);
            if (hasField(parent.getClass(), fieldName) && isSetterMatches(fieldName, list, parent)) {
                Element collectionsElement = document.createElement("FXCollections");
                packages.add("javafx.collections");
                originalElement.appendChild(collectionsElement);
                collectionsElement.setAttribute(FX_FACTORY, "observableArrayList");
                originalElement = collectionsElement;
            }
            Element appendTo = originalElement;
            list.stream().filter(Objects::nonNull).forEach(object -> {
                packages.add(object.getClass().getPackage().getName());
                Element inlineEl = document.createElement(object.getClass().getSimpleName());
                inlineEl.setAttribute(FX_VALUE, object + "");
                appendTo.appendChild(inlineEl);
            });
            return;
        }
        if (list.stream().filter(Objects::nonNull).anyMatch(o -> hasClass(NEW_TAG_CLASSES, o.getClass()))) {
            if (list.stream().anyMatch(o -> !containsSame(allNode, o))) {
                Element createElement2 = document.createElement(fieldName);
                element.appendChild(createElement2);
                if (hasField(parent.getClass(), fieldName)) {
                    Element createElement = document.createElement("FXCollections");
                    packages.add("javafx.collections");
                    createElement2.appendChild(createElement);
                    createElement.setAttribute(FX_FACTORY, "observableArrayList");
                    createElement2 = createElement;
                }
                for (Object object : list) {
                    if (object != null && !containsSame(allNode, object)) {
                        nodeMap.put(object, createElement2);
                        allNode.add(object);
                    }
                }
            }
            return;
        }
        String classes = list.stream().findFirst().map(Object::getClass).map(Class::getName).orElse("");
        Class<? extends Object> parentClass = parent.getClass();
        LOG.info("attribute {} type {} of {} not set", fieldName, classes, parentClass);
        LOG.info("value {}", list);
    }

    private void processNamedArgs(Element element, String fieldName, Object fieldValue, Object parent) {
        List<String> namedArgs = getNamedArgs(parent.getClass());
        if (namedArgs.contains(fieldName)) {
            String newFieldId = referencedNodes.computeIfAbsent(fieldValue, this::newName);
            element.setAttribute(fieldName, "$" + newFieldId);
            if (allNode.indexOf(parent) < allNode.indexOf(fieldValue)) {
                NodeList elementsByTagName = document.getElementsByTagName(FX_DEFINE);
                if (elementsByTagName.getLength() == 0) {
                    Element firstChild = (Element) document.getFirstChild();
                    Element createElement = document.createElement(FX_DEFINE);
                    NodeList childNodes = firstChild.getChildNodes();
                    List<Node> removedElements = new ArrayList<>();
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        Node item = childNodes.item(i);
                        removedElements.add(item);
                        firstChild.removeChild(item);
                    }
                    firstChild.appendChild(createElement);
                    for (int i = 0; i < removedElements.size(); i++) {
                        Node item = removedElements.get(i);
                        firstChild.appendChild(item);
                    }
                }
                Node node = nodeMap.get(fieldValue);
                if (!node.getNodeName().equals(FX_DEFINE)) {
                    Element referenceTag = document.createElement(FX_REFERENCE);
                    referenceTag.setAttribute("source", newFieldId);
                    node.appendChild(referenceTag);
                    nodeMap.put(fieldValue, document.getElementsByTagName(FX_DEFINE).item(0));
                }
            }
        }
    }

    public static void createXMLFile(Parent node, File file) {
        new FXMLCreator().createFXMLFile(node, file);
    }

    public static Map<String, Object> differences(Object ob1) {
        Map<String, Object> diffFields = new LinkedHashMap<>();
        Class<?> cl = ob1.getClass();
        try {
            List<Method> fields = getGetterMethodsRecursive(cl);
            Object ob2 = getInstance(cl);
            for (Method f : fields) {
                Object fieldValue = invoke(ob1, f);
                Object fieldValue2 = invoke(ob2, f);
                if (!Objects.equals(fieldValue, fieldValue2)) {
                    String fieldName = getFieldNameCase(f);
                    LOG.trace("{} {}!={} ", fieldName, fieldValue, fieldValue2);
                    diffFields.put(fieldName, fieldValue);
                }
            }
        } catch (Exception e) {
            LOG.trace("", e);
            List<String> fields = getNamedArgs(cl);
            fields.addAll(getGetterMethodsRecursive(cl, 1).stream().map(ClassReflectionUtils::getFieldNameCase)
                .filter(t -> !fields.contains(t)).collect(toList()));
            Map<String, Object> collect = fields.stream().filter(m -> invoke(ob1, m) != null)
                .collect(toMap(m -> m, m -> invoke(ob1, m), (a, b) -> a != null ? a : b));
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
            throw new RuntimeIOException("ERROR in file " + file, e);
        }
        return primaryStage;
    }

    public static void main(String[] argv) {
        List<Class<? extends Application>> asList = Arrays.asList(ContestApplication.class);
        testApplications(asList, false);
//        for (Class<? extends Application> class1 : asList) {
//            duplicate(class1.getSimpleName() + ".fxml");
//        }
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
            List<Stage> stages = new ArrayList<>();

            Platform.runLater(RunnableEx.make(() -> {
                testSingleApp(class1, stages, close);
            }, error -> {
                LOG.error("ERROR IN {} ", class1);
                LOG.error("", error);
                errorClasses.add(class1);
                if (close) {
                    stages.forEach(Stage::close);
                }
            }));

        }
        return errorClasses;
    }

    private static boolean containsSame(List<Object> allNode, Object fieldValue) {
        return allNode.stream().anyMatch(ob -> ob == fieldValue);
    }

    private static Object getInstance(Class<?> cl) {
        try {
            return cl.newInstance();
        } catch (Exception e) {
            throw new RuntimeIOException("ERROR IN INSTANTIATION", e);
        }
    }

    private static void processMap(Element element, Object fieldValue) {
        Map<?, ?> properties = (Map<?, ?>) fieldValue;
        properties.forEach((k, v) -> {
            String string = Objects.toString(k);
            if (PROPERTY_REMAP.containsKey(string)) {
                String key = PROPERTY_REMAP.get(string);
                String value = Objects.toString(v);
                element.setAttribute(key, value);
            } else {
                LOG.info("property {} value {} NOT IN PROPERTY_REMAP", k, v);
            }
        });
    }

    private static void testSingleApp(Class<? extends Application> appClass, List<Stage> stages, boolean close)
        throws Exception {
        LOG.info("INITIALIZING {}", appClass.getSimpleName());
        Application a = appClass.newInstance();
        Stage primaryStage = new Stage();
        stages.add(primaryStage);
        primaryStage.setTitle(appClass.getSimpleName());
        a.start(primaryStage);
        primaryStage.toBack();
        File outFile = ResourceFXUtils.getOutFile(appClass.getSimpleName() + ".fxml");
        Parent root = primaryStage.getScene().getRoot();
        root.getStylesheets().addAll(primaryStage.getScene().getStylesheets());
        LOG.info("CREATING {}.fxml", appClass.getSimpleName());
        createXMLFile(root, outFile);
        Stage duplicateStage = duplicateStage(outFile, primaryStage.getTitle());
        duplicateStage.toBack();
        stages.add(duplicateStage);
        if (close) {
            stages.forEach(Stage::close);
        }
        String original = ClassReflectionUtils.displayStyleClass(root).replaceAll("#\\w+", "");
        Parent root2 = duplicateStage.getScene().getRoot();
        String generated = ClassReflectionUtils.displayStyleClass(root2).replaceAll("#\\w+", "")
            .replaceFirst(" " + root.getStyleClass(), "");
        if (!original.equals(generated)) {
            LOG.info("{} has different tree", appClass.getSimpleName());
        }
        LOG.info("{} successfull", appClass.getSimpleName());
    }


}
