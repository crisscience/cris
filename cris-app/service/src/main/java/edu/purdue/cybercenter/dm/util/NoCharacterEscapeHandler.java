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
public class NoCharacterEscapeHandler implements CharacterEscapeHandler {

    private NoCharacterEscapeHandler() {
    }

    public static final NoCharacterEscapeHandler theInstance = new NoCharacterEscapeHandler();

    @Override
    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
        out.write(ch, start, length);
    }
}
