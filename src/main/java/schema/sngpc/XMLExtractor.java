package schema.sngpc;

import static simplebuilder.SimpleTextBuilder.newBoldText;
import static utils.RunnableEx.remap;
import static utils.RunnableEx.run;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.SupplierEx;

public final class XMLExtractor {

    private XMLExtractor() {
    }

    public static void addValue(Node item, TreeItem<Map<String, String>> e) {
            NamedNodeMap attributes = item.getAttributes();
            for (int i = 0; attributes != null && i < attributes.getLength(); i++) {
                Node item2 = attributes.item(i);
                e.getValue().put(item2.getNodeName(), item2.getNodeValue());
            }
            if (!item.hasChildNodes()) {
                return;
            }
            NodeList childNodes = item.getChildNodes();
            List<Node> collect =
                    IntStream.range(0, childNodes.getLength()).mapToObj(childNodes::item).collect(Collectors.toList());
            if (collect.stream().allMatch(n -> n.getNodeType() == Node.TEXT_NODE)) {
                if (collect.size() == 1) {
                    String nodeValue = collect.get(0).getNodeValue();
                    String nodeName = item.getNodeName();
                    e.setValue(newMap(nodeName, nodeValue));
                    return;
                }
                for (Node item2 : collect) {
                    if (item2.getNodeType() == Node.TEXT_NODE) {
                        String nodeValue = item2.getNodeValue();
                        String nodeName = item.getNodeName();
                        e.getValue().put(nodeName, nodeValue);
                    }
                }
            }
    }

    public static Document newDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        // factory.setAttribute("http://javax.xml.XMLConstants/feature/secure-processing",
        // true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        return documentBuilder.newDocument();
    }

    public static Map.Entry<String, String> newEntry(String key, String value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static Map<String, String> newMap(String key, String value) {
        return new SimpleMap(key, value);
    }

    public static void readXMLFile(TreeView<Map<String, String>> build,
            Map<Node, TreeItem<Map<String, String>>> allItems, File file) {
        remap(() -> tryToRead(build, allItems, file), "ERROR READING");
    }

    public static void saveToFile(Document document, File file) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
        transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
    }

    private static Map<String, String> newMap(Node item) {
        if (item.getNodeType() == Node.ELEMENT_NODE) {
            return new SimpleMap();
        }

        return newMap(item.getNodeName(), item.getNodeValue());
    }

    private static boolean notTextNorComment(Node item) {
        return item.getNodeType() != Node.COMMENT_NODE && item.getNodeType() != Node.TEXT_NODE;
    }

    private static void tryToRead(TreeView<Map<String, String>> build,
            Map<Node, TreeItem<Map<String, String>>> allItems, File file) {
        XmlObject parse = SupplierEx.remap(() -> XmlObject.Factory.parse(file), "ERROR PARSING");

        Node rootNode = parse.getDomNode();
        List<Node> currentNodes = new ArrayList<>();
        currentNodes.add(rootNode);
        TreeItem<Map<String, String>> value = new TreeItem<>(newMap(rootNode));
        value.setGraphic(newBoldText(rootNode.getNodeName()));
        build.setRoot(value);
        build.setShowRoot(false);
        allItems.put(rootNode, value);
        while (!currentNodes.isEmpty()) {
            Node domNode = currentNodes.remove(0);
            NodeList childNodes = domNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (notTextNorComment(item)) {
                    currentNodes.add(0, item);
                    TreeItem<Map<String, String>> e = new TreeItem<>(newMap(item));
                    allItems.get(domNode).getChildren().add(e);
                    allItems.put(item, e);
                    e.setGraphic(newBoldText(item.getNodeName()));
                    run(() -> addValue(item, e));
                }
            }
        }
    }
}
