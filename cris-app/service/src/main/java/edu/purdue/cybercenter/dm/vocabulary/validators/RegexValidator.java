package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.vocabulary.error.ErrorCodes;
import edu.purdue.cybercenter.dm.vocabulary.error.Errors;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorType;
import edu.purdue.cybercenter.dm.vocabulary.error.ModuleCode;
import edu.purdue.cybercenter.dm.xml.vocabulary.Property;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexValidator implements BaseValidator {

    public static final String validatorName = "advanced";

    private String regexp;
    private Term term;

    public RegexValidator(ValidationType.Validator validator, Errors errors, String fieldName, Term term) {
        this.term = term;
        List<Property> validatorProps = validator.getProperty();
        for (Property property : validatorProps) {
            String propName = property.getName();
            if (propName.equalsIgnoreCase("regexp")) {
                regexp = property.getValue();
            } else {
                // Some unknown parameter specified in validator object which Numeric Validator doesn't know about.
                errors.add(ModuleCode.VOCABULARY, ErrorType.WARN, ErrorCodes.ERR_VOCB_VALIDATOR_REGEX_VALIDATOR_UNKNOWN_PROPERTY, fieldName, new Object[]{
                    validatorName, validator.getType()});
            }
        }
    }

    @Override
    public Object validate(Object value, String fieldName, Errors errors) {
        if (regexp == null) {
            errors.setValid(false);
            errors.add(ModuleCode.VOCABULARY, ErrorType.WARN, ErrorCodes.ERR_VOCB_VALIDATOR_REGEX_VALIDATOR_REGEXP_REQUIRED, fieldName, new Object[]{ value });
            return errors;
        }
        try {
            Pattern p = Pattern.compile(regexp);
            Matcher matcher = p.matcher(value.toString());
            if (!matcher.matches()) {
                // Some unknown parameter specified in validator object which Numeric Validator doesn't know about.
                String description = term.getDescription();
                String format = (description != null && !description.isEmpty()) ? description : regexp;
                errors.setValid(false);
                errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_REGEX_VALIDATOR_MISMATCH, fieldName, new Object[]{value, format});
                return errors;
            } else {
                errors.setValid(true);
                return errors;
            }
        } catch (PatternSyntaxException e) {
            errors.setValid(false);
            errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_REGEX_VALIDATOR_INVALID_REGEX, fieldName, new Object[]{value});
            return errors;
        }
    }
}