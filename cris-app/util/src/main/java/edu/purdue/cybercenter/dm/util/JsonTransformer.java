/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author xu222
 */
public class JsonTransformer {
    private static final String JSON_DIRECTIVE = "$directive";
    private static final String JSON_VALUE = "$value";
    private static final String JSON_DATA = "$data";
    private static final String JSON_CONTEXT = "$context";
    private static final String JSON_PATH = "$path";
    private static final String JSON_OPTION = "$option";
    private static final String JSON_DIRECTIVE_MERGE = "$merge";

    public static String transformJson(String jsonIn) {
        if (StringUtils.isBlank(jsonIn)) {
            return "";
        }
        String trimmedJsonIn = jsonIn.trim();
        Map<String, Object> jsonObject;
        if (trimmedJsonIn.startsWith("{")) {
            try {
                jsonObject = Helper.deserialize(jsonIn, Map.class);
            } catch (Exception ex) {
                return jsonIn;
            }
        } else {
            return jsonIn;
        }
        Object directive = jsonObject.get(JsonTransformer.JSON_DIRECTIVE);
        if (directive == null) {
            return jsonIn;
        }
        Object data = jsonObject.get(JsonTransformer.JSON_DATA);
        if (data == null) {
            // return emoty string if there's no data
            return "";
        }
        Map<String, Object> context = (Map<String, Object>) jsonObject.get(JsonTransformer.JSON_CONTEXT);
        if (directive instanceof Map) {
            data = applyDirective((Map<String, Object>) directive, data, context);
        } else if (directive instanceof List) {
            List<Map<String, Object>> directives = (List) directive;
            for (Map<String, Object> d : directives) {
                data = applyDirective(d, data, context);
            }
        } else {
            throw new RuntimeException("Invalid directive oin json: " + jsonIn);
        }
        String jsonOut = Helper.deepSerialize(data);
        return jsonOut;
    }

    private static Object applyDirective(Map<String, Object> directive, Object data, Map<String, Object> context) {
        if (directive == null || directive.isEmpty()) {
            throw new RuntimeException("empty directive");
        }
        if (directive.size() != 1) {
            throw new RuntimeException("invalid directive format: " + Helper.deepSerialize(directive));
        }
        String name = directive.keySet().iterator().next();
        Map<String, Object> definition = (Map<String, Object>) directive.get(name);
        switch (name) {
            case JsonTransformer.JSON_DIRECTIVE_MERGE:
                data = jsonMerge(definition, data, context);
                break;
            default:
        }
        return data;
    }

    private static Object jsonMerge(Map<String, Object> definition, Object data, Map<String, Object> context) {
        String path = (String) definition.get(JsonTransformer.JSON_PATH);
        //Object value = definition.get(JsonTransformer.JSON_VALUE);
        //Map<String, Object> option = (Map<String, Object>) definition.get(JsonTransformer.JSON_OPTION);
        boolean isRoot = StringUtils.isBlank(path);
        String subKey = null;
        Object subData = data;
        Object parentData = data;
        if (!isRoot) {
            String[] parts = path.split("\\.");
            if (parts.length != 0) {
                for (String part : parts) {
                    parentData = subData;
                    subKey = part;
                    subData = ((Map<String, Object>) parentData).get(subKey);
                }
            } else {
                parentData = data;
                subKey = path;
                subData = ((Map<String, Object>) parentData).get(subKey);
            }
        }
        if (!(subData instanceof List)) {
            return data;
        }
        Map<String, Object> mergedObject = mergeObjects(subData);
        Object result;
        if (!isRoot) {
            ((Map<String, Object>) parentData).put(subKey, mergedObject);
            result = data;
        } else {
            result = mergedObject;
        }
        return result;
    }

    private static Map<String, Object> mergeObjects(Object object) {
        if (!(object instanceof Map) && !(object instanceof List)) {
            throw new RuntimeException("mergeObjects: object must be either map or list: " + object.toString());
        }
        if (object instanceof Map) {
            return (Map) object;
        }
        List<Map> objects = (List) object;
        Map<String, Object> mergedObject = new HashMap<>();
        for (Map o : objects) {
            mergeMaps(mergedObject, o);
        }
        return mergedObject;
    }

    private static Map<String, Object> mergeMaps(Map<String, Object> map1, Map<String, Object> map2) {
        if (map2 != null) {
            for (Entry<String, Object> entry2 : map2.entrySet()) {
                String key2 = entry2.getKey();
                Object value1 = map1.get(key2);
                Object value2 = entry2.getValue();
                if (value1 == null) {
                    map1.put(key2, value2);
                } else if (value2 == null) {
                    if (value1 instanceof Map) {
                        // ignore merge null into composite type
                    } else {
                        // merge null into primitive type
                        map1.put(key2, value2);
                    }
                } else if (!(value1 instanceof Map) || !(value2 instanceof Map)) {
                    map1.put(key2, value2);
                } else {
                    mergeMaps((Map) value1, (Map) value2);
                }
            }
        }
        return map1;
    }

}
