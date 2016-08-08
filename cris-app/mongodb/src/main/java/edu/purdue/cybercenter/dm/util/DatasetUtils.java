/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.purdue.cybercenter.dm.util;

import com.mongodb.util.JSON;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author xu222
 */
public class DatasetUtils {

    private static final String CollectionPrefix = "dataset_";
    private static final String AuditSuffix = "_aud";

    public static String serialize(Object object) {
        return JSON.serialize(object);
    }

    public static Object deserialize(String json) {
        return JSON.parse(json);
    }

    public static String wrapWithTemplate(String templateUuid, String json) {
        // assume "json" is a valid json
        return "{\"" + templateUuid + "\": " + json + "}";
    }

    public static Map<String, Object> wrapWithTemplate(String templateUuid, Object object) {
        // assume "object" is either a map or list
        if (!(object instanceof Map) && !(object instanceof List) ) {
            throw new RuntimeException("Object must be either a map or list: " + object.toString());
        }

        Map<String, Object> wrapper = new HashMap<String, Object>();
        wrapper.put(templateUuid, object);

        return wrapper;
    }

    public static List<Integer> convertEnumStatesToIntegerStates(List<EnumDatasetState> states) {
        List<Integer> integerStates = new ArrayList<>();
        for (EnumDatasetState state : states) {
            integerStates.add(state.getIndex());
        }
        return integerStates;
    }

    public static Set<String> uuidsToCollectionNames(Set<String> uuids) {
        Set<String> collectionNames = new HashSet<>();
        for (String uuid : uuids) {
            collectionNames.add(DatasetUtils.makeCollectionName(UUID.fromString(uuid), false));
        }
        return collectionNames;
    }

    public static List extractField(String path, List<Map> objects) {
        return extractField(path, objects, false);
    }

    public static List extractField(String path, List<Map> objects, Boolean distinct) {
        if (distinct == null) {
            distinct = false;
        }

        List returnObjects = new ArrayList();
        for (Map object : objects) {
            Object returnObject = extractField(path, object);
            if (distinct) {
                if (!returnObjects.contains(returnObject)) {
                    returnObjects.add(returnObject);
                }
            } else {
                returnObjects.add(returnObject);
            }
        }
        return returnObjects;
    }

    public static Object extractField(String path, Map object) {
        if (path == null || path.isEmpty() || object == null) {
            return object;
        }

        Object returnObject = object;
        String[] fields = path.split("\\.");
        for (String field : fields) {
            if (returnObject instanceof Map) {
                returnObject = ((Map<String, Object>) returnObject).get(field);
            } else {
                returnObject = null;
                break;
            }
        }

        return returnObject;
    }

    public static Object getOneOrNothing(List list) {
        if (list != null && list.size() == 1) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public static Object getOne(List list) {
        if (list != null && list.size() >= 1) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public static String makeCollectionName(String sUuid, boolean isAudit) {
        String collectionName;
        if (sUuid != null) {
            if (isAudit) {
                collectionName = CollectionPrefix + sUuid.replace("-", "_")  + AuditSuffix;
            } else {
                collectionName = CollectionPrefix + sUuid.replace("-", "_");
            }
        } else {
            throw new RuntimeException("Cannot make a collection name: uuid is null");
        }
        return collectionName;
    }

    public static String makeCollectionName(UUID uuid, boolean isAudit) {
        String collectionName;
        if (uuid != null) {
            collectionName = makeCollectionName(uuid.toString(), isAudit);
        } else {
            throw new RuntimeException("Cannot make a collection name: uuid is null");
        }
        return collectionName;
    }

    public static UUID collectionNameToUuid(String collectionName) {
        UUID uuid;
        if (collectionName.startsWith(CollectionPrefix)) {
            String sUuid = collectionName.substring(CollectionPrefix.length());
            if (!sUuid.endsWith(AuditSuffix)) {
                uuid = UUID.fromString(sUuid.replace("_", "-"));
            } else {
                // exclude audit tables
                uuid = null;
            }
        } else {
            uuid = null;
        }
        return uuid;
    }
}
