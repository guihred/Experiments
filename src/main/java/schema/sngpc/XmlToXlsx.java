package schema.sngpc;

import extract.DocumentHelper;
import extract.ExcelService;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.*;

public final class XmlToXlsx {
    private static final Logger LOG = HasLogging.log();

    private XmlToXlsx() {
    }

    public static void convertXML2XLS(File file) {
        Document parse = DocumentHelper.getDoc(file);
        List<List<String>> arrayList = new ArrayList<>();
        foreach(parse.getElementsByTagName("Row"), xmlObject -> {
            List<String> arrayList2 = new ArrayList<>();
            NodeList childNodes = xmlObject.getChildNodes();
            foreach(childNodes, (cell, i) -> foreach(cell.getChildNodes(), data -> {
                if ("Data".equals(data.getNodeName())) {
                    String textContent = data.getTextContent();
                    arrayList2.add(textContent.trim());
                }
            }));
            arrayList.add(arrayList2);
        });

        arrayList.forEach(l -> LOG.info("{}", l));

        File outFile = ResourceFXUtils.getOutFile("xlsx/" + file.getName().replaceAll("\\.\\w+$", ".xlsx"));

        Map<String, FunctionEx<List<String>, Object>> mapa = new LinkedHashMap<>();
        for (int i = 0; i < arrayList.get(0).size(); i++) {
            int j = i;
            mapa.put("" + i, l -> l.get(j));
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

    private static void logError(String fieldName, Throwable e) {
        LOG.error("ERROR IN FIELD {} {}", fieldName, e.getMessage());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void setField(Object ob, Map<String, Method> collect, Map<String, Object> attributeMap,
            String fieldName) {
        Method method = collect.get(fieldName);
        Class setterType = method.getParameters()[0].getType();
        String upperCase = attributeMap.getOrDefault(fieldName, "").toString().toUpperCase();
        if (setterType.isEnum()) {
            RunnableEx.make(() -> {
                Enum<?> valueOf = Enum.valueOf(setterType, upperCase);
                method.invoke(ob, valueOf);
            }, e -> logError(fieldName, e)).run();
            return;
        }
        if (setterType.isPrimitive()) {
            RunnableEx.make(() -> {
                Method method2 = Class.forName("java.lang." + StringSigaUtils.changeCase(setterType.getName()))
                        .getMethod("valueOf", String.class);
                method.invoke(ob, method2.invoke(null, upperCase));
            }, e -> logError(fieldName, e)).run();
            return;
        }
        LOG.info("{}.{} NOT SET TO {} AS {}", ob.getClass().getSimpleName(), fieldName, upperCase, setterType);

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void setField(Object ob, Map<String, Method> collect, Node item, String nodeName,
            Map<String, Object> attributeMap, Optional<String> findFirst, Map<Class<?>, SupplierEx<?>> supplierMap) {
        Method method = findFirst.map(e -> collect.get(e + nodeName)).orElse(collect.get(nodeName));
        Class setterType = method.getParameters()[0].getType();
        if (setterType.isEnum()) {
            RunnableEx.make(() -> {
                String orElse = findFirst.orElse(nodeName);
                String upperCase = attributeMap.getOrDefault(orElse, "").toString().toUpperCase();
                Enum<?> valueOf = Enum.valueOf(setterType, upperCase);
                method.invoke(ob, valueOf);
            }, e -> logError(nodeName, e)).run();
            return;
        }
        if (supplierMap.containsKey(setterType)) {
            RunnableEx.make(() -> {
                Object object = SupplierEx.remap(supplierMap.get(setterType), "ERROR " + setterType);
                childMap(item, object, supplierMap);
                method.invoke(ob, object);
            }, e -> logError(nodeName, e)).run();
            return;
        }
        LOG.info("CONVERT {}-> {}", item, setterType);
    }

}
