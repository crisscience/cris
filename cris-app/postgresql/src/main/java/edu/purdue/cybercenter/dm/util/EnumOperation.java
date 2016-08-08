package edu.purdue.cybercenter.dm.util;

/*
 * This class should be in sync with its domain counter part
 */
public enum EnumOperation {
    NONE (0, "Not defined", ""),
    LIST (1, "Created", ""),
    SHOW (2, "Submitted", ""),
    CREATE (3, "Submitted", ""),
    UPDATE (4, "Submitted", ""),
    DELETE (5, "Submitted", ""),
    IMPORT (6, "Submitted", ""),
    DOWNLOAD (7, "Submitted", ""),
    RUN (8, "Submitted", ""),
    MAINTAIN (9, "Submitted", ""),
    RETIRE (10, "Submitted", ""),
    AVIL (11, "Available", "");

    private final Integer index;
    private final String name;
    private final String description;

    EnumOperation(Integer index, String name, String description) {
        this.index = index;
        this.name = name;
        this.description = description;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
