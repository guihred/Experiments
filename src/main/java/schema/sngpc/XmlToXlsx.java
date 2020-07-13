package schema.sngpc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.*;

public class XmlToXlsx {
    private static final Logger LOG = HasLogging.log();

    public static void convertXML2XLS(File file) throws IOException {
        try (Workbook xssfWorkbook = new SXSSFWorkbook(100)) {
            Document parse = SupplierEx.remap(
                    () -> DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file), "ERROR PARSING");
            NodeList rootNode = parse.getElementsByTagName("Style");
            Map<Class<?>, SupplierEx<?>> supplierMap = new HashMap<>();
            supplierMap.put(CellStyle.class, xssfWorkbook::createCellStyle);
            supplierMap.put(Font.class, xssfWorkbook::createFont);

            for (int i = 0; i < rootNode.getLength(); i++) {
                Node xmlObject = rootNode.item(i);

                CellStyle style = xssfWorkbook.createCellStyle();
                Map<String, Object> fieldMap = childMap(xmlObject, style, supplierMap);
                LOG.info("{}", fieldMap);
                // attributes.item(index)
            }

            File outFile = ResourceFXUtils.getOutFile("xlsx/" + file.getName().replaceAll("xls", "xlsx"));
            xssfWorkbook.write(new FileOutputStream(outFile));
        }
    }

    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\guigu\\Documents\\Dev\\Dataprev\\Downs\\export.xls");
        convertXML2XLS(file);
    }

    private static Map<String, Object> attributeMap(Node xmlObject) {
        NamedNodeMap attributes = xmlObject.getAttributes();
        Map<String, Object> hashMap = new HashMap<>();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node item = attributes.item(i);
                String nodeName = item.getNodeName().replaceAll("ss:", "");
                String textContent = item.getTextContent();
                hashMap.put(nodeName, textContent);
            }
        }
        return hashMap;
    }

    private static Map<String, Object> childMap(Node xmlObject, CellStyle ob,
            Map<Class<?>, SupplierEx<?>> supplierMap) {
        List<Method> setters = ClassReflectionUtils.setters(ob.getClass());
        Map<String, Method> collect = setters.stream().collect(Collectors.toMap(ClassReflectionUtils::getFieldName,
                e -> e, (u, v) -> u.getParameters()[0].getType().isPrimitive() ? v : u));
        foreach(xmlObject.getChildNodes(), item -> {
            String nodeName = item.getNodeName();
            Map<String, Object> attributeMap = attributeMap(item);
            Optional<String> findFirst =
                    attributeMap.keySet().stream().filter(e -> collect.containsKey(e + nodeName)).findFirst();
            if (collect.containsKey(nodeName) || findFirst.isPresent()) {
                setField(ob, collect, item, nodeName, attributeMap, findFirst, supplierMap);
            } else {
                foreach(item.getChildNodes(), it -> {
                    Map<String, Object> attributes = attributeMap(it);
                    attributes.forEach((k, v) -> {
                        if (collect.containsKey(it.getNodeName() + v)) {

                        }
                    });

                });
            }
        });
        Map<String, Object> hashMap = new HashMap<>();
        return hashMap;
    }

    private static void foreach(NodeList childNodes, Consumer<Node> cons) {
        for (int j = 0; j < childNodes.getLength(); j++) {
            cons.accept(childNodes.item(j));
        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void setField(Object ob, Map<String, Method> collect, Node item, String nodeName,
            Map<String, Object> attributeMap, Optional<String> findFirst, Map<Class<?>, SupplierEx<?>> supplierMap) {
        Method method = findFirst.map(e -> collect.get(e + nodeName)).orElse(collect.get(nodeName));
        Class<?> setterType = method.getParameters()[0].getType();
        if (setterType.isEnum()) {
            RunnableEx.make(() -> {
                String orElse = findFirst.orElse(nodeName);
                String upperCase = attributeMap.getOrDefault(orElse, "").toString().toUpperCase();
                Class setterType2 = setterType;
                Enum<?> valueOf = Enum.valueOf(setterType2, upperCase);
                method.invoke(ob, valueOf);
                LOG.info("SETTING FIELD {} {}", orElse, upperCase);
            }, e -> LOG.info("ERROR IN FIELD {} {}", nodeName, e.getMessage())).run();
        } else if (supplierMap.containsKey(setterType)) {
            RunnableEx.make(() -> {
                Object object = SupplierEx.remap(supplierMap.get(setterType), "ERROR " + setterType);

                method.invoke(ob, object);
            }, e -> LOG.info("ERROR IN FIELD {} {}", nodeName, e.getMessage())).run();
        } else {
            LOG.info("CONVERT {}-> {}", item, setterType);
        }
    }

}
