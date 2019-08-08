package schema.sngpc;

import static utils.ClassReflectionUtils.mapProperty;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import rosario.RosarioComparadorArquivos;
import utils.ClassReflectionUtils;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public final class FXMLCreator {
    private static final Logger LOG = HasLogging.log();
    private static final List<String> IGNORE = Arrays.asList("baselineOffset", "localToParentTransform",
        "localToSceneTransform", "parentPopup", "boundsInParent", "boundsInLocal", "scene", "childrenUnmodifiable",
        "styleableParent", "parent", "labelPadding");
    private static final List<Class<?>> ATTRIBUTE_CLASSES = Arrays.asList(Double.class, String.class, Color.class,
        Integer.class, Boolean.class);
    private static final List<Class<?>> NEW_TAG_CLASSES = Arrays.asList(EventTarget.class);

    private FXMLCreator() {
    }

    public static void createXMLFile(Parent node, File file) {
        try {
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
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(file);
            transformer.transform(domSource, streamResult);
            String data = FileUtils.readFileToString(file).replaceAll("><", ">\n<");
            FileUtils.writeStringToFile(file, data);
        } catch (Exception e) {
            LOG.error("", e);
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
                    LOG.info("{} {}!={} ", fieldName, fieldValue, fieldValue2);
                    diffFields.put(fieldName, fieldValue);
                }
            }
        } catch (Exception e) {
            LOG.error("", e);
        }
        return diffFields;
    }

    public static void duplicateStage(File file) {
        duplicateStage(file, file.getName());
    }

    public static void duplicateStage(File file, String title) {
        try {
            Stage primaryStage = new Stage();
            Parent content = FXMLLoader.load(file.toURI().toURL());
            Scene scene = new Scene(content);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public static void main(String argv[]) {
        List<Class<? extends Application>> asList = Arrays.asList(RosarioComparadorArquivos.class);
        testApplications(asList);

//        duplicate("TableVisualizationExampleApp.fxml");

    }

    @SafeVarargs
    public static void testApplications(Class<? extends Application>... asList) {
        testApplications(Arrays.asList(asList));
    }

    public static void testApplications(List<Class<? extends Application>> asList) {
        ResourceFXUtils.initializeFX();
        for (Class<? extends Application> class1 : asList) {
            Platform.runLater(RunnableEx.make(() -> {
                Application a = class1.newInstance();
                Stage primaryStage = new Stage();
                primaryStage.setTitle(class1.getSimpleName());
                a.start(primaryStage);
//                primaryStage.close();
                File outFile = ResourceFXUtils.getOutFile(class1.getSimpleName() + ".fxml");
                createXMLFile(primaryStage.getScene().getRoot(), outFile);
                duplicateStage(outFile, primaryStage.getTitle());
                LOG.info("{} successfull", class1.getSimpleName());
            }));

        }
    }

    public static void xmlExample() throws TransformerFactoryConfigurationError {
        try {

            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

            Document document = documentBuilder.newDocument();

            // root element
            Element root = document.createElement("company");
            document.appendChild(root);

            // employee element
            Element employee = document.createElement("employee");

            root.appendChild(employee);

            // set an attribute to staff element
            Attr attr = document.createAttribute("id");
            attr.setValue("10");
            employee.setAttributeNode(attr);

            // you can also use staff.setAttribute("id", "1") for this

            // firstname element
            Element firstName = document.createElement("firstname");
            firstName.appendChild(document.createTextNode("James"));
            employee.appendChild(firstName);

            // lastname element
            Element lastname = document.createElement("lastname");
            lastname.appendChild(document.createTextNode("Harley"));
            employee.appendChild(lastname);

            // email element
            Element email = document.createElement("email");
            email.appendChild(document.createTextNode("james@example.org"));
            employee.appendChild(email);

            // department elements
            Element department = document.createElement("department");
            department.appendChild(document.createTextNode("Human Resources"));
            employee.appendChild(department);

            // create the xml file
            // transform the DOM Object to an XML File
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(System.out);

            // If you use
            // StreamResult result = new StreamResult(System.out);
            // the output will be pushed to the standard output ...
            // You can use that for debugging

            transformer.transform(domSource, streamResult);

            System.out.println("Done creating XML File");

        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    static void duplicate(String out) {
        if (Platform.isFxApplicationThread()) {
            duplicateStage(ResourceFXUtils.getOutFile(out));
        } else {
            ResourceFXUtils.initializeFX();
            Platform.runLater(() -> duplicateStage(ResourceFXUtils.getOutFile(out)));
        }
    }

    private static Object getInstance(Class<?> cl) throws Exception {
        try {
            return cl.newInstance();
        } catch (Exception e) {
            return cl.getConstructors()[0].newInstance(100);
        }
    }

    private static boolean hasClass(Class<? extends Object> class1) {
        return NEW_TAG_CLASSES.stream().anyMatch(c -> c.isAssignableFrom(class1) || class1.isAssignableFrom(c));
    }

    private static void processField(Document document, Map<Object, org.w3c.dom.Node> nodeMap, List<Object> allNode,
        Element createElement, String s, Object fieldValue, Object node2) {
        if (IGNORE.contains(s) || allNode.contains(fieldValue)) {
            return;
        }
        if (hasClass(fieldValue.getClass())) {
            Element createElement2 = document.createElement(s);
            createElement.appendChild(createElement2);
            nodeMap.put(fieldValue, createElement2);
            allNode.add(fieldValue);
        } else if (fieldValue instanceof Collection) {
            Collection<?> list = (Collection<?>) fieldValue;
            if (list.stream().anyMatch(o -> hasClass(o.getClass()))) {
                Element createElement2 = document.createElement(s);
                createElement.appendChild(createElement2);
                for (Object object : list) {
                    if (!allNode.contains(object)) {
                        nodeMap.put(object, createElement2);
                        allNode.add(object);
                    }
                }
            }
        } else {

            if (ATTRIBUTE_CLASSES.contains(fieldValue.getClass())) {
                if (ClassReflectionUtils.hasSetterMethods(node2.getClass(), s)) {
                    Object mapProperty2 = mapProperty(fieldValue);
                    createElement.setAttribute(s, mapProperty2 + "");
                } else {
                    LOG.info("{} is not an attribute of {}", s, node2.getClass());
                }
            } else {
                LOG.info("attribute {}-{} not in ", s, fieldValue.getClass());
            }

        }
    }
}
