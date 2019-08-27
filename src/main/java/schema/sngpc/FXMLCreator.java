package schema.sngpc;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static schema.sngpc.FXMLCreatorHelper.*;
import static utils.ClassReflectionUtils.*;
import static utils.StringSigaUtils.changeCase;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.image.Image;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.exception.RuntimeIOException;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import others.TreeElement;
import utils.ClassReflectionUtils;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public final class FXMLCreator {
    private static final String FX_REFERENCE = "fx:reference";
    private static final String FX_FACTORY = "fx:factory";
    private static final String FX_ID = "fx:id";
    private static final String FX_VALUE = "fx:value";
    private static final String FX_DEFINE = "fx:define";
    private static final Logger LOG = HasLogging.log();

    private Collection<Class<?>> referenceClasses = new HashSet<>(REFERENCE_CLASSES);
    private Document document;
    private Map<Object, org.w3c.dom.Node> nodeMap = new IdentityHashMap<>();
    private Map<Object, org.w3c.dom.Node> originalMap = new IdentityHashMap<>();
    private List<Object> allNode = new ArrayList<>();
    private Set<String> packages = new LinkedHashSet<>();
    private Map<String, String> referencedMethod = new LinkedHashMap<>();
    private Map<Object, String> referencedNodes = new IdentityHashMap<>();
    private LinkedHashMap<Class<?>, List<String>> differencesMap = new LinkedHashMap<>();

    public void createFXMLFile(Object node, File file) {
        String packageName = FXMLCreator.class.getPackage().getName();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute("http://javax.xml.XMLConstants/feature/secure-processing", true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            document = documentBuilder.newDocument();
            allNode.add(node);
            processNodes(file, packageName);

            Node firstChild = document.getFirstChild();
            document.removeChild(firstChild);
            packages.forEach(p -> document.appendChild(document.createProcessingInstruction("import", p + ".*")));
            document.appendChild(firstChild);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
            transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
            transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(file);
            transformer.transform(domSource, streamResult);

            createController(file, packageName);

        } catch (Exception e) {
            throw new RuntimeIOException("ERROR in file " + file.getName(), e);
        }
    }

    private String computeMethod(Object parent, String fieldName, Object fieldValue, String nodeId) {
        String signature = "()";
        Class<?> class1 = fieldValue.getClass().getInterfaces()[0];
        TypeVariable<?>[] typeParameters = class1.getTypeParameters();
        if (typeParameters.length > 0) {
            TypeVariable<?> typeVariable = typeParameters[0];
            Type[] bounds = typeVariable.getBounds();
            for (Type type : bounds) {
                String typeName = type.getTypeName();
                String[] split2 = typeName.split("\\.");
                String packageName = Stream.of(split2).limit(split2.length - 1L).collect(joining("."));
                packages.add(packageName);
                signature = "(" + split2[split2.length - 1] + " e)";
            }
        }
        Method setter = ClassReflectionUtils.getSetter(parent.getClass(), fieldName);
        Parameter[] parameterTypes = setter.getParameters();
        for (Parameter class2 : parameterTypes) {
            String[] methodSignature = class2.toString().split("[^\\w\\.]");
            String eventType = methodSignature[methodSignature.length - 3];
            String[] split2 = eventType.split("\\.");
            String packageName = Stream.of(split2).limit(split2.length - 1L).collect(joining("."));
            if (!packageName.isEmpty()) {
                packages.add(packageName);
                signature = "(" + split2[split2.length - 1] + " e)";
            } else {
                LOG.info("Field not set parent={} field={} value={} id={}", parent, fieldName, fieldValue, nodeId);
            }
        }
        String nodeId2 = Character.isLowerCase(nodeId.charAt(0)) ? changeCase(nodeId) : nodeId;
        return fieldName + nodeId2 + signature;
    }

    private void createController(File file, String packageName) throws IOException {
        String name = file.getName().replaceAll("\\.fxml", "") + "Controller";
        File outFile = ResourceFXUtils.getOutFile(name + ".java");
        List<String> lines = new ArrayList<>();
        lines.add("package " + packageName + ";");
        lines.add("import javafx.fxml.*;");
        lines.addAll(packages.stream().map(e -> "import " + e + ".*;").collect(toList()));
        lines.add("public class " + name + "{");
        lines.addAll(referencedNodes.entrySet().stream()
            .map(e -> String.format("\t@FXML%n\t%s %s;", e.getKey().getClass().getSimpleName(), e.getValue()))
            .collect(toList()));
        lines.addAll(
            referencedMethod.values().stream().map(e -> String.format("\tpublic void %s{%n\t}", e)).collect(toList()));
        lines.add("}");
        Files.write(outFile.toPath(), lines, StandardCharsets.UTF_8);
        List<String> compileClass = ControllerCompiler.compileClass(outFile);
        if (!compileClass.contains("Classe Adicionada com sucesso")) {
            LOG.info("{}", compileClass);
        }
    }

    private Element createListElement(Element element, String fieldName, Object parent, Collection<?> list) {
        Element originalElement = document.createElement(fieldName);
        element.appendChild(originalElement);
        if (hasField(parent.getClass(), fieldName) && isSetterMatches(fieldName, list, parent)) {
            Element collectionsElement = document.createElement("FXCollections");
            packages.add("javafx.collections");
            originalElement.appendChild(collectionsElement);
            collectionsElement.setAttribute(FX_FACTORY, "observableArrayList");
            return collectionsElement;
        }
        return originalElement;
    }

    @SuppressWarnings("deprecation")
    private Map<String, Object> differences(Object ob1) {
        Map<String, Object> diffFields = new LinkedHashMap<>();
        Class<?> cl = ob1.getClass();
        if (ob1 instanceof Image) {
            diffFields.put("url", ((Image) ob1).impl_getUrl());
        }
        try {
            Object ob2 = getInstance(cl);
            addDifferences(cl, diffFields, ob1, ob2);
        } catch (Exception e) {
            LOG.trace("", e);
            List<String> fields = getNamedArgs(cl);
            List<String> mappedDifferences = differencesMap.computeIfAbsent(cl,
                c -> allNode.stream().filter(ob -> ob.getClass() == cl && ob != ob1).limit(100)
                    .flatMap(ob2 -> TreeElement.getDifferences(c, ob1, ob2).stream()).distinct()
                    .collect(Collectors.toList()));
            fields.addAll(mappedDifferences);
            Map<String, Object> collect = fields.stream().distinct().filter(m -> invoke(ob1, m) != null)
                .collect(toMap(m -> m, m -> invoke(ob1, m), (a, b) -> a != null ? a : b, LinkedHashMap::new));
            diffFields.putAll(collect);
        }
        return diffFields;
    }

    private String newName(Object f) {
        if (hasField(f.getClass(), "id")) {
            Object fieldValue = mapProperty(getFieldValue(f, "id"));
            String id = Objects.toString(fieldValue, "").replaceAll("[\\W_]", "");
            if (StringUtils.isNotBlank(id)) {
                String string = changeCase(id);
                if (StringUtils.isNumeric(string)) {
                    return changeCase(f.getClass().getSimpleName()) + string;
                }
                if (referencedNodes.values().stream().anyMatch(string::equals)) {
                    return string + referencedNodes.size();
                }
                return string;
            }
        }
        String simpleName = f.getClass().getSimpleName();
        String simple = changeCase(simpleName);
        return simple + referencedNodes.size();
    }

    private void processField(Element element, String fieldName, Object fieldValue, Object parent) {
        if (IGNORE.contains(fieldName)) {
            return;
        }
        if (containsSame(allNode, fieldValue)) {
            processNamedArgs(element, fieldName, fieldValue, parent);
            return;
        }

        if (fieldValue instanceof Collection) {
            processList(element, fieldName, parent, (Collection<?>) fieldValue);
            return;
        }
        if (hasClass(ATTRIBUTE_CLASSES, fieldValue.getClass()) && hasField(parent.getClass(), fieldName)
            && (fieldValue instanceof String || isSetterMatches(fieldName, fieldValue, parent))) {
            Object mapProperty2 = mapProperty(fieldValue);
            element.setAttribute(fieldName, mapProperty2 + "");
            return;
        }
        if (hasClass(CONDITIONAL_TAG_CLASSES, fieldValue.getClass()) && hasField(parent.getClass(), fieldName)) {
            Element createElement2 = document.createElement(fieldName);
            element.appendChild(createElement2);
            nodeMap.put(fieldValue, createElement2);
            allNode.add(fieldValue);
            return;
        }
        if (fieldValue instanceof Map) {
            processMap(element, fieldValue, fieldName);
            return;
        }
        if (METHOD_CLASSES.stream().anyMatch(c -> c.isInstance(fieldValue))) {
            if (!hasPublicConstructor(fieldValue.getClass())) {
                processMethod(element, fieldName, fieldValue, parent);
                return;
            }
            if (!referenceClasses.contains(fieldValue.getClass())) {
                LOG.info("{} added to referenceClasses", fieldValue.getClass());
            }
            referenceClasses.add(fieldValue.getClass());
        }
        if (getNamedArgs(parent.getClass()).contains(fieldName) && hasPublicConstructor(fieldValue.getClass())) {
            if (!referenceClasses.contains(fieldValue.getClass())) {
                LOG.info("{} added to referenceClasses", fieldValue.getClass());
            }
            referenceClasses.add(fieldValue.getClass());
        }
        if (hasClass(referenceClasses, fieldValue.getClass())) {
            processReferenceNode(element, fieldName, fieldValue);
            return;
        }
        if (!hasField(parent.getClass(), fieldName)) {
            return;
        }
        if (!hasClass(ATTRIBUTE_CLASSES, fieldValue.getClass())) {
            Class<? extends Object> class1 = fieldValue.getClass();
            List<Class<?>> allClasses = allClasses(class1);
            LOG.info(" {} not in ATTRIBUTE_CLASSES", allClasses);
        }

        LOG.info("{} does have {}", parent.getClass(), fieldName);

    }

    private void processList(Element element, String fieldName, Object parent, Collection<?> list) {
        if (list.isEmpty()) {
            return;
        }
        if (FORMAT_LIST.containsKey(fieldName)) {
            String apply = FORMAT_LIST.get(fieldName).apply(list);
            element.setAttribute(fieldName, apply);
            return;
        }
        if (list.stream().filter(Objects::nonNull).anyMatch(o -> hasClass(ATTRIBUTE_CLASSES, o.getClass()))) {
            Element appendTo = createListElement(element, fieldName, parent, list);
            list.stream().filter(Objects::nonNull).forEach(object -> {
                packages.add(object.getClass().getPackage().getName());
                Element inlineEl = document.createElement(object.getClass().getSimpleName());
                inlineEl.setAttribute(FX_VALUE, object + "");
                appendTo.appendChild(inlineEl);
            });
            return;
        }
        if (list.stream().filter(Objects::nonNull).anyMatch(o -> hasClass(NEW_TAG_CLASSES, o.getClass()))) {
            if (list.stream().anyMatch(o -> !containsSame(allNode, o))) {
                Element originalElement = createListElement(element, fieldName, parent, list);
                for (Object object : list) {
                    if (object != null && !containsSame(allNode, object)) {
                        nodeMap.put(object, originalElement);
                        allNode.add(object);
                    }
                }
            }
            return;
        }

        if (list.stream().filter(Objects::nonNull).allMatch(o -> isClassPublic(o.getClass()))
            && hasField(parent.getClass(), fieldName)) {
            Element createElement2 = createListElement(element, fieldName, parent, list);
            for (Object object : list) {
                if (object != null) {
                    nodeMap.put(object, createElement2);
                    allNode.add(object);
                }
            }
            return;
        }

        String classes = list.stream().findFirst().map(Object::getClass).map(Class::getName).orElse("");
        Class<? extends Object> parentClass = parent.getClass();
        LOG.info("attribute {} type {} of {} not set", fieldName, classes, parentClass);
        LOG.info("value {}", list);
    }

    private void processMap(Element element, Object fieldValue, String fieldName) {
        Map<?, ?> properties = (Map<?, ?>) fieldValue;
        if (isClassPublic(fieldValue.getClass())) {
            Element createElement2 = document.createElement(fieldName);
            element.appendChild(createElement2);
            Class<? extends Object> class1 = fieldValue.getClass();
            packages.add(class1.getPackage().getName());
            Element mapElement = document.createElement(class1.getSimpleName());
            createElement2.appendChild(mapElement);
            properties.forEach((k, v) -> {
                String string = Objects.toString(k).replaceAll("([#])", "");
                String key = PROPERTY_REMAP.getOrDefault(string, string);
                String value = Objects.toString(v, "");
                RunnableEx.make(() -> mapElement.setAttribute(key, value),
                    e -> LOG.error("error setting attribute {}={}", k, v));

            });
        } else {
            properties.forEach((k, v) -> {
                String string = Objects.toString(k);
                if (PROPERTY_REMAP.containsKey(string)) {
                    String key = PROPERTY_REMAP.getOrDefault(string, string);
                    String value = Objects.toString(v);
                    RunnableEx.make(() -> element.setAttribute(key, value),
                        e -> LOG.error("error setting attribute {}={}", k, v)).run();
                }
            });
        }
    }

    private void processMethod(Element element, String fieldName, Object fieldValue, Object parent) {
        String nodeName = referencedNodes.computeIfAbsent(parent, this::newName);
        String nameMethod = referencedMethod.computeIfAbsent(fieldValue.getClass().getName(),
            e -> computeMethod(parent, fieldName, fieldValue, nodeName));
        element.setAttribute(fieldName, "#" + nameMethod.replaceAll("\\(.+\\)", ""));
    }

    private void processNamedArgs(Element element, String fieldName, Object fieldValue, Object parent) {
        if (allNode.indexOf(parent) < allNode.indexOf(fieldValue)) {
            NodeList elementsByTagName = document.getElementsByTagName(FX_DEFINE);
            if (elementsByTagName.getLength() == 0) {
                Element firstChild = (Element) document.getFirstChild();
                Element createElement = document.createElement(FX_DEFINE);
                Node childNodes = firstChild.getFirstChild();
                firstChild.appendChild(createElement);
                firstChild.insertBefore(createElement, childNodes);
            }
            Node node = nodeMap.get(fieldValue);
            if (!node.getNodeName().equals(FX_DEFINE)) {
                originalMap.put(fieldValue, node);
                nodeMap.put(fieldValue, document.getElementsByTagName(FX_DEFINE).item(0));
            }
        }
        if (hasField(parent.getClass(), fieldName)) {
            String newFieldId = referencedNodes.computeIfAbsent(fieldValue, this::newName);
            element.setAttribute(fieldName, "$" + newFieldId);
        }
    }

    private void processNodes(File file, String packageName) {
        for (int i = 0; i < allNode.size(); i++) {
            Object node2 = allNode.get(i);
            org.w3c.dom.Node parent = nodeMap.getOrDefault(node2, document);
            String name = node2.getClass().getSimpleName().replaceAll("\\$", ".");
            String name2 = node2.getClass().getPackage().getName();
            if (node2.getClass().getEnclosingClass() != null) {
                name = node2.getClass().getEnclosingClass().getSimpleName() + "." + node2.getClass().getSimpleName();
            }
            packages.add(name2);
            Element createElement = document.createElement(name);
            parent.appendChild(createElement);

            Map<String, Object> fields = differences(node2);
            if (hasClass(ATTRIBUTE_CLASSES, node2.getClass())) {
                String nodeString = nodeValue(node2);
                createElement.setAttribute(FX_VALUE, nodeString);
                fields.clear();
            }

            fields.forEach((fieldName, fieldValue) -> {
                if (fieldValue != null) {
                    processField(createElement, fieldName, fieldValue, node2);
                }
            });
            if (originalMap.containsKey(node2)) {
                String newFieldId = referencedNodes.computeIfAbsent(node2, this::newName);
                Element referenceTag = document.createElement(FX_REFERENCE);
                referenceTag.setAttribute("source", newFieldId);
                originalMap.get(node2).appendChild(referenceTag);
            }

            if (hasClass(NECESSARY_REFERENCE, node2.getClass())) {
                referencedNodes.computeIfAbsent(node2, this::newName);
            }

            if (referencedNodes.containsKey(node2)) {
                createElement.setAttribute(FX_ID, referencedNodes.get(node2));
            }
            if (parent == document) {
                createElement.setAttribute("xmlns:fx", "http://javafx.com/fxml");
                String replaceAll = file.getName().replaceAll("\\.fxml", "");
                createElement.setAttribute("fx:controller", packageName + "." + replaceAll + "Controller");
            }
        }
    }

    private void processReferenceNode(Element element, String fieldName, Object fieldValue) {
        Class<? extends Object> targetClass = fieldValue.getClass();
        String simpleName = targetClass.getSimpleName();
        if (!referencedNodes.containsKey(fieldValue)) {
            Map<String, Object> differences = differences(fieldValue);
            Element defineElement;
            Node firstChild = document.getFirstChild().getFirstChild();
            if (FX_DEFINE.equals(firstChild.getNodeName())) {
                defineElement = (Element) firstChild;
            } else {
                defineElement = document.createElement(FX_DEFINE);
                Node firstChild2 = document.getFirstChild();
                firstChild2.appendChild(defineElement);
                firstChild2.insertBefore(defineElement, firstChild);
            }
            Element createElement = document.createElement(simpleName);
            defineElement.appendChild(createElement);
            differences.forEach((k, v) -> {
                if (v != null && hasClass(ATTRIBUTE_CLASSES, v.getClass()) && hasField(targetClass, k)) {
                    Object mapProperty2 = mapProperty(v);
                    String value = mapProperty2 + "";
                    createElement.setAttribute(k, value.replaceAll("\\.0$", ""));
                }
            });
            packages.add(targetClass.getPackage().getName());
            String name = referencedNodes.computeIfAbsent(fieldValue, this::newName);
            createElement.setAttribute(FX_ID, name);
        }

        element.setAttribute(fieldName, "$" + referencedNodes.get(fieldValue));
    }

    private static void addDifferences(Class<?> cl, Map<String, Object> diffFields, Object ob1, Object ob2) {
        if (ob2 == null) {
            return;
        }
        List<String> differences = TreeElement.getDifferences(cl, ob1, ob2);
        for (String f : differences) {
            diffFields.put(f, invoke(ob1, f));
        }
    }

    private static boolean containsSame(List<Object> allNode, Object fieldValue) {
        return allNode.stream().anyMatch(ob -> ob == fieldValue);
    }

    private static String nodeValue(Object node2) {
        String nodeString = Objects.toString(node2, "");
        if (node2.getClass().isEnum()) {
            return ((Enum<?>) node2).name();
        }
        return nodeString;
    }
}
