package schema.sngpc;

import static utils.ClassReflectionUtils.mapProperty;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import utils.ClassReflectionUtils;
import utils.HasLogging;
import utils.ResourceFXUtils;

public final class FXMLCreator {
    private static final Logger LOG = HasLogging.log();

    private FXMLCreator() {
    }

    public static void createXMLFile(Parent node, File file) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Map<Node, org.w3c.dom.Node> nodeMap = new HashMap<>();
            List<Node> allNode = new ArrayList<>();
            allNode.add(node);
            for (int i = 0; i < allNode.size(); i++) {
                Node node2 = allNode.get(i);
                org.w3c.dom.Node parent = nodeMap.getOrDefault(node2,
                    nodeMap.getOrDefault(node2.getParent(), document));
                String name = node2.getClass().getName();
                if (name.contains("$")) {
                    continue;
                }

                Element createElement = document.createElement(name);
                parent.appendChild(createElement);
                Map<String, Object> fields = differences(node2, node2.getClass());
                fields.forEach((s, fieldValue) -> {
                    if (fieldValue != null) {
                        if (fieldValue instanceof Node) {
                            Node fieldValue2 = (Node) fieldValue;
                            Element createElement2 = document.createElement(s);
                            createElement.appendChild(createElement2);
                            nodeMap.put(fieldValue2, createElement2);
                            allNode.add(fieldValue2);
                        } else if (fieldValue instanceof List) {
                            List<?> list = (List<?>) fieldValue;
                            if (!list.isEmpty()) {
                                for (Object object : list) {
                                    if (object instanceof Node) {
                                        Element createElement2 = document.createElement(s);
                                        createElement.appendChild(createElement2);
                                        Node object2 = (Node) object;
                                        nodeMap.put(object2, createElement2);
                                        allNode.add(object2);
                                    }
                                }
                            }
                        } else {
                            createElement.setAttribute(s, fieldValue + "");

                        }
                    }

                });

                if (parent == document) {
                    createElement.setAttribute("xmlns:fx", "http://javafx.com/fxml");
                }

                if (node2 instanceof Parent) {
                    List<javafx.scene.Node> childrenUnmodifiable = ((Parent) node2).getChildrenUnmodifiable().stream()
                        .filter(e -> !nodeMap.containsKey(e)).collect(Collectors.toList());
                    if (!childrenUnmodifiable.isEmpty()) {
                        allNode.addAll(childrenUnmodifiable);
                        Element createElement2 = document.createElement("children");
                        createElement.appendChild(createElement2);
                        nodeMap.put(node2, createElement2);
                    }
                }
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(file);
            transformer.transform(domSource, streamResult);

        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public static Map<String, Object> differences(Object ob1, Class<?> cl) {
        Map<String, Object> diffFields = new HashMap<>();

        try {
            List<Method> getterMethods = ClassReflectionUtils.getGetterMethods(cl);

            List<Method> fields = getterMethods;
            Object ob2 = cl.newInstance();
            for (Method f : fields) {

                Object fieldValue = mapProperty(ClassReflectionUtils.invoke(ob1, f));
                Object fieldValue2 = mapProperty(ClassReflectionUtils.invoke(ob2, f));
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
        try {
            Stage primaryStage = new Stage();
            Parent content = FXMLLoader.load(file.toURI().toURL());
            Scene scene = new Scene(content);
            primaryStage.setTitle("Look N Feel Chooser");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public static void main(String argv[]) {
        ResourceFXUtils.initializeFX();
        Platform.runLater(() -> duplicateStage(ResourceFXUtils.getOutFile("first.fxml")));

        //
//        xmlExample();
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

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }
}
