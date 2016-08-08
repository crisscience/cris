/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author xu222
 */
public class JsonDateFactory implements ObjectFactory {

    @Override
    public Object instantiate(ObjectBinder ob, Object o, Type type, Class type1) {
        if (!(o instanceof Map || o instanceof Long)) {
            throw new RuntimeException("Unable to instantiate a Date using: " + (o == null ? "null" : o.toString()));
        }

        Date date;
        if (o instanceof Long) {
            // object version
            date = new Date((Long) o);
        } else {
            try {
                // string version
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                date = sdf.parse((String) ((Map) o).get("$date"));
            } catch (ParseException ex) {
                throw new RuntimeException("Unable to instantiate a Date, wrong format: " + o.toString(), ex);
            }
        }

        return date;
    }

}
