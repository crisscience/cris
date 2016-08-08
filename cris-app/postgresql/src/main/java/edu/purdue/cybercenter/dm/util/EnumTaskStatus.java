package edu.purdue.cybercenter.dm.util;

/*
 * This class should be in sync with its domain counter part
 */
public enum EnumTaskStatus {
    NONE (0, "Not defined", ""),
    SCHEDULED (1, "Scheduled", ""),
    READY (2, "Ready", ""),
    RUNNING (3, "Running", ""),
    BLOCKED (4, "Blocked", ""),
    DONE_SUCCEEDED (5, "Done - Succeeded", ""),
    DONE_FAILED (6, "Done - Failed", ""),
    DONE_CANCELLED (7, "Done - Cancelled", "");

    private final Integer index;
    private final String name;
    private final String description;

    EnumTaskStatus(Integer index, String name, String description) {
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
