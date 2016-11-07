package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.vocabulary.error.Error;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorCodes;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorType;
import edu.purdue.cybercenter.dm.vocabulary.error.Errors;
import edu.purdue.cybercenter.dm.vocabulary.error.ModuleCode;
import edu.purdue.cybercenter.dm.xml.vocabulary.AttachTo;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 *
 * @author rangars
 */

@Component
public class VocabularyValidatorImpl implements VocabularyValidator {

    public static final String ROOT_LEVEL_ERRORS_KEY = "";

    private static final ScriptEngine engine;
    static {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName("nashorn");
    }

    //TODO: Fix valid flag statuses.

    @Override
    public Object validate(Term term, Object value) {
        return validate(term, value, null, value, true);
    }

    private <T> T evaluateJson(String expression, Map<String, Object> scope, Class<T> clazz) throws ScriptException {
            ScriptContext scriptContext = new SimpleScriptContext();
            Bindings engineScope = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);

            if (scope != null) {
                engineScope.putAll(scope);
            }

            T result = (T) engine.eval(expression, engineScope);

            return result;
    }

    private boolean isShow(String showExpression, Map<String, Object> scope) {
        boolean isShow = true;
        if (StringUtils.isNotEmpty(showExpression)) {
            try {
                isShow = evaluateJson(showExpression, scope, Boolean.class);
            } catch (ScriptException ex) {
                isShow = false;
            }
        }

        return isShow;
    }

    private boolean isRequired(String requiredExpression, Map<String, Object> scope) {
        boolean isRequired = false;
        if (StringUtils.isNotEmpty(requiredExpression)) {
            try {
                isRequired = evaluateJson(requiredExpression, scope, Boolean.class);
            } catch (ScriptException ex) {
                isRequired = false;
            }
        }

        return isRequired;
    }

    private Object getObjectOnPath(String path, Map<String, Object> map) {
        if (StringUtils.isEmpty(path) || map == null || map.isEmpty()) {
            return map;
        }

        String[] aliases = path.split(".");
        for (String alias : aliases) {
            map = (Map<String, Object>) map.get(alias);
        }

        return map;
    }

    private void putToPath(String key, Object value, String path, Map<String, Object> map) {
        map = (Map<String, Object>) getObjectOnPath(path, map);
        if (map != null) {
            map.put(key, value);
        }
    }

    private void removeFromPath(String key, String path, Map<String, Object> map) {
        map = (Map<String, Object>) getObjectOnPath(path, map);
        if (map != null) {
            map.remove(key);
        }
    }

    private Object validate(Term term, Object value, String path, Object root, boolean show) {
        if (term == null) {
            throw new RuntimeException("Cannot validate the value using a null term definition: " + value != null ? value.toString() : "null");
        }

        boolean isTermList = (term.isList() != null) && term.isList();
        boolean isValueList = (value != null && (value instanceof List || value instanceof Object[]));

        String sShowExpression = term.getShowExpression();
        String sRequiredExpression = term.getRequiredExpression();
        Map<String, Object> scope;
        if (root instanceof Map) {
            scope = (Map<String, Object>) root;
        } else {
            scope = null;
        }
        boolean isShow = show && isShow(sShowExpression, scope);
        boolean isRequired = (term.isRequired() != null ? term.isRequired() : isRequired(sRequiredExpression, scope));

        HashMap<String, Object> wrappedMap = new HashMap<>();
        String alias = term.getAlias() == null ? term.getName() : term.getAlias();
        if (isTermList) {
            if (isValueList || value == null) {
                if (value == null) {
                    // it treats null value as empty array
                    value = new ArrayList();
                }

                if (isRequired && isShow && ((List) value).isEmpty()) {
                    Errors errors = new Errors();
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_TEMPLATE_MISSING_REQUIRED_TERM, path, new Object[]{value.getClass().getName()});
                    errors.setValid(false);
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put(ROOT_LEVEL_ERRORS_KEY, errors);
                    wrappedMap.put(alias, errorMap);
                } else {
                    // validate current term
                    List<Object> errorList = new ArrayList<>();
                    term.setList(false);
                    // create _alias key
                    String key = "_" + alias;
                    for (Object v : (List) value) {
                        // put v to path.key
                        putToPath(key, v, path, scope);
                        Object error = validate(term, v, path, root, isShow);
                        errorList.add(((Map) error).get(alias));
                    }
                    // remove path.key
                    removeFromPath(key, path, scope);
                    term.setList(true);
                    wrappedMap.put(alias, errorList);
                }
            } else {
                // Term is a list but value is not
                Errors errors = new Errors();
                errors.setValid(false);
                String className = value.getClass().getName();
                HashMap<String, Object> errorMap = new HashMap<>();
                errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALUE_MAP_EXPECTED, path, new Object[]{className});
                errorMap.put(ROOT_LEVEL_ERRORS_KEY, errors);
                wrappedMap.put(alias, errorMap);
            }
        } else {
            // validate nested terms
            if (path == null) {
                path = "";
            } else {
                if (path.isEmpty()) {
                    path = alias;
                } else {
                    path = path + "." + alias;
                }
            }

            HashMap<String, Object> errorMap = new HashMap<>();
            List<Term> nestedTerms = term.getTerm();
            if (!nestedTerms.isEmpty() && value != null) {
                // process nested terms
                if (value instanceof Map) {
                    // value is a map
                    for (Term t : term.getTerm()) {
                        String tAlias = t.getAlias() == null ? t.getName() : t.getAlias();
                        errorMap.putAll((Map<? extends String,?>) validate(t, ((Map<String, Object>) value).get(tAlias), path, root, isShow));
                    }
                    for (AttachTo a : term.getAttachTo()) {
                        String tAlias = a.getUseAlias();
                        errorMap.putAll((Map<? extends String,?>) validateAttachTo(a, ((Map<String, Object>) value).get(tAlias), path, value, isShow));
                    }
                } else {
                    // value is not a map
                    Errors errors = new Errors();
                    errors.setValid(false);
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALUE_MAP_EXPECTED, path, new Object[]{value.getClass().getName()});
                    errorMap.put(alias, errors);
                }
            }

            // validate the current term
            Object errorObject;
            if (isRequired && isShow) {
                // Value Validation becomes necessary
                if (value != null) {
                    errorObject = validateValue(term, value, path);
                } else {
                    Errors errors = new Errors();
                    errors.setValid(false);
                    errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_TEMPLATE_MISSING_REQUIRED_TERM, path, new Object[]{alias});
                    errorObject = errors;
                }
            } else {
                if (value != null && !value.equals("")) {
                    // Validate value
                    errorObject = validateValue(term, value, path);
                } else {
                    Errors errors = new Errors();
                    errors.setValid(true);
                    errorObject = errors;
                }
            }
            errorMap.put(ROOT_LEVEL_ERRORS_KEY, errorObject);
            wrappedMap.put(alias, errorMap);
        }

        return wrappedMap;
    }

    private Object validateAttachTo(AttachTo attachTo, Object value, String path, Object parent, boolean show) {
        HashMap<String, Object> errorMap = new HashMap<>();

        String sShowExpression = attachTo.getShowExpression();
        String sRequiredExpression = attachTo.getRequiredExpression();
        boolean isShow = show && isShow(sShowExpression, (Map<String, Object>) parent);
        boolean isRequired = (attachTo.isRequired() != null ? attachTo.isRequired() : isRequired(sRequiredExpression, (Map<String, Object>) parent));

        if (isRequired && isShow) {
            String alias = attachTo.getUseAlias();
            Boolean isList = attachTo.isList();
            if (value == null) {
                // missing value (either list or non-list)
                System.out.println("**** missing value (either list or non-list)");
                Errors errors = new Errors();
                errors.setValid(false);
                errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_TEMPLATE_MISSING_REQUIRED_TERM, path, new Object[]{alias});
                HashMap<String, Object> subErrorMap = new HashMap<>();
                subErrorMap.put(ROOT_LEVEL_ERRORS_KEY, errors);
                errorMap.put(alias, subErrorMap);
            } else {
                if (isList != null && isList) {
                    if (value instanceof List) {
                        if (((List) value).isEmpty()) {
                            // empty list
                            System.out.println("**** empty list");
                            Errors errors = new Errors();
                            errors.setValid(false);
                            errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_TEMPLATE_MISSING_REQUIRED_TERM, path, new Object[]{alias});
                            HashMap<String, Object> subErrorMap = new HashMap<>();
                            subErrorMap.put(ROOT_LEVEL_ERRORS_KEY, errors);
                            errorMap.put(alias, subErrorMap);
                        }
                    } else {
                        // wrong type of value for list
                        System.out.println("**** wrong type of value for list");
                        Errors errors = new Errors();
                        errors.setValid(false);
                        errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_TEMPLATE_MISSING_REQUIRED_TERM, path, new Object[]{alias});
                        HashMap<String, Object> subErrorMap = new HashMap<>();
                        subErrorMap.put(ROOT_LEVEL_ERRORS_KEY, errors);
                        errorMap.put(alias, subErrorMap);
                    }
                } else {
                    if (value instanceof String && ((String) value).isEmpty()) {
                        // treat empty string as missing value
                        Errors errors = new Errors();
                        errors.setValid(false);
                        errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_TEMPLATE_MISSING_REQUIRED_TERM, path, new Object[]{alias});
                        HashMap<String, Object> subErrorMap = new HashMap<>();
                        subErrorMap.put(ROOT_LEVEL_ERRORS_KEY, errors);
                        errorMap.put(alias, subErrorMap);
                    }
                }
            }
        }

        return errorMap;
    }

    /**
     * Private Methods
     */
    private Object validateValue(Term term, Object value, String path) {
        ValidationType validationType = term.getValidation();
        Object errors = null;
        if (validationType != null) {
            List<Validator> validators = validationType.getValidator();
            for (Validator validator : validators) {
                boolean isList = (term.isList() == null ? false : term.isList());
                // to avoid the multiselect problem: accept a list/array of values without having list property set to true
                // 1. multiselect enabled list validation is the validation
                // 2. multiple file selection
                boolean mayAllowMultipleValues = (validator.getType().equals(ListValidator.validatorName) || validator.getType().equals(FileValidator.validatorName));
                if ((!mayAllowMultipleValues || (mayAllowMultipleValues && isList)) && (value instanceof List || value instanceof Object[])) {
                    if (isList) {
                        errors = new ArrayList<>();
                        if (value instanceof List) {
                            for (Object v : (List) value) {
                                ((ArrayList<Errors>)errors).add(validateValue(validator, v, path, term));
                            }
                        }
                        if (value instanceof Object[]) {
                            for (Object v : (Object[]) value) {
                                ((ArrayList<Errors>)errors).add(validateValue(validator, v, path, term));
                            }
                        }
                    } else {
                        errors = new Errors();
                        ((Errors)errors).add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_LIST_PROPERTY_NOT_SET, path, new Object[]{value.getClass().getName()});
                        ((Errors)errors).setValid(false);
                    }
                } else {
                    errors = validateValue(validator, value, path, term);
                }
            }
        }

        if (errors == null) {
            errors = new Errors();
            ((Errors)errors).setValid(true);
        }

        return errors;
    }

    private Errors validateValue(Validator validator, Object value, String fieldName, Term term) {
        Errors errors = new Errors();

        String validatorType = validator.getType();
        if (validatorType != null) {
            if (validatorType.equals(TextValidator.validatorName)) {
                TextValidator nv = new TextValidator(validator, errors, fieldName, term);
                nv.validate(value, fieldName, errors);
            } else if (validatorType.equals(NumericValidator.validatorName)) {
                NumericValidator nv = new NumericValidator(validator, errors, fieldName, term);
                nv.validate(value, fieldName, errors);
            } else if (validatorType.equals(ListValidator.validatorName)) {
                ListValidator lv = new ListValidator(validator, errors, fieldName, term);
                lv.validate(value, fieldName, errors);
            } else if (validatorType.equals(PreDefinedValidator.validatorName)) {
                PreDefinedValidator pdf = new PreDefinedValidator(validator, errors, fieldName, term);
                pdf.validate(value, fieldName, errors);
            } else if (validatorType.equals(DateTimeValidator.validatorName)) {
                DateTimeValidator dateTimeValidator = new DateTimeValidator(validator, errors, fieldName, term);
                dateTimeValidator.validate(value, fieldName, errors);
            } else if (validatorType.equals(SpecialValidator.validatorName)) {
                SpecialValidator specialValidator = new SpecialValidator(validator, errors, fieldName, term);
                specialValidator.validate(value, fieldName, errors);
            } else if (validatorType.equals(BooleanValidator.validatorName)) {
                BooleanValidator booleanValidator = new BooleanValidator();
                booleanValidator.validate(value, fieldName, errors);
            } else if (validatorType.equals(FileValidator.validatorName)) {
                FileValidator fileValidator = new FileValidator(validator, errors, fieldName, term);
                fileValidator.validate(value, fieldName, errors);
            } else if (validatorType.equals(RegexValidator.validatorName)) {
                RegexValidator regexValidator = new RegexValidator(validator, errors, fieldName, term);
                regexValidator.validate(value, fieldName, errors);
            } else if (validatorType.equals(CompositeValidator.validatorName)) {
                CompositeValidator regexValidator = new CompositeValidator(validator, errors, fieldName, term);
                regexValidator.validate(value, fieldName, errors);
            } else {
                // missing validator type is treated the same as composite type
                CompositeValidator regexValidator = new CompositeValidator(validator, errors, fieldName, term);
                regexValidator.validate(value, fieldName, errors);
            }
        } else {
            errors.setValid(true);
        }

        return errors;
    }

    public static Boolean result(Object validationResult) {
        Boolean valid = true;

        if (validationResult instanceof Map) {
            for (Object object : (((Map) validationResult).values())) {
                valid = valid & result(object);
            }
        } else if (validationResult instanceof List) {
            for (Object object : (((List) validationResult))) {
                valid = valid & result(object);
            }
        } else if (validationResult instanceof Errors) {
            valid = ((Errors) validationResult).isValid();
        } else {
            throw new RuntimeException("Invalid validation result structure");
        }

        return valid;
    }

    public static List<Errors> errors(Object validationResult) {
        List<Errors> errors;

        if (validationResult instanceof Map) {
            errors = new ArrayList<>();
            for (Object object : (((Map) validationResult).values())) {
                errors.addAll(errors(object));
            }
        } else if (validationResult instanceof List) {
            errors = new ArrayList<>();
            for (Object object : (((List) validationResult))) {
                errors.addAll(errors(object));
            }
        } else if (validationResult instanceof Errors) {
            errors = Arrays.asList((Errors) validationResult);
        } else {
            throw new RuntimeException("Invalid validation result structure");
        }

        return errors;
    }

    public static List<Error> errorList(Object validationResult) {
        List<Error> errors;

        if (validationResult instanceof Map) {
            errors = new ArrayList<>();
            for (Object object : (((Map) validationResult).values())) {
                errors.addAll(errorList(object));
            }
        } else if (validationResult instanceof List) {
            errors = new ArrayList<>();
            for (Object object : (((List) validationResult))) {
                errors.addAll(errorList(object));
            }
        } else if (validationResult instanceof Errors) {
            errors = ((Errors) validationResult).getErrorList();
        } else {
            throw new RuntimeException("Invalid validation result structure");
        }

        return errors;
    }

}
