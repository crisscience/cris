/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import flexjson.transformer.AbstractTransformer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author xu222
 */
public class JsonDateTransformer extends AbstractTransformer {

    @Override
    public void transform(Object object) {
        if (object != null && !(object instanceof Date)) {
            throw new RuntimeException("Object is not of type Date: " + object.toString());
        }

        if (object != null) {
            getContext().writeOpenObject();
            getContext().writeName("$date");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            getContext().writeQuoted(sdf.format((Date) object));
            getContext().writeCloseObject();
        } else {
            getContext().write("null");
        }
    }

}
