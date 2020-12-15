package fxml.utils;

import extract.DocumentHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.ExcelService;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;

public final class XmlToXlsx {
    private static final Logger LOG = HasLogging.log();

    private XmlToXlsx() {
    }

    public static void convertXML2XLS(File file) {
        Document parse = DocumentHelper.getDoc(file);
        List<List<String>> lines = new ArrayList<>();
        foreach(parse.getElementsByTagName("Row"), xmlObject -> {
            List<String> arrayList2 = new ArrayList<>();
            NodeList childNodes = xmlObject.getChildNodes();
            foreach(childNodes, (cell, i) -> foreach(cell.getChildNodes(), data -> {
                if ("Data".equals(data.getNodeName())) {
                    String textContent = data.getTextContent();
                    arrayList2.add(textContent.trim());
                }
            }));
            lines.add(arrayList2);
        });

        lines.forEach(l -> LOG.info("{}", l));

        File outFile = ResourceFXUtils.getOutFile("xlsx/" + file.getName().replaceAll("\\.\\w+$", ".xlsx"));

        Map<String, FunctionEx<List<String>, Object>> mapa = new LinkedHashMap<>();
        for (int i = 0; i < lines.get(0).size(); i++) {
            int j = i;
            mapa.put("" + i, l -> l.get(j));
        }

        ExcelService.getExcel(lines, mapa, outFile);
    }

    private static void foreach(NodeList childNodes, Consumer<Node> cons) {
        for (int j = 0; j < childNodes.getLength(); j++) {
            cons.accept(childNodes.item(j));
        }
    }

    private static void foreach(NodeList childNodes, ObjIntConsumer<Node> cons) {
        for (int j = 0; j < childNodes.getLength(); j++) {
            cons.accept(childNodes.item(j), j);
        }
    }
}
