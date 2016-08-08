/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import flexjson.transformer.AbstractTransformer;
import java.util.UUID;

/**
 *
 * @author agarwa50
 */
public class JsonUuidTransformer extends AbstractTransformer {

    @Override
    public void transform(Object object) {
        if (object != null && !(object instanceof UUID)) {
            throw new RuntimeException("Object is not of type UUID: " + object.toString());
        }

        if (object != null) {
            getContext().writeOpenObject();
            getContext().writeName("$uuid");
            getContext().writeQuoted(object.toString());
            getContext().writeCloseObject();
        } else {
            getContext().write("null");
        }
    }

}
