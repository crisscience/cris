package edu.purdue.cybercenter.dm.vocabulary.error;

public enum ModuleCode {

    VOCABULARY("VOCB"), STORAGE("STOR"), WORKFLOW("WKFL");

    private String value;

    private ModuleCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
