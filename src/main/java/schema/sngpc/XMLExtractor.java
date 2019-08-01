package schema.sngpc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.assertj.core.api.exception.RuntimeIOException;
import org.slf4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.HasLogging;

public class XMLExtractor {
    private static final Logger LOG = HasLogging.log();

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

    public static Map<String, String> newMap(String key, String value) {
        Map<String, String> hashMap = new HashMap<String, String>() {
            @Override
            public String toString() {
                return value;
            }
        };
        hashMap.put(key, value);
        return hashMap;
    }

    public static Text newText(Node item) {
        Text text = new Text(item.getNodeName());
        Font font = Font.getDefault();
        text.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()));
        return text;
    }

    public static void readXMLFile(TreeView<Map<String, String>> build,
        Map<Node, TreeItem<Map<String, String>>> allItems, File file) {
        try {
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

        } catch (Exception e) {
            throw new RuntimeIOException("ERROR READING", e);
        }
    }

}
