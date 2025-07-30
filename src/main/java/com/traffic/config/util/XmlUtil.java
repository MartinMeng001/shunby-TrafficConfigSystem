package com.traffic.config.util;

import com.traffic.config.exception.ConfigException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public final class XmlUtil {

    private XmlUtil() {
        // 工具类，禁止实例化
    }

    // ==================== JAXB操作 ====================

    /**
     * 对象转换为XML字符串
     *
     * @param object 要转换的对象
     * @param clazz 对象类型
     * @param formatted 是否格式化输出
     * @return XML字符串
     * @throws ConfigException 转换失败时抛出
     */
    public static <T> String objectToXml(T object, Class<T> clazz, boolean formatted) {
        if (object == null || clazz == null) {
            throw new IllegalArgumentException("对象和类型不能为空");
        }

        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Marshaller marshaller = context.createMarshaller();

            // 设置输出格式
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);

            return writer.toString();

        } catch (JAXBException e) {
            log.error("对象转XML失败: {}", clazz.getSimpleName(), e);
            throw new ConfigException("XML_MARSHAL_ERROR", "对象转XML失败", e);
        }
    }

    /**
     * 对象转换为XML字符串（格式化输出）
     *
     * @param object 要转换的对象
     * @param clazz 对象类型
     * @return XML字符串
     */
    public static <T> String objectToXml(T object, Class<T> clazz) {
        return objectToXml(object, clazz, true);
    }

    /**
     * XML字符串转换为对象
     *
     * @param xmlString XML字符串
     * @param clazz 目标对象类型
     * @return 转换后的对象
     * @throws ConfigException 转换失败时抛出
     */
    @SuppressWarnings("unchecked")
    public static <T> T xmlToObject(String xmlString, Class<T> clazz) {
        if (!StringUtils.hasText(xmlString) || clazz == null) {
            throw new IllegalArgumentException("XML字符串和类型不能为空");
        }

        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            StringReader reader = new StringReader(xmlString);
            return (T) unmarshaller.unmarshal(reader);

        } catch (JAXBException e) {
            log.error("XML转对象失败: {}", clazz.getSimpleName(), e);
            throw new ConfigException("XML_UNMARSHAL_ERROR", "XML转对象失败", e);
        }
    }

    /**
     * 对象保存为XML文件
     *
     * @param object 要保存的对象
     * @param clazz 对象类型
     * @param filePath 文件路径
     * @param formatted 是否格式化输出
     * @throws ConfigException 保存失败时抛出
     */
    public static <T> void objectToXmlFile(T object, Class<T> clazz, String filePath, boolean formatted) {
        if (object == null || clazz == null || !StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("对象、类型和文件路径不能为空");
        }

        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Marshaller marshaller = context.createMarshaller();

            // 设置输出格式
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            Path path = Paths.get(filePath);

            // 创建父目录
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            try (FileWriter writer = new FileWriter(path.toFile(), StandardCharsets.UTF_8)) {
                marshaller.marshal(object, writer);
            }

            log.debug("对象保存为XML文件成功: {}", filePath);

        } catch (JAXBException | IOException e) {
            log.error("对象保存为XML文件失败: {}", filePath, e);
            throw new ConfigException("XML_FILE_SAVE_ERROR", "对象保存为XML文件失败: " + filePath, e);
        }
    }

    /**
     * 从XML文件加载对象
     *
     * @param filePath 文件路径
     * @param clazz 目标对象类型
     * @return 加载的对象
     * @throws ConfigException 加载失败时抛出
     */
    @SuppressWarnings("unchecked")
    public static <T> T xmlFileToObject(String filePath, Class<T> clazz) {
        if (!StringUtils.hasText(filePath) || clazz == null) {
            throw new IllegalArgumentException("文件路径和类型不能为空");
        }

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new ConfigException("FILE_NOT_FOUND", "XML文件不存在: " + filePath);
            }

            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            try (FileReader reader = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
                return (T) unmarshaller.unmarshal(reader);
            }

        } catch (JAXBException | IOException e) {
            log.error("从XML文件加载对象失败: {}", filePath, e);
            throw new ConfigException("XML_FILE_LOAD_ERROR", "从XML文件加载对象失败: " + filePath, e);
        }
    }

    // ==================== DOM操作 ====================

    /**
     * 解析XML字符串为Document对象
     *
     * @param xmlString XML字符串
     * @return Document对象
     * @throws ConfigException 解析失败时抛出
     */
    public static Document parseXmlString(String xmlString) {
        if (!StringUtils.hasText(xmlString)) {
            throw new IllegalArgumentException("XML字符串不能为空");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();

            try (StringReader reader = new StringReader(xmlString)) {
                return builder.parse(new org.xml.sax.InputSource(reader));
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("解析XML字符串失败", e);
            throw new ConfigException("XML_PARSE_ERROR", "解析XML字符串失败", e);
        }
    }

    /**
     * 解析XML文件为Document对象
     *
     * @param filePath 文件路径
     * @return Document对象
     * @throws ConfigException 解析失败时抛出
     */
    public static Document parseXmlFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new ConfigException("FILE_NOT_FOUND", "XML文件不存在: " + filePath);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(path.toFile());

        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("解析XML文件失败: {}", filePath, e);
            throw new ConfigException("XML_FILE_PARSE_ERROR", "解析XML文件失败: " + filePath, e);
        }
    }

    /**
     * Document对象转换为XML字符串
     *
     * @param document Document对象
     * @param formatted 是否格式化输出
     * @return XML字符串
     * @throws ConfigException 转换失败时抛出
     */
    public static String documentToXmlString(Document document, boolean formatted) {
        if (document == null) {
            throw new IllegalArgumentException("Document对象不能为空");
        }

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            // 设置输出格式
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            if (formatted) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.toString();

        } catch (TransformerException e) {
            log.error("Document转XML字符串失败", e);
            throw new ConfigException("XML_TRANSFORM_ERROR", "Document转XML字符串失败", e);
        }
    }

    /**
     * Document对象保存为XML文件
     *
     * @param document Document对象
     * @param filePath 文件路径
     * @param formatted 是否格式化输出
     * @throws ConfigException 保存失败时抛出
     */
    public static void documentToXmlFile(Document document, String filePath, boolean formatted) {
        if (document == null || !StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("Document对象和文件路径不能为空");
        }

        try {
            Path path = Paths.get(filePath);

            // 创建父目录
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            // 设置输出格式
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            if (formatted) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }

            try (FileWriter writer = new FileWriter(path.toFile(), StandardCharsets.UTF_8)) {
                transformer.transform(new DOMSource(document), new StreamResult(writer));
            }

            log.debug("Document保存为XML文件成功: {}", filePath);

        } catch (TransformerException | IOException e) {
            log.error("Document保存为XML文件失败: {}", filePath, e);
            throw new ConfigException("XML_FILE_SAVE_ERROR", "Document保存为XML文件失败: " + filePath, e);
        }
    }

    // ==================== XPath操作 ====================

    /**
     * 使用XPath查询单个节点
     *
     * @param document Document对象
     * @param xpathExpression XPath表达式
     * @return 查询到的节点，如果没有找到则返回null
     * @throws ConfigException 查询失败时抛出
     */
    public static Node selectSingleNode(Document document, String xpathExpression) {
        if (document == null || !StringUtils.hasText(xpathExpression)) {
            throw new IllegalArgumentException("Document对象和XPath表达式不能为空");
        }

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (Node) xpath.evaluate(xpathExpression, document, XPathConstants.NODE);

        } catch (XPathExpressionException e) {
            log.error("XPath查询单个节点失败: {}", xpathExpression, e);
            throw new ConfigException("XPATH_QUERY_ERROR", "XPath查询失败: " + xpathExpression, e);
        }
    }

    /**
     * 使用XPath查询多个节点
     *
     * @param document Document对象
     * @param xpathExpression XPath表达式
     * @return 查询到的节点列表
     * @throws ConfigException 查询失败时抛出
     */
    public static NodeList selectNodes(Document document, String xpathExpression) {
        if (document == null || !StringUtils.hasText(xpathExpression)) {
            throw new IllegalArgumentException("Document对象和XPath表达式不能为空");
        }

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (NodeList) xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);

        } catch (XPathExpressionException e) {
            log.error("XPath查询多个节点失败: {}", xpathExpression, e);
            throw new ConfigException("XPATH_QUERY_ERROR", "XPath查询失败: " + xpathExpression, e);
        }
    }

    /**
     * 使用XPath查询节点文本内容
     *
     * @param document Document对象
     * @param xpathExpression XPath表达式
     * @return 节点文本内容，如果没有找到则返回null
     * @throws ConfigException 查询失败时抛出
     */
    public static String selectSingleNodeText(Document document, String xpathExpression) {
        if (document == null || !StringUtils.hasText(xpathExpression)) {
            throw new IllegalArgumentException("Document对象和XPath表达式不能为空");
        }

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (String) xpath.evaluate(xpathExpression, document, XPathConstants.STRING);

        } catch (XPathExpressionException e) {
            log.error("XPath查询节点文本失败: {}", xpathExpression, e);
            throw new ConfigException("XPATH_QUERY_ERROR", "XPath查询失败: " + xpathExpression, e);
        }
    }

    /**
     * 使用XPath更新节点文本内容
     *
     * @param document Document对象
     * @param xpathExpression XPath表达式
     * @param newValue 新的文本内容
     * @return 是否更新成功
     * @throws ConfigException 更新失败时抛出
     */
    public static boolean updateNodeText(Document document, String xpathExpression, String newValue) {
        if (document == null || !StringUtils.hasText(xpathExpression)) {
            throw new IllegalArgumentException("Document对象和XPath表达式不能为空");
        }

        try {
            Node node = selectSingleNode(document, xpathExpression);
            if (node != null) {
                node.setTextContent(newValue != null ? newValue : "");
                return true;
            }
            return false;

        } catch (Exception e) {
            log.error("XPath更新节点文本失败: {}", xpathExpression, e);
            throw new ConfigException("XPATH_UPDATE_ERROR", "XPath更新失败: " + xpathExpression, e);
        }
    }

    // ==================== XML验证 ====================

    /**
     * 验证XML字符串格式是否正确
     *
     * @param xmlString XML字符串
     * @return 是否格式正确
     */
    public static boolean isValidXml(String xmlString) {
        if (!StringUtils.hasText(xmlString)) {
            return false;
        }

        try {
            parseXmlString(xmlString);
            return true;
        } catch (Exception e) {
            log.debug("XML格式验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证XML文件格式是否正确
     *
     * @param filePath 文件路径
     * @return 是否格式正确
     */
    public static boolean isValidXmlFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }

        try {
            parseXmlFile(filePath);
            return true;
        } catch (Exception e) {
            log.debug("XML文件格式验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 使用XSD验证XML
     *
     * @param xmlString XML字符串
     * @param xsdFilePath XSD文件路径
     * @return 验证结果，包含是否有效和错误信息
     * @throws ConfigException 验证过程失败时抛出
     */
    public static ValidationResult validateXmlWithXsd(String xmlString, String xsdFilePath) {
        if (!StringUtils.hasText(xmlString) || !StringUtils.hasText(xsdFilePath)) {
            throw new IllegalArgumentException("XML字符串和XSD文件路径不能为空");
        }

        try {
            // 创建Schema
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = factory.newSchema(new File(xsdFilePath));

            // 创建Validator
            Validator validator = schema.newValidator();

            // 解析XML
            Document document = parseXmlString(xmlString);

            // 验证
            validator.validate(new DOMSource(document));

            return new ValidationResult(true, "XML验证通过");

        } catch (SAXException e) {
            log.warn("XML验证失败: {}", e.getMessage());
            return new ValidationResult(false, "XML验证失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("XML验证过程失败", e);
            throw new ConfigException("XML_VALIDATION_ERROR", "XML验证过程失败", e);
        }
    }

    /**
     * XML验证结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("ValidationResult{valid=%s, message='%s'}", valid, message);
        }
    }

    // ==================== XML格式化和美化 ====================

    /**
     * 格式化XML字符串
     *
     * @param xmlString 原始XML字符串
     * @param indent 缩进空格数
     * @return 格式化后的XML字符串
     * @throws ConfigException 格式化失败时抛出
     */
    public static String formatXml(String xmlString, int indent) {
        if (!StringUtils.hasText(xmlString)) {
            throw new IllegalArgumentException("XML字符串不能为空");
        }

        try {
            Document document = parseXmlString(xmlString);

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(Math.max(1, indent)));

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.toString();

        } catch (Exception e) {
            log.error("XML格式化失败", e);
            throw new ConfigException("XML_FORMAT_ERROR", "XML格式化失败", e);
        }
    }

    /**
     * 格式化XML字符串（默认2个空格缩进）
     *
     * @param xmlString 原始XML字符串
     * @return 格式化后的XML字符串
     */
    public static String formatXml(String xmlString) {
        return formatXml(xmlString, 2);
    }

    /**
     * 压缩XML字符串（移除格式化）
     *
     * @param xmlString 格式化的XML字符串
     * @return 压缩后的XML字符串
     * @throws ConfigException 压缩失败时抛出
     */
    public static String compactXml(String xmlString) {
        if (!StringUtils.hasText(xmlString)) {
            throw new IllegalArgumentException("XML字符串不能为空");
        }

        try {
            Document document = parseXmlString(xmlString);

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.toString();

        } catch (Exception e) {
            log.error("XML压缩失败", e);
            throw new ConfigException("XML_COMPACT_ERROR", "XML压缩失败", e);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取元素的所有子元素
     *
     * @param element 父元素
     * @return 子元素列表
     */
    public static List<Element> getChildElements(Element element) {
        List<Element> children = new ArrayList<>();
        if (element != null) {
            NodeList nodeList = element.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    children.add((Element) node);
                }
            }
        }
        return children;
    }

    /**
     * 获取元素的所有属性
     *
     * @param element 元素
     * @return 属性映射（属性名 -> 属性值）
     */
    public static Map<String, String> getElementAttributes(Element element) {
        Map<String, String> attributes = new HashMap<>();
        if (element != null && element.hasAttributes()) {
            org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                attributes.put(attr.getNodeName(), attr.getNodeValue());
            }
        }
        return attributes;
    }

    /**
     * 检查两个XML字符串是否等效（忽略格式差异）
     *
     * @param xml1 XML字符串1
     * @param xml2 XML字符串2
     * @return 是否等效
     */
    public static boolean isXmlEquivalent(String xml1, String xml2) {
        if (xml1 == null && xml2 == null) {
            return true;
        }
        if (xml1 == null || xml2 == null) {
            return false;
        }

        try {
            String compact1 = compactXml(xml1);
            String compact2 = compactXml(xml2);
            return compact1.equals(compact2);
        } catch (Exception e) {
            log.warn("XML等效性比较失败", e);
            return false;
        }
    }
}