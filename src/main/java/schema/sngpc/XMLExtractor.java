package schema.sngpc;

import static simplebuilder.SimpleTextBuilder.newBoldText;
import static utils.RunnableEx.remap;

import java.io.File;
import java.io.IOException;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.RunnableEx;

public final class XMLExtractor {

    private XMLExtractor() {
    }

    public static void addValue(Node item, TreeItem<Map<String, String>> e) {
        RunnableEx.run(() -> {
            NamedNodeMap attributes = item.getAttributes();
            for (int i = 0; attributes != null && i < attributes.getLength(); i++) {
                Node item2 = attributes.item(i);
                e.getValue().put(item2.getNodeName(), item2.getNodeValue());
            }
            if (!item.hasChildNodes()) {
                return;
            }
            NodeList childNodes = item.getChildNodes();
            List<Node> collect = IntStream.range(0, childNodes.getLength()).mapToObj(i -> childNodes.item(i))
                    .collect(Collectors.toList());

            for (Node item2 : collect) {
                if (item2.getNodeType() == Node.TEXT_NODE) {
                    String nodeValue = item2.getNodeValue();
                    String nodeName = item.getNodeName();
                    e.getValue().put(nodeName, nodeValue);
                }
            }
        });
    }

    public static Document newDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute("http://javax.xml.XMLConstants/feature/secure-processing", true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        return documentBuilder.newDocument();
    }

    public static Map.Entry<String, String> newEntry(String key, String value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static Map<String, String> newMap(String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            return new SimpleMap(key, value);
        }
        return new SimpleMap();
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

    private static void tryToRead(TreeView<Map<String, String>> build,
            Map<Node, TreeItem<Map<String, String>>> allItems, File file) throws XmlException, IOException {
        XmlObject parse = XmlObject.Factory.parse(file);
        Node rootNode = parse.getDomNode();
        List<Node> currentNodes = new ArrayList<>();
        currentNodes.add(rootNode);
        TreeItem<Map<String, String>> value = new TreeItem<>(newMap(rootNode.getNodeName(), rootNode.getNodeValue()));
        value.setGraphic(newBoldText(rootNode.getNodeName()));
        build.setRoot(value);
        allItems.put(rootNode, value);
        while (!currentNodes.isEmpty()) {
            Node domNode = currentNodes.remove(0);
            NodeList childNodes = domNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item.getNodeType() != Node.TEXT_NODE) {
                    currentNodes.add(0, item);
                    TreeItem<Map<String, String>> e = new TreeItem<>(newMap(item.getNodeName(), item.getNodeValue()));
                    allItems.get(domNode).getChildren().add(e);
                    allItems.put(item, e);
                    e.setGraphic(newBoldText(item.getNodeName()));
                    addValue(item, e);
                }
            }
        }
    }
}
