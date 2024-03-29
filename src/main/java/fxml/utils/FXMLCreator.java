package fxml.utils;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static utils.ClassReflectionUtils.*;
import static utils.ResourceFXUtils.getOutFile;
import static utils.StringSigaUtils.changeCase;
import static utils.TreeElement.getDifferences;
import static utils.ex.RunnableEx.make;

import extract.web.DocumentHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.ClassReflectionUtils;
import utils.TermFrequency;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class FXMLCreator {
    private static final String FX_REFERENCE = "fx:reference";
    private static final String FX_FACTORY = "fx:factory";
    private static final String FX_ID = "fx:id";
    private static final String FX_VALUE = "fx:value";
    private static final String FX_DEFINE = "fx:define";
    private static final Logger LOG = HasLogging.log();

    private static final String PACKAGE_NAME = FXMLCreator.class.getPackage().getName();
    private Collection<Class<?>> referenceClasses = new HashSet<>(FXMLConstants.getReferenceClasses());
    private Document document;
    private Map<Object, org.w3c.dom.Node> nodeMap = new IdentityHashMap<>();
    private Map<Object, List<org.w3c.dom.Node>> originalMap = new IdentityHashMap<>();
    private List<String> persistentFields = new ArrayList<>();
    private List<Object> allNode = new ArrayList<>();
    private Set<String> packages = new LinkedHashSet<>();
    private Map<String, String> referencedMethod = new LinkedHashMap<>();
    private Map<Object, String> referencedNodes = new IdentityHashMap<>();

    private LinkedHashMap<Class<?>, List<String>> differencesMap = new LinkedHashMap<>();

    public FXMLCreator() {
    }

    public FXMLCreator(List<String> persistentFields) {
        this.persistentFields.addAll(persistentFields);
    }
    public void createFXMLFile(Object node, File file) {
        RunnableEx.remap(() -> {
            document = DocumentHelper.newDocument();
            allNode.add(node);
            processNodes(file);

            Node firstChild = document.getFirstChild();
            document.removeChild(firstChild);
            packages.forEach(p -> document.appendChild(document.createProcessingInstruction("import", p + ".*")));
            document.appendChild(firstChild);
            DocumentHelper.saveToFile(document, file);
            createController(file);
        }, "ERROR in file " + file.getName());
    }

    private void addReferenceList(Element element, String fieldName, Object parent, Collection<?> list) {
        Element originalElement = createListElement(element, fieldName, parent, list);
        for (Object object : list) {
            if (object != null) {
                if (!containsSame(allNode, object)) {
                    addToAllNode(object, originalElement);
                } else if (!Objects.equals(nodeMap.get(object), originalElement)) {
                    originalMap.computeIfAbsent(object, o -> new ArrayList<>()).add(originalElement);
                }
            }
        }
    }

    private void addToAllNode(Object fieldValue, Element createElement2) {
        nodeMap.put(fieldValue, createElement2);
        allNode.add(fieldValue);
    }

    private String computeMethod(Object parent, String fieldName, Object fieldValue, String nodeId) {
        String signature = ClassReflectionUtils.getSignature(parent, fieldName, fieldValue, packages);
        String nodeId2 = Character.isLowerCase(nodeId.charAt(0)) ? changeCase(nodeId) : nodeId;
        return fieldName + nodeId2 + signature;
    }

    private void createController(File file) throws IOException {
        String name = file.getName().replaceAll("\\.fxml", "") + "Controller";
        File outFile = getOutFile("java/" + name + ".java");
        List<String> lines = new ArrayList<>();
        lines.add("package " + PACKAGE_NAME + ";");
        lines.add("import javafx.fxml.*;");
        lines.addAll(packages.stream().map(e -> "import " + e + ".*;").collect(toList()));
        lines.add("public class " + name + "{");
        lines.addAll(referencedNodes.entrySet().stream().map(
                e -> String.format("\t@FXML%n\tprivate %s %s;", e.getKey().getClass().getSimpleName(), e.getValue()))
                .collect(toList()));
        lines.addAll(referencedMethod.values().stream().map(e -> String.format("\tpublic void %s{%n\t}", e))
                .collect(toList()));
        lines.add("}");
        Files.write(outFile.toPath(), lines);
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

    private Map<String, Object> differences(Object ob1) {
        Map<String, Object> diffFields = new LinkedHashMap<>();
        Class<?> cl = ob1.getClass();
        if (ob1 instanceof Image) {
            String url = (String) ClassReflectionUtils.invoke(ob1, "impl_getUrl");
            RunnableEx.runIf(url, u -> {
                String mapUrl = mapUrl(u);
                diffFields.put("url", mapUrl);
            });
        }
        try {
            Object ob2 = getInstance(cl);
            addDifferences(cl, diffFields, ob1, ob2);
        } catch (Exception e) {
            LOG.trace("", e);
            List<String> mappedDifferences = differencesMap.computeIfAbsent(cl,
                    c -> allNode.stream().filter(ob -> ob.getClass() == cl && ob != ob1).limit(100)
                            .flatMap(ob2 -> getDifferences(c, ob1, ob2).stream()).distinct().collect(toList()));
            mappedDifferences.addAll(getNamedArgs(cl));
            Map<String, Object> fieldMap = mappedDifferences.stream().distinct().filter(m -> invoke(ob1, m) != null)
                    .collect(toMap(m -> m, m -> invoke(ob1, m), SupplierEx::nonNull, LinkedHashMap::new));
            diffFields.putAll(fieldMap);
        }
        Map<String, Object> different = persistentFields.stream().filter(s -> ClassReflectionUtils.hasGetter(cl, s))
                .collect(Collectors.toMap(s -> s, s -> invoke(ob1, s)));
        diffFields.putAll(different);
        return diffFields;
    }

    private boolean isReferenceableList(String fieldName, Collection<?> list) {
        return list.stream().filter(Objects::nonNull).anyMatch(o -> !containsSame(allNode, o))
                || !"children".equals(fieldName) && !list.getClass().getSimpleName().contains("Unmodifiable");
    }

    private String newName(Object f) {
        if (hasField(f.getClass(), "id")) {
            Object fieldValue = ClassReflectionUtils.mapProperty(getFieldValue(f, "id"));
            String id = Objects.toString(fieldValue, "").replaceAll("[\\W_]", "");
            if (StringUtils.isNotBlank(id)) {
                String finalID = Character.isLowerCase(id.charAt(0)) ? id : changeCase(id);
                if (StringUtils.isNumeric(finalID)) {
                    return changeCase(f.getClass().getSimpleName()) + finalID;
                }
                if (referencedNodes.values().stream().anyMatch(finalID::equals)
                        || TermFrequency.getJavaKeywords().contains(finalID)) {
                    return finalID + referencedNodes.size();
                }
                return finalID;
            }
        }
        String simpleName = f.getClass().getSimpleName();
        String simple = changeCase(simpleName);
        return simple + referencedNodes.size();
    }

    private void processField(Element element, String fieldName, Object fieldValue, Object parent) {
        if (containsSame(allNode, fieldValue)) {
            processNamedArgs(element, fieldName, fieldValue, parent);
            return;
        }

        if (fieldValue instanceof Collection) {
            processList(element, fieldName, parent, (Collection<?>) fieldValue);
            return;
        }
        if (isSuitableAsAttribute(fieldName, fieldValue, parent)) {
            Object mapProperty2 = ClassReflectionUtils.mapProperty(fieldValue);
            element.setAttribute(fieldName, mapProperty2 + "");
            return;
        }
        if (tryCreateAttribute(element, fieldName, fieldValue, parent)) {
            return;
        }
        if (fieldValue instanceof Map) {
            processMap(element, fieldValue, fieldName);
            return;
        }
        if (tryCreateMethod(element, fieldName, fieldValue, parent)) {
            return;
        }
        if (isStarterArgument(fieldName, fieldValue, parent)) {
            referenceClasses.add(fieldValue.getClass());
        }
        if (hasClass(referenceClasses, fieldValue.getClass())) {
            processReferenceNode(element, fieldName, fieldValue);
            return;
        }
        if (!hasField(parent.getClass(), fieldName)) {
            return;
        }
        logIfNotAttribute(fieldValue);

        LOG.info("{} does have {}", parent.getClass(), fieldName);

    }

    private void processList(Element element, String fieldName, Object parent, Collection<?> list) {
        if (notNullNorEmpty(list)) {
            return;
        }
        if (FXMLConstants.getFormatList().containsKey(fieldName)) {
            String apply = FXMLConstants.getFormatList().get(fieldName).apply(list);
            element.setAttribute(fieldName, apply);
            return;
        }
        if (list.stream().filter(Objects::nonNull)
                .anyMatch(o -> hasClass(FXMLConstants.getAttributeClasses(), o.getClass()))) {
            Element appendTo = createListElement(element, fieldName, parent, list);
            list.stream().filter(Objects::nonNull).forEach(object -> {
                packages.add(object.getClass().getPackage().getName());
                Element inlineEl = document.createElement(object.getClass().getSimpleName());
                inlineEl.setAttribute(FX_VALUE, object + "");
                appendTo.appendChild(inlineEl);
            });
            return;
        }
        if (list.stream().filter(Objects::nonNull)
                .anyMatch(o -> hasClass(FXMLConstants.getNewTagClasses(), o.getClass()))) {
            if (isReferenceableList(fieldName, list)) {
                addReferenceList(element, fieldName, parent, list);
                return;
            }
            logIfNotChildren(fieldName, parent);
            return;
        }

        if (list.stream().filter(Objects::nonNull).allMatch(o -> isClassPublic(o.getClass()))
                && hasField(parent.getClass(), fieldName)) {
            Element createElement2 = createListElement(element, fieldName, parent, list);
            for (Object object : list) {
                if (object != null) {
                    addToAllNode(object, createElement2);
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
                String key = FXMLConstants.getPropertyRemap().getOrDefault(string, string);
                String value = Objects.toString(v, "");
                make(() -> mapElement.setAttribute(key, value), e -> LOG.error("error setting attribute {}={}", k, v))
                        .run();
            });
            return;
        }
        properties.forEach((k, v) -> {
            String string = Objects.toString(k);
            if (FXMLConstants.getPropertyRemap().containsKey(string)) {
                String key = FXMLConstants.getPropertyRemap().getOrDefault(string, string);
                String value = Objects.toString(v);
                make(() -> element.setAttribute(key, value), e -> LOG.error("error setting attribute {}={}", k, v))
                        .run();
            }
        });
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
                originalMap.computeIfAbsent(fieldValue, o -> new ArrayList<>()).add(node);
                nodeMap.put(fieldValue, document.getElementsByTagName(FX_DEFINE).item(0));
            }
        }
        if (hasField(parent.getClass(), fieldName)) {
            String newFieldId = referencedNodes.computeIfAbsent(fieldValue, this::newName);
            element.setAttribute(fieldName, "$" + newFieldId);
        }
    }

    private void processNodes(File file) {
        for (int i = 0; i < allNode.size(); i++) {
            Object node2 = allNode.get(i);
            org.w3c.dom.Node parent = nodeMap.getOrDefault(node2, document);
            String packageName2 = node2.getClass().getPackage().getName();
            String name = getClassName(node2);
            packages.add(packageName2);
            Element createElement = document.createElement(name);
            parent.appendChild(createElement);

            Map<String, Object> fields = differences(node2);
            if (hasClass(FXMLConstants.getAttributeClasses(), node2.getClass())) {
                String nodeString = nodeValue(node2);
                createElement.setAttribute(FX_VALUE, nodeString);
                fields.clear();
            }

            fields.forEach((fieldName, fieldValue) -> {
                if (fieldValue != null && !FXMLConstants.getIgnore().contains(fieldName)) {
                    processField(createElement, fieldName, fieldValue, node2);
                }
            });
            if (originalMap.containsKey(node2)) {
                String newFieldId = referencedNodes.computeIfAbsent(node2, this::newName);
                Element referenceTag = document.createElement(FX_REFERENCE);
                referenceTag.setAttribute("source", newFieldId);
                List<Node> list = originalMap.get(node2);
                list.forEach(l -> l.appendChild(referenceTag.cloneNode(false)));
            }

            if (hasClass(FXMLConstants.getNecessaryReference(), node2.getClass())) {
                referencedNodes.computeIfAbsent(node2, this::newName);
            }

            if (referencedNodes.containsKey(node2)) {
                createElement.setAttribute(FX_ID, referencedNodes.get(node2));
            }
            if (parent == document) {
                createElement.setAttribute("xmlns:fx", "http://javafx.com/fxml");
                String replaceAll = file.getName().replaceAll("\\.fxml", "");
                createElement.setAttribute("fx:controller", PACKAGE_NAME + "." + replaceAll + "Controller");
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
                if (v != null && hasClass(FXMLConstants.getAttributeClasses(), v.getClass())
                        && hasField(targetClass, k)) {
                    Object mapProperty2 = ClassReflectionUtils.mapProperty(v);
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

    private boolean tryCreateAttribute(Element element, String fieldName, Object fieldValue, Object parent) {
        boolean a = false;
        if (hasClass(FXMLConstants.getConditionalTagClasses(), fieldValue.getClass())
                && hasField(parent.getClass(), fieldName)) {
            Element createElement2 = document.createElement(fieldName);
            element.appendChild(createElement2);
            addToAllNode(fieldValue, createElement2);
            a = true;
        }
        return a;
    }

    private boolean tryCreateMethod(Element element, String fieldName, Object fieldValue, Object parent) {
        if (FXMLConstants.getMethodClasses().stream().anyMatch(c -> c.isInstance(fieldValue))) {
            if (!hasPublicConstructor(fieldValue.getClass())) {
                processMethod(element, fieldName, fieldValue, parent);
                return true;
            }
            referenceClasses.add(fieldValue.getClass());
        }
        return false;
    }

    private static void addDifferences(Class<?> cl, Map<String, Object> diffFields, Object ob1, Object ob2) {
        if (ob2 == null) {
            return;
        }
        List<String> differences = getDifferences(cl, ob1, ob2);
        for (String f : differences) {
            diffFields.put(f, invoke(ob1, f));
        }
    }

    private static boolean containsSame(List<Object> allNode, Object fieldValue) {
        return allNode.stream().anyMatch(ob -> ob == fieldValue);
    }

    private static String getClassName(Object node2) {
        if (node2.getClass().getEnclosingClass() != null) {
            return node2.getClass().getEnclosingClass().getSimpleName() + "." + node2.getClass().getSimpleName();
        }
        return node2.getClass().getSimpleName().replaceAll("\\$", ".");
    }

    private static boolean isStarterArgument(String fieldName, Object fieldValue, Object parent) {
        return getNamedArgs(parent.getClass()).contains(fieldName) && hasPublicConstructor(fieldValue.getClass());
    }

    private static boolean isSuitableAsAttribute(String fieldName, Object fieldValue, Object parent) {
        return hasClass(FXMLConstants.getAttributeClasses(), fieldValue.getClass())
                && hasField(parent.getClass(), fieldName)
                && (fieldValue instanceof String || isSetterMatches(fieldName, fieldValue, parent));
    }

    private static void logIfNotAttribute(Object fieldValue) {
        if (!hasClass(FXMLConstants.getAttributeClasses(), fieldValue.getClass())) {
            Class<? extends Object> class1 = fieldValue.getClass();
            List<Class<?>> allClasses = allClasses(class1);
            LOG.info(" {} not in ATTRIBUTE_CLASSES", allClasses);
        }
    }

    private static void logIfNotChildren(String fieldName, Object parent) {
        if (!"children".equals(fieldName)) {
            Class<? extends Object> parentClass = parent.getClass();
            LOG.info("{} of {} not set", fieldName, parentClass);
        }
    }

    private static String mapUrl(String st) {
        File file = new File(getOutFile().getParentFile(), "classes");
        String s = "file:/" + file.toString().replaceAll("\\\\", "/");
        return st.replace(s, "@");
    }

    private static String nodeValue(Object node2) {
        if (node2.getClass().isEnum()) {
            return ((Enum<?>) node2).name();
        }
        return Objects.toString(node2, "");
    }

    private static boolean notNullNorEmpty(Collection<?> list) {
        return list.isEmpty() || list.stream().allMatch(Objects::isNull);
    }
}
