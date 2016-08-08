package edu.purdue.cybercenter.dm.util;

/*
 * This class should be in sync with its domain counter part
 */
public enum EnumJobStatus {
    NONE (0, "Not defined", ""),
    CREATED (1, "Created", ""),
    SUBMITTED (2, "Submitted", ""),
    STARTED (3, "Started", ""),
    FINISHED (4, "Finished", ""),
    CANCELED (5, "Canceled", ""),
    CLOSED (6, "Closed", "");

    private final Integer index;
    private final String name;
    private final String description;

    EnumJobStatus(Integer index, String name, String description) {
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
