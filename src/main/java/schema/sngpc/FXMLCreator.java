package schema.sngpc;

import static utils.ClassReflectionUtils.hasField;
import static utils.ClassReflectionUtils.mapProperty;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.ConstraintsBase;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javax.xml.XMLConstants;
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
import paintexp.PaintMain;
import utils.ClassReflectionUtils;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public final class FXMLCreator {
    private static final Logger LOG = HasLogging.log();
    private static final List<String> IGNORE = Arrays.asList("needsLayout", "layoutBounds", "baselineOffset",
        "localToParentTransform", "eventDispatcher", "skin", "background", "controlCssMetaData",
        "localToSceneTransform", "parentPopup", "cssMetaData", "classCssMetaData", "boundsInParent", "boundsInLocal",
        "scene", "childrenUnmodifiable", "styleableParent", "parent", "labelPadding");
    private static final List<Class<?>> ATTRIBUTE_CLASSES = Arrays.asList(Double.class, String.class, Color.class,
        Integer.class, Boolean.class, Pos.class, Orientation.class, TextAlignment.class, KeyCode.class,
        KeyCombination.class);
    private static final List<Class<?>> NEW_TAG_CLASSES = Arrays.asList(ConstraintsBase.class, EventTarget.class);
    private static final List<Class<?>> CONDITIONAL_TAG_CLASSES = Arrays.asList(Insets.class, Font.class, Point3D.class,
        Material.class, PropertyValueFactory.class);
    private static final Map<String, Function<Collection<?>, String>> FORMAT_LIST = ImmutableMap
        .<String, Function<Collection<?>, String>>builder()
        .put("styleClass", l -> l.stream().map(Object::toString).collect(Collectors.joining(" "))).build();

    private FXMLCreator() {
    }

    public static void createXMLFile(Parent node, File file) {
        try {

            System.setProperty(XMLConstants.FEATURE_SECURE_PROCESSING, "true");
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Map<Object, org.w3c.dom.Node> nodeMap = new HashMap<>();
            List<String> packages = new ArrayList<>();
            List<Object> allNode = new ArrayList<>();
            allNode.add(node);
            for (int i = 0; i < allNode.size(); i++) {
                Object node2 = allNode.get(i);
                org.w3c.dom.Node parent = nodeMap.getOrDefault(node2, document);
                String name = node2.getClass().getSimpleName().replaceAll("\\$", "_");
                String name2 = node2.getClass().getPackage().getName();
                if (!packages.contains(name2)) {
                    packages.add(name2);
                }
                Element createElement = document.createElement(name);
                parent.appendChild(createElement);
                Map<String, Object> fields = differences(node2);
                fields.forEach((s, fieldValue) -> {
                    if (fieldValue != null) {
                        processField(document, nodeMap, allNode, createElement, s, fieldValue, node2);
                    }
                });
                if (parent == document) {
                    createElement.setAttribute("xmlns:fx", "http://javafx.com/fxml");
                }
            }
            Node firstChild = document.getFirstChild();
            document.removeChild(firstChild);
            packages.forEach(p -> document.appendChild(document.createProcessingInstruction("import", p + ".*")));
            document.appendChild(firstChild);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
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
            List<Method> fields = ClassReflectionUtils.getGetterMethodsRecursive(cl);
            Map<String, Object> collect = fields.stream().filter(m -> ClassReflectionUtils.invoke(ob1, m) != null)
                .collect(Collectors.toMap(ClassReflectionUtils::getFieldNameCase,
                    m -> ClassReflectionUtils.invoke(ob1, m), (a, b) -> a == null ? b : a));
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

    public static void main(String argv[]) {
        List<Class<? extends Application>> asList = Arrays.asList(PaintMain.class);
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
            return cl.getConstructors()[0].newInstance(null);
        } catch (Exception e) {
            LOG.trace("", e);
            throw new RuntimeIOException("ERROR IN INSTANTIATION", e);
        }
    }

    private static boolean hasClass(List<Class<?>> newTagClasses, Class<? extends Object> class1) {
        return newTagClasses.stream().anyMatch(c -> c.isAssignableFrom(class1) || class1.isAssignableFrom(c));
    }

    private static void processField(Document document, Map<Object, org.w3c.dom.Node> nodeMap, List<Object> allNode,
        Element element, String fieldName, Object fieldValue, Object parent) {
        if (IGNORE.contains(fieldName) || allNode.stream().anyMatch(ob -> ob == fieldValue)) {
            return;
        }
        if (fieldValue instanceof javafx.scene.Node) {
            ObservableMap<Object, Object> properties = ((javafx.scene.Node) fieldValue).getProperties();
            properties.forEach((k, v) -> {

                String key = Objects.toString(k);
                String value = Objects.toString(v);

                element.setAttribute(key, value);
            });

        }

        if (fieldValue instanceof Collection) {
            Collection<?> list = (Collection<?>) fieldValue;
            if (list.isEmpty()) {
                return;
            }
            if (FORMAT_LIST.containsKey(fieldName)) {
                String apply = FORMAT_LIST.get(fieldName).apply(list);
                element.setAttribute(fieldName, apply);
            } else if (list.stream().anyMatch(o -> hasClass(NEW_TAG_CLASSES, o.getClass()))) {
                Element createElement2 = document.createElement(fieldName);
                element.appendChild(createElement2);
                for (Object object : list) {
                    if (!allNode.contains(object)) {
                        nodeMap.put(object, createElement2);
                        allNode.add(object);
                    }
                }
            } else {
                String classes = list.stream().findFirst().map(Object::getClass).map(Class::getName).orElse("");
                Class<? extends Object> parentClass = parent.getClass();
                LOG.info("attribute {} type {} of {} not set", fieldName, classes, parentClass);
                LOG.info("value {}", list);
            }
        } else if (hasClass(NEW_TAG_CLASSES, fieldValue.getClass())
            && (parent instanceof Collection || hasField(parent.getClass(), fieldName))
            || hasClass(CONDITIONAL_TAG_CLASSES, fieldValue.getClass()) && hasField(parent.getClass(), fieldName)) {
            Element createElement2 = document.createElement(fieldName);
            element.appendChild(createElement2);
            nodeMap.put(fieldValue, createElement2);
            allNode.add(fieldValue);
        } else if (ATTRIBUTE_CLASSES.contains(fieldValue.getClass()) && hasField(parent.getClass(), fieldName)) {
            Object mapProperty2 = mapProperty(fieldValue);
            element.setAttribute(fieldName, mapProperty2 + "");
        } else {
//            if(fieldValue.getProperties())

            if (!ATTRIBUTE_CLASSES.contains(fieldValue.getClass())) {
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
