package schema.sngpc;

import static utils.RunnableEx.remap;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.HasLogging;

public final class XMLExtractor {
    private static final Logger LOG = HasLogging.log();

    private XMLExtractor() {
    }

    public static void addValue(Node item, TreeItem<Map<String, String>> e) {
        try {
            NamedNodeMap attributes = item.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node item2 = attributes.item(i);
                if (e.getValue() == null) {
                    e.setValue(newMap(item.getNodeName(), item.getNodeValue()));
                }
                e.getValue().put(item2.getNodeName(), item2.getNodeValue());
            }
            String nodeValue = item.getFirstChild().getNodeValue();
            if (StringUtils.isNotBlank(nodeValue)) {
                e.setValue(newMap(item.getNodeName(), nodeValue));
            }

        } catch (Exception e2) {
            LOG.trace("", e2);
        }
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
        return new SimpleMap(key, value);
    }

    public static Text newText(Node item) {
        Text text = new Text(item.getNodeName());
        Font font = Font.getDefault();
        text.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()));
        return text;
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
        Node domNode = parse.getDomNode();
        List<Node> currentNodes = new ArrayList<>();
        currentNodes.add(domNode);
        TreeItem<Map<String, String>> value = new TreeItem<>(newMap(domNode.getNodeName(), domNode.getNodeValue()));
        value.setGraphic(newText(domNode));
        build.setRoot(value);
        allItems.put(domNode, value);
        while (!currentNodes.isEmpty()) {
            domNode = currentNodes.remove(0);
            NodeList childNodes = domNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item.getNodeType() != Node.TEXT_NODE) {
                    currentNodes.add(0, item);
                    TreeItem<Map<String, String>> e = new TreeItem<>(
                        newMap(item.getNodeName(), item.getNodeValue()));
                    allItems.get(domNode).getChildren().add(e);
                    allItems.put(item, e);
                    e.setGraphic(newText(item));
                    addValue(item, e);
                }
            }
        }
    }
}
