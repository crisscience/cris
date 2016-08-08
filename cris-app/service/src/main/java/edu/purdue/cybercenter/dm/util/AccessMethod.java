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
public enum AccessMethod {
    FS      (0, "fs", "Use file system to access files"),
    FTP     (1, "ftp", "Use ftp to access files"),
    HTTP    (2, "http", "Use http to access files"),
    GLOBUS  (3, "globus", "Use globus to access files");

    private final int index;
    private final String name;
    private final String description;

    AccessMethod(int index, String name, String description) {
        this.index = index;
        this.name = name;
        this.description = description;
    }

    public int getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }
}
