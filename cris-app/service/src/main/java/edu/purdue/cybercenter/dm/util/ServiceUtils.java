/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

/**
 *
 * @author xu222
 */
public class ServiceUtils {

    public static String makeFilePath(Integer id, String filename) {
        String destPathPattern = "00000000000000000000".substring(0, 20 - id.toString().length()) + id;
        String s1 = destPathPattern.substring(0, 4);
        String s2 = destPathPattern.substring(4, 8);
        String s3 = destPathPattern.substring(8, 12);
        String s4 = destPathPattern.substring(12, 16);
        String s5 = destPathPattern.substring(16, 20);
        String destPath = s1 + "/" + s2 + "/" + s3 + "/" + s4 + "/";
        String destName = s5 + "_" + filename;
        String dest = destPath + destName;
        return dest;
    }

}
