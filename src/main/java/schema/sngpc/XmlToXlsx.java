package schema.sngpc;

import extract.ExcelService;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.*;

public class XmlToXlsx {
    private static final Logger LOG = HasLogging.log();

    public static void convertXML2XLS(File file) {
        Document parse = SupplierEx.remap(() -> DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file),
                "ERROR PARSING");
        // NodeList rootNode = parse.getElementsByTagName("Style");
        // Map<Class<?>, SupplierEx<?>> supplierMap = new HashMap<>();
        // supplierMap.put(CellStyle.class, xssfWorkbook::createCellStyle);
        // supplierMap.put(Font.class, xssfWorkbook::createFont);
        // Map<String, CellStyle> styleMap = new HashMap<>();
        // foreach(rootNode, xmlObject -> {
        // CellStyle style = xssfWorkbook.createCellStyle();
        // Map<String, String> childMap = childMap(xmlObject, style, supplierMap);
        // styleMap.put(childMap.get("ID"), style);
        // });
        List<List<String>> arrayList = new ArrayList<>();

        foreach(parse.getElementsByTagName("Row"), (xmlObject) -> {
            List<String> arrayList2 = new ArrayList<>();
            NodeList childNodes = xmlObject.getChildNodes();
            foreach(childNodes, (cell, i) -> {
                foreach(cell.getChildNodes(), (Node data) -> {
                    if ("Data".equals(data.getNodeName())) {
                        String textContent = data.getTextContent();
                        arrayList2.add(i, textContent.trim());
                    }
                });
            });
            arrayList.add(arrayList2);
        });

        arrayList.forEach(l -> System.out.println(l));

        File outFile = ResourceFXUtils.getOutFile("xlsx/" + file.getName().replaceAll("xls", "xlsx"));

        Map<String, FunctionEx<List<String>, Object>> mapa = new LinkedHashMap<>();
        for (int i = 0; i < arrayList.get(0).size(); i++) {
            int j = i;
            mapa.put("" + i, (l) -> l.get(j));
        }

        ExcelService.getExcel(arrayList, mapa, outFile);
    }


    private static Map<String, Object> attributeMap(Node xmlObject) {
        NamedNodeMap attributes = xmlObject.getAttributes();
        Map<String, Object> hashMap = new HashMap<>();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node item = attributes.item(i);
                String nodeName = item.getNodeName().replaceAll("^\\w+:", "");
                String textContent = item.getTextContent();
                hashMap.put(nodeName, textContent);
            }
        }
        return hashMap;
    }

    private static Map<String, String> childMap(Node xmlObject, Object ob, Map<Class<?>, SupplierEx<?>> supplierMap) {
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
            }
        });
        Map<String, Object> attributeMap = attributeMap(xmlObject);
        Map<String, String> hashMap = new HashMap<>();
        attributeMap.forEach((k, v) -> {
            if (collect.containsKey(k)) {
                setField(ob, collect, attributeMap, k);
            } else {
                hashMap.put(k, Objects.toString(v));
                LOG.info("{}.{} NOT SET TO {}", ob.getClass().getSimpleName(), k, v);
            }
        });
        return hashMap;
    }

    private static void foreach(NodeList childNodes, BiConsumer<Node, Integer> cons) {
        for (int j = 0; j < childNodes.getLength(); j++) {
            cons.accept(childNodes.item(j), j);
        }
    }

    private static void foreach(NodeList childNodes, Consumer<Node> cons) {
        for (int j = 0; j < childNodes.getLength(); j++) {
            cons.accept(childNodes.item(j));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void setField(Object ob, Map<String, Method> collect, Map<String, Object> attributeMap,
            String fieldName) {
        Method method = collect.get(fieldName);
        Class<?> setterType = method.getParameters()[0].getType();

        String upperCase = attributeMap.getOrDefault(fieldName, "").toString().toUpperCase();
        if (setterType.isEnum()) {
            RunnableEx.make(() -> {
                Class setterType2 = setterType;
                Enum<?> valueOf = Enum.valueOf(setterType2, upperCase);
                method.invoke(ob, valueOf);
            }, e -> LOG.error("ERROR IN FIELD {} {}", fieldName, e.getMessage())).run();
        } else if (setterType.isPrimitive()) {
            RunnableEx.make(() -> {
                Method method2 = Class.forName("java.lang." + StringSigaUtils.changeCase(setterType.getName()))
                        .getMethod("valueOf", String.class);
                method.invoke(ob, method2.invoke(null, upperCase));
            }, e -> LOG.error("ERROR IN FIELD {} {}", fieldName, e.getMessage())).run();

        } else {
            LOG.info("{}.{} NOT SET TO {} AS {}", ob.getClass().getSimpleName(), fieldName, upperCase, setterType);
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
            }, e -> LOG.error("ERROR IN FIELD {} {}", nodeName, e.getMessage())).run();
        } else if (supplierMap.containsKey(setterType)) {
            RunnableEx.make(() -> {
                Object object = SupplierEx.remap(supplierMap.get(setterType), "ERROR " + setterType);
                childMap(item, object, supplierMap);

                method.invoke(ob, object);
            }, e -> LOG.error("ERROR IN FIELD {} {}", nodeName, e.getMessage())).run();
        } else {
            LOG.info("CONVERT {}-> {}", item, setterType);
        }
    }

}
