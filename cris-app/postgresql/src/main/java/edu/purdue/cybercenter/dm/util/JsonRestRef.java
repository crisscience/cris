/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import flexjson.transformer.AbstractTransformer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 *
 * @author xu222
 */
public class JsonRestRef extends AbstractTransformer implements ObjectFactory {

    private static final String UnableToTransformObject = "%s: unable to transform object: %s";
    private static final String UnableToInstantiateValue = "%s: unable to instantiate value: %s";

    private String ctxPath;

    public JsonRestRef() {
        this.ctxPath = "";
    }

    public JsonRestRef(String ctxPath) {
        this.ctxPath = ctxPath;
    }

    public String getCtxPath() {
        return ctxPath;
    }

    public void setCtxPath(String ctxPath) {
        this.ctxPath = ctxPath;
    }

    @Override
    public void transform(Object object) {
        try {
            List<String> path = getContext().getPath().getPath();
            int size = path.size();
            if (object instanceof String && path.get(size - 1).equals("$ref")) {
                getContext().writeQuoted(getCtxPath() + "/" + object);
            } else {
                if (object == null) {
                    getContext().write("null");
                } else {
                    getContext().writeQuoted(object.toString());
                }
            }
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(String.format(UnableToTransformObject, ex.getMessage(), object.toString()));
        } catch (SecurityException ex) {
            throw new RuntimeException(String.format(UnableToTransformObject, ex.getMessage(), object.toString()));
        }
    }

    @Override
    public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
        Object object;
        if (!(value instanceof Map)) {
            object = null;
        } else {
            Map<String, String> map = (Map<String, String>) value;
            if (!map.containsKey("$ref")) {
                object = null;
            } else {
                String restId = (String) map.get("$ref");
                if (ctxPath.length() != 0 && restId.startsWith(ctxPath)) {
                    restId = restId.substring(ctxPath.length());
                }
                String[] arr = restId.split("/");
                if (arr.length != 1 && (arr.length != 3 || !arr[1].equals(targetClass.getSimpleName().toLowerCase() + "s"))) {
                    object =  null;
                } else {
                    try {
                        String id;
                        if (arr.length == 1) {
                            id = restId;
                        } else {
                            id = arr[2];
                        }
                        object = targetClass.getMethod("find" + targetClass.getSimpleName(), Integer.class).invoke(null, (Integer.parseInt(id)));
                    } catch (InvocationTargetException ex) {
                        throw new RuntimeException(String.format(UnableToInstantiateValue, ex.getTargetException().getMessage(), value.toString()));
                    } catch (IllegalArgumentException ex) {
                        throw new RuntimeException(String.format(UnableToInstantiateValue, ex.getMessage(), value.toString()));
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(String.format(UnableToInstantiateValue, ex.getMessage(), value.toString()));
                    } catch (NoSuchMethodException ex) {
                        throw new RuntimeException(String.format(UnableToInstantiateValue, ex.getMessage(), value.toString()));
                    } catch (SecurityException ex) {
                        throw new RuntimeException(String.format(UnableToInstantiateValue, ex.getMessage(), value.toString()));
                    }
                }
            }
        }

        return object;
    }
}
