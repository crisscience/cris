/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.purdue.cybercenter.dm.util;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

/**
 *
 * @author xu222
 */
public class Helper {

    public static int streamToFile(InputStream is, String path) throws FileNotFoundException, IOException {
        int size;
        try (OutputStream os = new FileOutputStream(path)) {
            size = IOUtils.copy(is, os);
            os.flush();
        }
        return size;
    }

    public static int fileToStream(String path, OutputStream os) throws FileNotFoundException, IOException {
        int size;
        try (InputStream is = new FileInputStream(path)) {
            size = IOUtils.copy(is, os);
        }
        return size;
    }

    public static String fileToString(String filename) {
        String content;
        File file = new File(filename);
        try (InputStream is = new FileInputStream(file)) {
            content = IOUtils.toString(is);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(String.format("%s: file does not exist: %s", ex.getMessage(), filename));
        } catch (IOException ex) {
            throw new RuntimeException(String.format("%s: Unable to convert file to string: %s", ex.getMessage(), filename));
        }

        return content;
    }

    public static void stringToFile(String string, String filename) throws FileNotFoundException, IOException {
        try (OutputStream os = new FileOutputStream(filename); InputStream is = IOUtils.toInputStream(string)) {
            IOUtils.copy(is, os);
        }
    }

    private static String serialize(Object object, boolean deep) {
        JsonUuidTransformer jut = new JsonUuidTransformer();
        JsonDateTransformer jdt = new JsonDateTransformer();

        JSONSerializer serializer = new JSONSerializer().exclude("*.class").exclude("$ref").transform(jut, UUID.class).transform(jdt, Date.class);
        String json;
        if (deep) {
            json = serializer.deepSerialize(object);
        } else {
            json = serializer.serialize(object);
        }
        return json;
    }

    public static String serialize(Object object) {
        return serialize(object, false);
    }

    public static String deepSerialize(Object object) {
        return serialize(object, true);
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        T object;
        if (StringUtils.isEmpty(json)) {
            object = null;
        } else {
            JsonUuidFactory juf = new JsonUuidFactory();
            JsonDateFactory jdf = new JsonDateFactory();
            object = new JSONDeserializer<T>().use(UUID.class, juf).use(Date.class, jdf).deserialize(json, clazz);
        }

        return object;
    }

    public static Map<String, Object> toMap(Object object) {
        return Helper.deserialize(Helper.deepSerialize(object), Map.class);
    }

    public static String replaceSpecialCharacters(String text) {
        return StringUtils.isEmpty(text) ? text : text.replaceAll("\\s", "_").replaceAll("\\W+", "_");
    }

}
