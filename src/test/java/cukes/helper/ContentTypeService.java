package cukes.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import cukes.type.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.*;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Service
public class ContentTypeService {

    public Map<String, String> getParameters(String payload) throws UnsupportedEncodingException {
        Map<String, String> parameters = new LinkedHashMap<>();
        payload = URLDecoder.decode(payload, StandardCharsets.UTF_8.name());
        payload = payload.replaceAll("\\s+","");
        String[] pairs = payload.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            parameters.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        return parameters;
    }

    public void assertContentByType(ContentType contentType, String expectedContent, String actualContent) throws IOException, JSONException, SAXException {
        if(contentType == ContentType.JSON) {
            assertJSON(expectedContent, actualContent);
        }
        else if(contentType == ContentType.XML) {
            assertXML(expectedContent, actualContent);
        }
    }

    private void assertJSON(String expectedJSON, String actualJSON) throws JSONException {
        JSONCompareResult result = JSONCompare.compareJSON(expectedJSON, actualJSON, JSONCompareMode.LENIENT);
        if(result.failed()) {
            System.out.println("act json: " + actualJSON);
            System.out.println("expected json: " + expectedJSON);
            throw new AssertionError(result.getMessage());
        }
    }

    private void assertXML(String expectedXML, String actualXML) throws IOException, SAXException {
        XMLUnit.setIgnoreComments(Boolean.TRUE);
        XMLUnit.setIgnoreWhitespace(Boolean.TRUE);
        XMLUnit.setNormalizeWhitespace(Boolean.TRUE);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(Boolean.TRUE);
        XMLUnit.setIgnoreAttributeOrder(Boolean.TRUE);
        XMLUnit.setCompareUnmatched(Boolean.FALSE);

        DetailedDiff detailedDiff = new DetailedDiff(XMLUnit.compareXML(expectedXML, actualXML));
        detailedDiff.overrideDifferenceListener(new DifferenceListener() {

            @Override
            public void skippedComparison(Node node1, Node node2) { /* skip comparison */ }

            @Override
            public int differenceFound(Difference difference) {
                if (difference.getId() == DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                return RETURN_ACCEPT_DIFFERENCE;
            }
        });

        if(!detailedDiff.similar()) {
            StringBuilder builder = new StringBuilder();

            for (Object object : detailedDiff.getAllDifferences()) {
                builder.append(object.toString()).append("\n");
            }
            throw new RuntimeException(builder.toString());
        }
    }

    public String getContentTypeString(ContentType contentType, Object object) throws JsonProcessingException, JAXBException {
        if(contentType == ContentType.JSON) {
            return getJSONString(object);
        }
        else if(contentType == ContentType.XML) {
            return getXMLString(object);
        }

        return null;
    }

    private String getXMLString(Object object) throws JAXBException {
        StringWriter writer = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(object, writer);
        return writer.toString();
    }

    private String getJSONString(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    public  <T> T getContentTypeObject(ContentType contentType, Class<T> clazz, String contentString) throws IOException, JAXBException {
        if(contentType == ContentType.JSON) {
            return getJsonObject(clazz, contentString);
        } else if(contentType == ContentType.XML) {
            return getXmlObject(clazz, contentString);
        } else {
            try {
                return clazz.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private <T> T getJsonObject(Class<T> clazz, String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(jsonString, clazz);
    }

    private <T> T getXmlObject(Class<T> clazz, String xmlString) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(xmlString);
        return clazz.cast(unmarshaller.unmarshal(reader));
    }

    public boolean matchContentByConditions(ContentType contentType, String content, Map<String, String> conditions, boolean doAssert) {
        if(contentType == ContentType.JSON) {
            return matchJSONByConditions(content, conditions, doAssert);
        }
        else if(contentType == ContentType.XML) {
            return matchXMLByConditions(content, conditions, doAssert);
        }

        return false;
    }

    private boolean matchJSONByConditions(String content, Map<String, String> conditions, boolean doAssert) {

        if(content == null || conditions == null) {
            return true;
        }

        boolean match = true;
        DocumentContext documentContext = JsonPath.parse(content);

        for (Map.Entry<String, String> entry : conditions.entrySet()) {

            String pathKey = entry.getKey();

            if(StringUtils.isBlank(pathKey)) {
                match = false;
                continue;
            }

            pathKey = pathKey.trim();

            if(!pathKey.startsWith("$.")) {
                pathKey = "$." + pathKey;
            }

            Object object = documentContext.read(pathKey);
            String actualValue = String.valueOf(object);

            if(doAssert) {
                assertEquals(entry.getValue(), actualValue);
            } else if (!actualValue.equals(entry.getValue())) {
                return false;
            }
        }

        return match;
    }

    private boolean matchXMLByConditions(String content, Map<String, String> conditions, boolean doAssert) {

        if(content == null || conditions == null) {
            return true;
        }

        try {
            Document document = getDocument(content);

            for (Map.Entry<String, String> entry : conditions.entrySet()) {

                String xpathExpression = entry.getKey().trim();

                if(!xpathExpression.contains("/")) {
                    if(xpathExpression.contains("(") && xpathExpression.contains(")")) {
                        xpathExpression = xpathExpression.replace("(", "(//");
                    }
                    else {
                        xpathExpression = "/" + entry.getKey();
                    }
                    xpathExpression = xpathExpression.replace(".", "/");
                }

                String elementValue = getElementByXpathExpression(document, xpathExpression);

                if(doAssert) {
                    assertEquals(entry.getValue(), elementValue);
                } else if (elementValue == null || !elementValue.equals(entry.getValue())) {
                    return false;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return true;
    }

    private Document getDocument(String xmlDocument) throws IOException, ParserConfigurationException, SAXException {
        ByteArrayInputStream xmlStream = new ByteArrayInputStream(xmlDocument.getBytes());
        Reader reader = new InputStreamReader(xmlStream,"UTF-8");
        InputSource inputSource = new InputSource(reader);
        inputSource.setEncoding("UTF-8");

        //get the factory
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        //Using factory get an instance of document builder
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

        //parse using builder to get DOM representation of the XML file
        return docBuilder.parse(inputSource);
    }

    private String getElementByXpathExpression(Document document, String xpathExpression) throws XPathExpressionException {
        XPathExpression xp = XPathFactory.newInstance().newXPath().compile(xpathExpression);
        return xp.evaluate(document);
    }
}
