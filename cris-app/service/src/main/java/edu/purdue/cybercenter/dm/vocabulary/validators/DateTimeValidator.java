package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.vocabulary.error.ErrorCodes;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorType;
import edu.purdue.cybercenter.dm.vocabulary.error.Errors;
import edu.purdue.cybercenter.dm.vocabulary.error.ModuleCode;
import edu.purdue.cybercenter.dm.xml.vocabulary.Property;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType.Validator;
import java.util.List;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeValidator implements BaseValidator {

    public static final String validatorName = "date-time";

    public enum ValidatorProperty implements EnumProperty {
        FORMAT("format", String.class, FORMAT_DATE, true);

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

    public static final String FORMAT_DATE = "date";
    public static final String FORMAT_TIME = "time";
    public static final String FORMAT_DATETIME = "dateTime";
    public static final String FORMAT_GDAY = "gDay";
    public static final String FORMAT_GMONTH = "gMonth";
    public static final String FORMAT_GMONTHDAY = "gMonthDay";
    public static final String FORMAT_GYEAR = "gYear";
    public static final String FORMAT_GYEARMONTH = "gYearMonth";

    private String format;

    public DateTimeValidator(Validator validator, Errors errors, String fieldName, Term term) {
        List<Property> validatorProps = validator.getProperty();
        for (Property property : validatorProps) {
            String propName = property.getName();
            if (propName.equalsIgnoreCase("format")) {
                format = property.getValue();
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
                String dateStr = (String) value;
                try {
                    if (format != null) {
                        DateTimeFormatter dateTimeFormatter;
                        if (dateStr.contains(".")) {
                            dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        } else {
                            dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
                        }

                        if (format.equalsIgnoreCase(FORMAT_DATE)) {
                            dateTimeFormatter.parseDateTime(dateStr);
                        } else if (format.equalsIgnoreCase(FORMAT_TIME)) {
                            dateTimeFormatter.parseDateTime(dateStr);
                        } else if (format.equalsIgnoreCase(FORMAT_DATETIME)) {
                            dateTimeFormatter.parseDateTime(dateStr);
                        } else if (format.equalsIgnoreCase(FORMAT_GDAY)) {
                            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd");
                            formatter.parseDateTime(dateStr);
                        } else if (format.equalsIgnoreCase(FORMAT_GMONTH)) {
                            DateTimeFormatter formatter = DateTimeFormat.forPattern("MM");
                            formatter.parseDateTime(dateStr);
                        } else if (format.equalsIgnoreCase(FORMAT_GMONTHDAY)) {
                            DateTimeFormatter formatter = DateTimeFormat.forPattern("MM-dd");
                            formatter.parseDateTime(dateStr);
                        } else if (format.equalsIgnoreCase(FORMAT_GYEAR)) {
                            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy");
                            formatter.parseDateTime(dateStr);
                        } else if (format.equalsIgnoreCase(FORMAT_GYEARMONTH)) {
                            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM");
                            formatter.parseDateTime(dateStr);
                        }
                    }
                    errors.setValid(true);
                } catch (IllegalArgumentException ex) {
                    // Some unknown parameter specified in validator object which List Validator doesn't know about.
                    errors.setValid(false);
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_DATETIME_INVALID_DATE, fieldName, new Object[] {
                            dateStr, format });
                }

            }
        }
        return errors;
    }

}
