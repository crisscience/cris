package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.vocabulary.error.ErrorCodes;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorType;
import edu.purdue.cybercenter.dm.vocabulary.error.Errors;
import edu.purdue.cybercenter.dm.vocabulary.error.ModuleCode;

public class BooleanValidator implements BaseValidator {

    public static final String validatorName = "boolean";

    public BooleanValidator() {
    }

    @Override
    public Object validate(Object value, String fieldName, Errors errors) {
        try{
            if ("true".equalsIgnoreCase(value.toString()) || "false".equalsIgnoreCase(value.toString())) {
                return errors;
            } else {
                errors.setValid(false);
                errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_BOOLEAN_VALIDATOR, fieldName, new Object[] { value });
                return errors;
            }
        } catch (Exception e) {
            errors.setValid(false);
            errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_BOOLEAN_VALIDATOR, fieldName, new Object[] { value });
            return errors;
        }
    }
}