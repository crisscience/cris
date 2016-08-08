package edu.purdue.cybercenter.dm.vocabulary.error;

import java.util.ArrayList;
import java.util.List;

public class Errors {

    private boolean valid = true;

    private final List<Error> errorList = new ArrayList<>();

    public void add(ModuleCode moduleCode, ErrorType errorType, String operationErrorCode, String fieldName, Object[] args) {
        errorList.add(new Error(moduleCode, errorType, operationErrorCode, fieldName, args));
    }

    public void add(ModuleCode moduleCode, ErrorType errorType, String operationErrorCode, String fieldName) {
        errorList.add(new Error(moduleCode, errorType, operationErrorCode, fieldName));
    }

    public List<Error> getErrorList() {
        return errorList;
    }

    public void setValid(boolean valid) {
        //can never be overwritten to true
        this.valid = valid && this.valid;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        for (Error error : errorList) {
            sb.append(error.toString()).append("\n");
        }
        return sb.toString();
    }

}
