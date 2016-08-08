package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.vocabulary.error.Errors;
import edu.purdue.cybercenter.dm.xml.vocabulary.Property;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType.Validator;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorCodes;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorType;
import edu.purdue.cybercenter.dm.vocabulary.error.ModuleCode;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextValidator implements BaseValidator {

    public static final String validatorName = "text";

    private static final String TYPE_ALPHANUMERIC = "alphanumeric";
    private static final String TYPE_NUMERIC = "numeric";
    private static final String TYPE_ALPHA = "alpha";
    private static final String TYPE_PRINTABLE = "printable";

    private static final String PROP_UIVERTICALLINES = "ui-vertical-lines";
    private static final String PROP_LENGTH = "length";
    private static final String PROP_TYPE = "type";

    public enum ValidatorProperty implements EnumProperty {
        TYPE(PROP_TYPE, String.class, TYPE_PRINTABLE, true),
        LENGTH(PROP_LENGTH, Integer.class, null, null),
        UIVERTICALLINES(PROP_UIVERTICALLINES, Integer.class, 1, true);

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

    Integer verticalLines;
    String textType;
    Integer length;

    public TextValidator(Validator validator, Errors errors, String fieldName, Term term) {
        List<Property> validatorProps = validator.getProperty();
        for (Iterator<Property> propIter = validatorProps.iterator(); propIter.hasNext();) {
            try {
                Property property = propIter.next();
                String propName = property.getName();
                if (propName.equalsIgnoreCase(PROP_TYPE)) {
                    // Range for the value.
                    textType = property.getValue();
                } else if (propName.equalsIgnoreCase(PROP_LENGTH)) {
                    // Max allowed Precision
                    length = safeConvertStrToInt(property.getValue());
                } else if (propName.equals(PROP_UIVERTICALLINES)) {
                    verticalLines = Integer.parseInt(property.getValue());
                } else {
                    // Some unknown parameter specified in validator object which Numeric Validator doesn't know about.
                    errors.add(ModuleCode.VOCABULARY, ErrorType.WARN, ErrorCodes.WARN_VOCB_VALIDATOR_ADDITIONAL_PROPERTY, fieldName, new Object[]{
                        validatorName, validator.getType()});
                }
            } catch (NumberFormatException e) {
                errors.setValid(false);
                errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_TEXT_NON_NUMERIC_UI_LINES, fieldName, new Object[]{
                    validatorName, validator.getType()});
            }
        }
    }

    @Override
    public Object validate(Object value, String fieldName, Errors errors) {
        if (value != null && value instanceof String) {
            validateType((String) value, errors, fieldName);
            validateLength((String) value, errors, fieldName);
        }

        return errors;
    }

    private void validateType(String value, Errors errors, String fieldName) {
        if (textType != null && textType.equalsIgnoreCase(TYPE_ALPHANUMERIC)) {
            if (value != null && !value.trim().equals("")) {
                Pattern p = Pattern.compile("[A-Za-z0-9_]*");
                Matcher matcher = p.matcher(value);
                if (!matcher.matches()) {
                    // Some unknown parameter specified in validator object which Numeric Validator doesn't know about.
                    errors.setValid(false);
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_TEXT_NON_ALPHANUMERIC_CHAR, fieldName, new Object[]{value});
                }
            }
        } else if (textType != null && textType.equalsIgnoreCase(TYPE_ALPHA)) {
            if (value != null && !value.trim().equals("")) {
                Pattern p = Pattern.compile("[A-Za-z]*");
                Matcher matcher = p.matcher(value);
                if (!matcher.matches()) {
                    // Some unknown parameter specified in validator object which Numeric Validator doesn't know about.
                    errors.setValid(false);
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_TEXT_NON_ALPHA_CHAR, fieldName, new Object[]{value});
                }
            }
        } else if (textType != null && textType.equalsIgnoreCase(TYPE_NUMERIC)) {
            if (value != null && !value.trim().equals("")) {
                Pattern p = Pattern.compile("[0-9]*");
                Matcher matcher = p.matcher(value);
                if (!matcher.matches()) {
                    // Some unknown parameter specified in validator object which Numeric Validator doesn't know about.
                    errors.setValid(false);
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_TEXT_NON_NUMERIC_CHAR, fieldName, new Object[]{value});
                }
            }
        } else {
            // default and printable
            if (value != null && !value.trim().equals("")) {
                Pattern p = Pattern.compile("[ -~\\s]*");
                Matcher matcher = p.matcher(value);
                if (!matcher.matches()) {
                    // Some unknown parameter specified in validator object which Numeric Validator doesn't know about.
                    errors.setValid(false);
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_TEXT_NON_PRINTABLE_CHAR, fieldName, new Object[]{value});
                }
            }
        }
    }

    private void validateLength(String value, Errors errors, String fieldName) {
        if (value != null && length != null) {
            int stringLength = value.length();
            if (stringLength > length) {
                // Some unknown parameter specified in validator object which Numeric Validator doesn't know about.
                errors.setValid(false);
                errors.add(ModuleCode.VOCABULARY, ErrorType.WARN, ErrorCodes.ERR_VOCB_VALIDATOR_TEXT_INVALID_LENGTH, fieldName,
                        new Object[]{value, length});
            }
        }
    }

    //TODO: consider use parseInt() directly
    public final Integer safeConvertStrToInt(String intStr) {
        Integer number = null;
        try {
            number = Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            // ignore;
        }

        return number;
    }

}
