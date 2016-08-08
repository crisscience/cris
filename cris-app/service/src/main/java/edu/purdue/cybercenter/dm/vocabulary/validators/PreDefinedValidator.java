package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.vocabulary.error.Errors;
import edu.purdue.cybercenter.dm.xml.vocabulary.Property;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType.Validator;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorCodes;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorType;
import edu.purdue.cybercenter.dm.vocabulary.error.ModuleCode;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreDefinedValidator implements BaseValidator {

    public static final String validatorName = "pre-defined";

    public static String ALIAS_ZIP = "zip";
    public static String ALIAS_EMAIL = "email";
    public static String ALIAS_PHONE = "phone";

    public static String REGEXP_ZIP = "[0-9]{5}-[0-9]{4}";
    public static String REGEXP_EMAIL = "[0-9_a-zA-Z]*@[a-zA-Z]*.[a-zA-Z]{3}";
    public static String REGEXP_PHONE = "[0-9]{3}-[0-9]{3}-[0-9]{3}";

    private String alias;

    public PreDefinedValidator(Validator validator, Errors errors, String fieldName, Term term) {
        List<Property> validatorProps = validator.getProperty();
        for (Property property : validatorProps) {
            String propName = property.getName();
            if (propName.equalsIgnoreCase("alias")) {
                String val = property.getValue();
                if (val != null && (val.equalsIgnoreCase(ALIAS_ZIP) || val.equalsIgnoreCase(ALIAS_EMAIL) || val.equalsIgnoreCase(ALIAS_PHONE))) {
                    alias = val;
                } else {
                    // Some unknown parameter specified in validator object which List Validator doesn't know about.
                    errors.setValid(false);
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_PREDEFINED_NON_EXISTENT, fieldName,
                            new Object[]{propName});
                }
            } else {
                // Some unknown parameter specified in validator object which List Validator doesn't know about.
                errors.add(ModuleCode.VOCABULARY, ErrorType.WARN, ErrorCodes.WARN_VOCB_VALIDATOR_ADDITIONAL_PROPERTY, fieldName, new Object[]{
                    validatorName, propName});
            }
        }
    }

    @Override
    public Object validate(Object value, String fieldName, Errors errors) {
        if (value != null) {
            if (value instanceof String) {
                String valueStr = (String) value;
                if (alias != null) {                            // Constructor makes sure that alias is null for non-existing validators.
                    Pattern p = null;
                    if (alias.equals(ALIAS_ZIP)) {
                        p = Pattern.compile(REGEXP_ZIP);
                    } else if (alias.equals(ALIAS_EMAIL)) {
                        p = Pattern.compile(REGEXP_EMAIL);
                    } else if (alias.equals(ALIAS_PHONE)) {
                        p = Pattern.compile(REGEXP_PHONE);
                    }

                    if (p != null) {
                        Matcher matcher = p.matcher(valueStr);
                        if (!matcher.matches()) {
                            // Some unknown parameter specified in validator object which Numeric Validator doesn't know about.
                            errors.setValid(false);
                            errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_PREDEFINED_INVALID_VALUE, fieldName,
                                    new Object[]{value, alias});
                            return errors;
                        }
                    }
                } else {
                    return errors;
                }

            }
        }
        return errors;
    }

}
