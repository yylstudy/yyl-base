package com.cqt.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.json.util.XMLMultipleEventWriter;
import de.odysseus.staxon.xml.util.PrettyXMLEventWriter;
import lombok.extern.slf4j.Slf4j;

import javax.xml.stream.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author linshiqiang
 */
@Slf4j
public class JsonAndXmlUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    public static <T> T xmlToEntity(Class<T> clazz, String xmlString) throws JsonProcessingException {
        String toJson = xmlToJson(xmlString);
        return OBJECT_MAPPER.readValue(toJson, clazz);
    }

    public static <T> T jsonToEntity(Class<T> clazz, String jsonString) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(jsonString, clazz);
    }

    public static String entityToXml(Object obj) throws JsonProcessingException {
        String json = OBJECT_MAPPER.writeValueAsString(obj);
        return jsonToXml(json);
    }

    public static Map<String, Object> xmlToMap(String xmlString) throws JsonProcessingException {

        String jsonStr = xmlToJson(xmlString);
        return OBJECT_MAPPER.readValue(jsonStr, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * xml转json 可指定字段为数组
     *
     * @param xmlString     xml
     * @param matchRoot     匹配
     * @param multiplePaths 数组字段路径
     * @return json
     */
    public static String xmlToJson(String xmlString, boolean matchRoot, String... multiplePaths) {
        StringReader input = new StringReader(xmlString);
        StringWriter output = new StringWriter();
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
        try {
            XMLEventReader reader = inputFactory.createXMLEventReader(input);
            XMLOutputFactory outputFactory = new JsonXMLOutputFactory();
            outputFactory.setProperty(JsonXMLOutputFactory.PROP_PRETTY_PRINT, true);
            XMLEventWriter writer = outputFactory.createXMLEventWriter(output);
            writer = new XMLMultipleEventWriter(writer, matchRoot, multiplePaths);
            writer.add(reader);
            reader.close();
            writer.close();
        } catch (XMLStreamException e) {
            log.error("xmlToJson: {},  error: ", xmlString, e);
        } finally {
            try {
                output.close();
                input.close();
            } catch (IOException e) {
                log.error("xmlToJson close error: ", e);
            }
        }
        return output.toString();
    }

    public static String xmlToJson(String xmlString) {

        StringReader input = new StringReader(xmlString);
        StringWriter output = new StringWriter();
        JsonXMLConfig config = new JsonXMLConfigBuilder()
                .autoArray(true)
                .autoPrimitive(true)
                .prettyPrint(true)
                .build();
        try {

            XMLEventReader reader = inputFactory.createXMLEventReader(input);
            XMLEventWriter writer = new JsonXMLOutputFactory(config).createXMLEventWriter(output);
            writer.add(reader);
            reader.close();
            writer.close();
        } catch (Exception e) {
            log.error("xmlToJson error: ", e);
        } finally {
            try {
                output.close();
                input.close();
            } catch (IOException e) {
                log.error("xmlToJson close error: ", e);
            }
        }
        return output.toString();
    }

    public static String jsonToXml(String jsonString) {
        StringReader input = new StringReader(jsonString);
        StringWriter output = new StringWriter();
        JsonXMLConfig config = new JsonXMLConfigBuilder()
                .multiplePI(false)
                .repairingNamespaces(false)
                .prettyPrint(true)
                .build();
        try {
            XMLEventReader reader = new JsonXMLInputFactory(config).createXMLEventReader(input);
            XMLEventWriter writer = xmlOutputFactory.createXMLEventWriter(output);
            writer = new PrettyXMLEventWriter(writer);
            writer.add(reader);
            reader.close();
            writer.close();
        } catch (Exception e) {
            log.error("jsonToXml error: ", e);
        } finally {
            try {
                output.close();
                input.close();
            } catch (IOException e) {
                log.error("jsonToXml error: close", e);
            }
        }
        // remove <?xml version="1.0" encoding="UTF-8"?>
        if (output.toString().length() >= 38) {
            return output.toString().substring(39);
        }
        return output.toString();
    }
}
