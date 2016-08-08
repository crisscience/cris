package edu.purdue.cybercenter.dm.vocabulary.error;

public class Error {

    private ModuleCode moduleCode;
    private ErrorType errorType;
    private String productCode;
    private String operationErrorCode;
    private String errorMessage;
    private String errorCode;
    private String fieldName;

    /**
     * Constructors
     *
     * @param moduleCode
     * @param operationErrorCode
     * @param args
     */
    public Error(ModuleCode moduleCode, ErrorType errorType, String operationErrorCode, String fieldName, Object[] args) {
        this.productCode = ErrorCodes.PRODUCT_CODE_CRIS;
        this.moduleCode = moduleCode;
        this.errorType = errorType;
        this.operationErrorCode = operationErrorCode;
        this.errorCode = this.productCode + "-" + this.moduleCode.getValue() + "-" + this.operationErrorCode;
        this.errorMessage = Messages.getMessage(this.errorCode, args);
        this.fieldName = fieldName;
    }

    public Error(ModuleCode moduleCode, ErrorType errorType, String operationErrorCode, String fieldName) {
        this(moduleCode, errorType, operationErrorCode, fieldName, null);
    }

    /***
     * Getters for all the params
     *
     * @return
     */

    public String getProductCode() {
        return productCode;
    }

    public ModuleCode getModuleCode() {
        return moduleCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getOperationErrorCode() {
        return operationErrorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getFieldName() {
        return fieldName;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return "[" + errorType + "] " + getErrorCode() + ": " + errorMessage;
    }

}
