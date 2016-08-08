/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

/**
 *
 * @author xu222
 */
public enum EnumDatasetState {

    Sandboxed(0, "Sandboxed", "A dataset in this state should not be used by any workflow"),
    Operational(1, "Operational", "A dataset is in noraml operation mode"),
    Archived(2, "Archived", "A dataset in this state cannot be modified by a workflow"),
    Deprecated(3, "Deprecated", "The dataset has been marked as unusable"),
    Temporary(4, "Temporary", "The dataset has been marked as temporary");

    private final Integer index;
    private final String name;
    private final String description;

    EnumDatasetState(Integer index, String name, String description) {
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
