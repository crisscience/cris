package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.vocabulary.error.ErrorCodes;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorType;
import edu.purdue.cybercenter.dm.vocabulary.error.Errors;
import edu.purdue.cybercenter.dm.vocabulary.error.ModuleCode;
import edu.purdue.cybercenter.dm.xml.vocabulary.Property;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileValidator implements BaseValidator {

    public static final String validatorName = "file";

    public enum ValidatorProperty implements EnumProperty {
        MULTIPLE("multiple", Boolean.class, false, true),
        GLOBUS("globus", Boolean.class, false, null);

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

    private static final String RegExpStorageFile = "StorageFile:[0-9]+";

    private boolean isMultiple = false;
    private boolean isGlobus = false;

    public FileValidator(ValidationType.Validator validator, Errors errors, String fieldName, Term term) {
        List<Property> validatorProps = validator.getProperty();
        for (Property property : validatorProps) {
            String propName = property.getName();
            String propValue = (property.getId() != null && !property.getId().isEmpty()) ? property.getId() : property.getValue();
            if (propName.equalsIgnoreCase("multiple")) {
                this.isMultiple = Boolean.valueOf(propValue); // Min allowed Precision
            } else if (propName.equalsIgnoreCase("globus")) {
                this.isGlobus = Boolean.valueOf(propValue); // Min allowed Precision
            } else {
                // Some unknown parameter specified in validator object which List Validator doesn't know about.
                errors.add(ModuleCode.VOCABULARY, ErrorType.WARN, ErrorCodes.WARN_VOCB_VALIDATOR_ADDITIONAL_PROPERTY, fieldName, new Object[]{
                    validatorName, propName});
            }
        }
    }

    @Override
    public Object validate(Object value, String fieldName, Errors errors) {
        try {
            Pattern p = Pattern.compile(RegExpStorageFile);

            if (!this.isMultiple && (value instanceof String)) {
                String fileName = value.toString();
                Matcher matcher = p.matcher(fileName);
                if (!matcher.matches()) {
                    errors.setValid(false);
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_FILE_VALIDATOR_INVALID_FILENAME, fieldName, new Object[]{value});
                } else {
                    errors.setValid(true);
                }
            } else if (this.isMultiple && (value instanceof List)) {
                List<String> lstValues = (List<String>) value;
                boolean valid = true;
                for (String file : lstValues) {
                    Matcher matcher = p.matcher(file);
                    if (!matcher.matches()) {
                        valid = false;
                        errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_FILE_VALIDATOR_INVALID_FILENAME, fieldName, new Object[]{value});
                    }
                }
                errors.setValid(valid);
            } else {
                errors.setValid(false);
                if (this.isMultiple) {
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALUE_LIST_EXPECTED, fieldName, new Object[]{value});
                } else {
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALUE_STRING_EXPECTED, fieldName, new Object[]{value});
                }
            }
        } catch (Exception e) {
            errors.setValid(false);
            errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_FILE_VALIDATOR_INVALID_FILENAME, fieldName, new Object[]{value});
        }

        return errors;
    }
}
