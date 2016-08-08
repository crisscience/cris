/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author xu222
 */
public class JsonUuidFactory implements ObjectFactory {

    @Override
    public Object instantiate(ObjectBinder ob, Object o, Type type, Class type1) {
        if (!(o instanceof Map)) {
            throw new RuntimeException("Unable to instantiate a UUID using: " + (o == null ? "null" : o.toString()));
        }

        String $uuid = (String)((Map) o).get("$uuid");
        UUID uuid;
        if ($uuid == null) {
            // object version
            Map<String, Object> map = (Map<String, Object>) o;
            long leastSignificantBits = ((Long) map.get("leastSignificantBits"));
            long mostSignificantBits = ((Long) map.get("mostSignificantBits"));
            uuid = new UUID(mostSignificantBits, leastSignificantBits);
        } else {
            // string version
            uuid = UUID.fromString($uuid);
        }

        return uuid;
    }
}
