package edu.purdue.cybercenter.dm.util;

/*
 * This class should be in sync with its domain counter part
 */
public enum EnumAssetStatus {
    Deprecated(0, "Deprecated", "An asset in this state indicate some problems"),
    Operational(1, "Operational", "An asset is in noraml operation mode");

    private final Integer index;
    private final String name;
    private final String description;

    EnumAssetStatus(Integer index, String name, String description) {
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
