package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.vocabulary.error.Errors;

public interface BaseValidator {

    public Object validate(Object value, String fieldName, Errors errors);

}
