package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.vocabulary.error.Errors;
import edu.purdue.cybercenter.dm.xml.vocabulary.Property;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType.Validator;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorCodes;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorType;
import edu.purdue.cybercenter.dm.vocabulary.error.ModuleCode;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.util.ArrayList;
import java.util.List;

public class ListValidator implements BaseValidator {

    public static final String validatorName = "list";

    /**
     *
     */
    public enum ValidatorProperty implements EnumProperty {
        ITEM("item", String.class, null, true),
        ISMULTISELECT("isMultiSelect", Boolean.class, false, null);

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

    private final List<String> items = new ArrayList<>();
    private Boolean isMultiSelect = false;

    public ListValidator(Validator validator, Errors errors, String fieldName, Term term) {
        List<Property> validatorProps = validator.getProperty();
        for (Property property : validatorProps) {
            String propName = property.getName();
            String propValue = (property.getId() != null && !property.getId().isEmpty()) ? property.getId() : property.getValue();
            if (propName.equalsIgnoreCase("item")) {
                this.items.add(propValue);
            } else if (propName.equalsIgnoreCase("isMultiSelect")) {
                this.isMultiSelect = Boolean.valueOf(propValue); // Min allowed Precision
            } else {
                // Some unknown parameter specified in validator object which List Validator doesn't know about.
                errors.add(ModuleCode.VOCABULARY, ErrorType.WARN, ErrorCodes.WARN_VOCB_VALIDATOR_ADDITIONAL_PROPERTY, fieldName, new Object[]{
                    validatorName, propName});
            }
        }
    }

    @Override
    public Object validate(Object value, String fieldName, Errors errors) {
        if (items.isEmpty()) {
            errors.setValid(false);
            errors.add(ModuleCode.VOCABULARY, ErrorType.WARN, ErrorCodes.ERR_VOCB_VALIDATOR_LIST_NO_ITEMS,
                    fieldName, new Object[] { fieldName });
            return errors;
        }

        if (value != null) {
            if (value instanceof String) {
                if (!this.items.contains((String) value)) {
                    errors.setValid(false);
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_LIST_INVALID_CONTENTS,
                            fieldName, new Object[] { value, this.items });
                    return errors;
                }
            } else if (value instanceof List) {
                List<String> lstValues = (List<String>) value;
                if (isMultiSelect) {
                    if (!this.items.containsAll(lstValues)){
                        errors.setValid(false);
                        errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_LIST_INVALID_CONTENTS,
                                fieldName, new Object[] { value, this.items });
                        return errors;
                    }
                } else {
                    errors.setValid(false);
                    errors.add(ModuleCode.VOCABULARY, ErrorType.WARN, ErrorCodes.WARN_VOCB_VALIDATOR_ADDITIONAL_VALUE,
                            fieldName, new Object[]{fieldName});
                    return errors;
                }
            }
        }

        return errors;
    }

}
