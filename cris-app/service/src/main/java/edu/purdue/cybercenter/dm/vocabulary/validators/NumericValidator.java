package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.vocabulary.error.Errors;
import edu.purdue.cybercenter.dm.xml.vocabulary.Property;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType.Validator;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorCodes;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorType;
import edu.purdue.cybercenter.dm.vocabulary.error.ModuleCode;
import edu.purdue.cybercenter.dm.vocabulary.validators.NumericRange.RangeType;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.util.ArrayList;
import java.util.List;

public class NumericValidator implements BaseValidator {

    private static final char RANGE_SEPERATOR_INCLUDE_HEAD = '[';
    private static final char RANGE_SEPERATOR_EXCLUDE_HEAD = '(';
    private static final char RANGE_SEPERATOR_INCLUDE_TAIL = ']';
    private static final char RANGE_SEPERATOR_EXCLUDE_TAIL = ')';

    public static final String validatorName = "numeric";

    public enum ValidatorProperty implements EnumProperty {
        RANGE("range", String.class, "[" + NumericRange.INFINITY_NEGATIVE + ", " + NumericRange.INFINITY_POSITIVE + "]", null);

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

    };

    List<NumericRange> numericalRanges;
    Integer maxPrecision;
    Integer minPrecision;
    Integer base;

    public NumericValidator(Validator validator, Errors errors, String fieldName, Term term) {
        List<Property> validatorProps = validator.getProperty();
        for (Property property : validatorProps) {
            String propName = property.getName();
            if (propName.equalsIgnoreCase("range")) {
                // Range for the value.
                this.numericalRanges = new ArrayList<>();
                setNumericalRanges(property.getValue(), numericalRanges);
            } else {
                // Some unknown parameter specified in validator object which Numeric Validator doesn't know about.
                errors.add(ModuleCode.VOCABULARY, ErrorType.WARN, ErrorCodes.WARN_VOCB_VALIDATOR_ADDITIONAL_PROPERTY, fieldName, new Object[]{
                    validatorName, propName});
            }
        }
    }

    @Override
    public Object validate(Object value, String fieldName, Errors errors) {
        if (value != null && value instanceof String) {
            String strValue = (String) value;
            try {
                // Validate Range
                validateRange(Double.parseDouble(strValue.trim()), errors, fieldName);
            } catch (NumberFormatException excp) {
                // Add error to the list and return false.
                errors.setValid(false);
                errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_NUMERIC_NOT_A_NUMBER, fieldName, new Object[]{value});
                return errors;
            }
        } else if (value != null && (value instanceof Integer || value instanceof Float || value instanceof Double)) {
            validateRange(Double.parseDouble(value.toString()), errors, fieldName);
        }

        return errors;

    }

    private void validateRange(Double value, Errors errors, String fieldName) {
        Boolean valid = false;
        if (numericalRanges != null && numericalRanges.size() > 0) {
            int numberOfRangeChecks = numericalRanges.size();
            for (int i = 0; i < numberOfRangeChecks; i++) {
                NumericRange range = numericalRanges.get(i);
                RangeType rangeMinType = range.getRangeMinType();
                RangeType rangeMaxType = range.getRangeMaxType();

                switch (rangeMinType) {
                    case RANGE_MIN_INCLUDE:
                        if (range.isRangeMinInfinity() || value >= range.getRangeMinValue()) {
                            switch (rangeMaxType) {
                                case RANGE_MAX_INCLUDE:
                                    if (range.isRangeMaxInfinity() || value <= range.getRangeMaxValue()) {
                                        valid = true;
                                        break;
                                    }
                                case RANGE_MAX_EXCLUDE:
                                    if (range.isRangeMaxInfinity() || value < range.getRangeMaxValue()) {
                                        valid = true;
                                        break;
                                    }
                                default:
                                    break;
                            }
                        }
                        break;
                    case RANGE_MIN_EXCLUDE:
                        if (range.isRangeMinInfinity() || value > range.getRangeMinValue()) {
                            switch (rangeMaxType) {
                                case RANGE_MAX_INCLUDE:
                                    if (range.isRangeMaxInfinity() || value <= range.getRangeMaxValue()) {
                                        valid = true;
                                        break;
                                    }
                                case RANGE_MAX_EXCLUDE:
                                    if (range.isRangeMaxInfinity() || value < range.getRangeMaxValue()) {
                                        valid = true;
                                        break;
                                    }
                                default:
                                    break;
                            }
                        }
                        break;
                    default:
                        break;
                }

                if (valid) {
                    break;
                }
            }

            if (!valid) {
                errors.setValid(false);
                errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_NUMERIC_RANGE_FAILED, fieldName, new Object[]{value,
                    numericalRanges.toString()});
            }
        }
    }

    private void setNumericalRanges(String str, List<NumericRange> resultRange) {
        RangeType rangeMinType;
        RangeType rangeMaxType;

        int startIndexInclude = str.indexOf(RANGE_SEPERATOR_INCLUDE_HEAD);
        int startIndexExclude = str.indexOf(RANGE_SEPERATOR_EXCLUDE_HEAD);
        int startIndex;

        // Decide on min
        if (startIndexInclude == -1) {
            startIndex = startIndexExclude;
            rangeMinType = NumericRange.RangeType.RANGE_MIN_EXCLUDE;
        } else if (startIndexExclude == -1) {
            startIndex = startIndexInclude;
            rangeMinType = NumericRange.RangeType.RANGE_MIN_INCLUDE;
        } else if (startIndexInclude < startIndexExclude) {
            startIndex = startIndexInclude;
            rangeMinType = NumericRange.RangeType.RANGE_MIN_INCLUDE;
        } else {
            startIndex = startIndexExclude;
            rangeMinType = NumericRange.RangeType.RANGE_MIN_EXCLUDE;
        }

        // Decide on max
        int endIndexInclude = str.indexOf(RANGE_SEPERATOR_INCLUDE_TAIL);
        int endIndexExclude = str.indexOf(RANGE_SEPERATOR_EXCLUDE_TAIL);
        int endIndex;

        if (endIndexInclude == -1) {
            endIndex = endIndexExclude;
            rangeMaxType = NumericRange.RangeType.RANGE_MAX_EXCLUDE;
        } else if (endIndexExclude == -1) {
            endIndex = endIndexInclude;
            rangeMaxType = NumericRange.RangeType.RANGE_MAX_INCLUDE;
        } else if (endIndexInclude < endIndexExclude) {
            endIndex = endIndexInclude;
            rangeMaxType = NumericRange.RangeType.RANGE_MAX_INCLUDE;
        } else {
            endIndex = endIndexExclude;
            rangeMaxType = NumericRange.RangeType.RANGE_MAX_EXCLUDE;
        }

        // Verify if it's time to come out of the recursion
        if (startIndex == -1 && endIndex == -1) {
            return;
        }

        if (startIndex != -1 && endIndex != -1) {
            resultRange.add(new NumericRange(str.substring(startIndex + 1, endIndex), rangeMinType, rangeMaxType));
            setNumericalRanges(str.substring(endIndex + 1), resultRange);
        } else if (startIndex == -1 && endIndex != -1) {
            resultRange.add(new NumericRange(str.substring(0, endIndex), rangeMinType, rangeMaxType));
            setNumericalRanges(str.substring(endIndex + 1), resultRange);
        } else if (startIndex != -1 && endIndex == -1) {
            resultRange.add(new NumericRange(str.substring(startIndex + 1, str.length()), rangeMinType, rangeMaxType));
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
