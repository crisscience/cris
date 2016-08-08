package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.vocabulary.error.Errors;
import edu.purdue.cybercenter.dm.xml.vocabulary.Property;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType.Validator;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorCodes;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorType;
import edu.purdue.cybercenter.dm.vocabulary.error.ModuleCode;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancedValidator implements BaseValidator {

    public static final String validatorName = "advanced";

    private static final String PROP_REGEXP = "regexp";

    public enum ValidatorProperty implements EnumProperty {
        REGEXP(PROP_REGEXP, String.class, null, true);

        private final String name;
        private final Class type;
        private final Object value;
        private final Boolean required;

        private ValidatorProperty(String name, Class type, Object value, Boolean required) {
            this.name = name;
            this.type = type;
            this.value = value;
            this.required = required;
        }

        @Override
        public String getId() {
            return null;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public Class getType() {
            return type;
        }
        @Override
        public Object getValue() {
            return value;
        }
        @Override
        public Boolean isRequired() {
            return required;
        }
    }

    private String regExp;

    public AdvancedValidator(Validator validator, Errors errors, String fieldName) {
        List<Property> validatorProps = validator.getProperty();
        for (Property property : validatorProps) {
            String propName = property.getName();
            if (propName.equalsIgnoreCase(PROP_REGEXP)) {
                regExp = property.getValue();
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
                if (regExp != null) {
                    Pattern p = Pattern.compile(regExp);
                    Matcher matcher = p.matcher(valueStr);
                    if (!matcher.matches()) {
                        errors.setValid(false);
                        errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_ADVANCED_INVALID_VALUE, fieldName,
                                new Object[] { value, regExp });
                    }
                }
            }
        }
        return errors;
    }

}
