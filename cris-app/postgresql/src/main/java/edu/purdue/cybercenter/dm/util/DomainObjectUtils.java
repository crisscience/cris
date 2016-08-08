/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.ObjectFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author xu222
 */
public class DomainObjectUtils {

    static final private String UnableToGetUseClass = "Unable to get use class: ";

    public static <T> String toJson(T object, String ctxPath) {
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        JsonUuidTransformer jut = new JsonUuidTransformer();
        JsonDateTransformer jdt = new JsonDateTransformer();
        return new JSONSerializer().exclude("*.class").exclude("$ref").include("id").exclude("*.id").transform(fjr, String.class).transform(jut, UUID.class).transform(jdt, Date.class).serialize(object);
    }  
    
    public static <T> String toJsonArray(Collection<T> collection, String ctxPath) {
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        JsonUuidTransformer jut = new JsonUuidTransformer();
        JsonDateTransformer jdt = new JsonDateTransformer();
        return new JSONSerializer().exclude("*.class").exclude("$ref").include("id").exclude("*.id").transform(fjr, String.class).transform(jut, UUID.class).transform(jdt, Date.class).serialize(collection);
    }

    public static <T> T fromJson(String json, String ctxPath, Class<T> clazz) {
        JSONDeserializer<T> deserializer = new JSONDeserializer<T>().use(null, clazz);

        JsonUuidFactory juf = new JsonUuidFactory();
        JsonDateFactory jdf = new JsonDateFactory();
        deserializer.use(UUID.class, juf);
        deserializer.use(Date.class, jdf);
        addUses(deserializer, ctxPath, clazz);

        return deserializer.deserialize(json);
    }

    public static <T> Collection<T> fromJsonArray(String json, String ctxPath, Class<T> clazz) {
        JSONDeserializer<List<T>> deserializer = new JSONDeserializer<List<T>>().use("values", clazz);

        JsonUuidFactory juf = new JsonUuidFactory();
        JsonDateFactory jdf = new JsonDateFactory();
        deserializer.use(UUID.class, juf);
        deserializer.use(Date.class, jdf);
        addUses(deserializer, ctxPath, clazz);

        return deserializer.deserialize(json);
    }

    /*********************************************************
     * Used internally
     *********************************************************/
    private static void addUses(JSONDeserializer deserializer, String ctxPath, Class clazz) {
        Map<Class, ObjectFactory> useClasses = getUseClasses(ctxPath, clazz);
        for (Map.Entry<Class, ObjectFactory> entry : useClasses.entrySet()) {
            deserializer.use(entry.getKey(), entry.getValue());
        }
    }

    private static <T> Map<Class, ObjectFactory> getUseClasses(String ctxPath, Class<T> clazz) {
        Map<Class, ObjectFactory> useClasses;
        try {
            Class[] classes = new Class[1];
            classes[0] = String.class;
            Method method = clazz.getMethod("getUseClasses", classes);
            useClasses = (Map<Class, ObjectFactory>) method.invoke(null, ctxPath);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(UnableToGetUseClass + ex.getMessage(), ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(UnableToGetUseClass + ex.getMessage(), ex);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(UnableToGetUseClass + ex.getMessage(), ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(UnableToGetUseClass + ex.getMessage(), ex);
        }
        return useClasses;
    }

}
