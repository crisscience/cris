/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author xu222
 */
public class CDataEscapeHandler implements CharacterEscapeHandler {

    public static final CDataEscapeHandler theInstance = new CDataEscapeHandler();

    @Override
    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
        if (isAttVal) {
            out.write(ch, start, length);
        } else {
            out.write("<![CDATA[" + String.valueOf(ch) + "]]>");
        }
    }

}
