package edu.purdue.cybercenter.dm.util;

/*
 * This class should be in sync with its domain counter part
 */
public enum EnumAssetType {
    Resource (0, "Resource", ""),
    Service (1, "Service", ""),
    Storage (2, "Storage", ""),
    StorageFile (3, "Storage File", ""),
    ComputationalNode (4, "Computational Node", ""),
    Vocabulary (5, "Vocabulary", ""),
    Term (6, "Term", ""),
    Project (7, "Project", ""),
    Experiment (8, "Experiment", ""),
    Dataset (9, "Dataset", ""),
    Content (10, "Content", ""),
    Tool (11, "Tool", ""),
    Workflow (12, "Workflow", ""),
    Report (13, "Report", ""),
    SmallObject (14, "Small Object", ""),
    Shortcut (15, "Shortcut", ""),
    Tile (16, "Tile", "");

    private final Integer index;
    private final String name;
    private final String description;

    EnumAssetType(Integer index, String name, String description) {
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
