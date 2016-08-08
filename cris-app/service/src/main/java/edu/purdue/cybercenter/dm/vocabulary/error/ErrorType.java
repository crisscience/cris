package edu.purdue.cybercenter.dm.vocabulary.error;

public enum ErrorType {

    INFO("INFO"), WARN("WARN"), ERROR("ERROR");

    private String value;

    private ErrorType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
