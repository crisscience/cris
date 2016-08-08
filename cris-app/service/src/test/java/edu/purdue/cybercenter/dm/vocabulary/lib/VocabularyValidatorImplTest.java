package edu.purdue.cybercenter.dm.vocabulary.lib;

import edu.purdue.cybercenter.dm.vocabulary.validators.VocabularyValidatorImpl;
import edu.purdue.cybercenter.dm.vocabulary.error.Error;
import edu.purdue.cybercenter.dm.vocabulary.validators.*;
import edu.purdue.cybercenter.dm.xml.vocabulary.Property;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType;
import org.junit.Before;
import org.junit.Test;
import static edu.purdue.cybercenter.dm.vocabulary.validators.VocabularyValidatorImpl.ROOT_LEVEL_ERRORS_KEY;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author rangars
 */

public class VocabularyValidatorImplTest {

    private VocabularyValidatorImpl vocabValidator;
    private Property minPrecisionProperty;
    private Property maxPrecisionProperty;
    private Property baseProperty;
    private Property rangeProperty;
    private Property typeProperty;
    private Property lengthProperty;
    private Property listItemProperty1;
    private Property listItemProperty2;
    private Property listItemProperty3;
    private Property multiSelectProperty;
    private Property dateTimeProperty;
    private Property uiVerticalLinesProperty;
    private Property regexpProperty;

    @Before
    public void setUp() throws Exception {
        vocabValidator = new VocabularyValidatorImpl();

        minPrecisionProperty = new Property();
        minPrecisionProperty.setName("minPrecision");
        maxPrecisionProperty = new Property();
        maxPrecisionProperty.setName("maxPrecision");
        baseProperty = new Property();
        baseProperty.setName("base");
        rangeProperty = new Property();
        rangeProperty.setName("range");
        typeProperty = new Property();
        typeProperty.setName("type");
        lengthProperty = new Property();
        lengthProperty.setName("length");
        listItemProperty1 = new Property();
        listItemProperty1.setName("item");
        listItemProperty2 = new Property();
        listItemProperty2.setName("item");
        listItemProperty3 = new Property();
        listItemProperty3.setName("item");
        multiSelectProperty = new Property();
        multiSelectProperty.setName("isMultiSelect");
        dateTimeProperty = new Property();
        dateTimeProperty.setName("format");
        uiVerticalLinesProperty = new Property();
        uiVerticalLinesProperty.setName("ui-vertical-lines");
        regexpProperty = new Property();
        regexpProperty.setName("regexp");
    }

    @Test
    public void numericValidatorTest_RangeIsOptional() throws Exception {
        ValidationType.Validator numericValidator = new ValidationType.Validator();
        numericValidator.setType(NumericValidator.validatorName);

        Term numericTerm = new Term();
        numericTerm.setUuid(UUID.randomUUID().toString());
        numericTerm.setVersion(UUID.randomUUID().toString());
        numericTerm.setAlias("NumericalItem");
        numericTerm.setName("NumericalItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(numericValidator);
        numericTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(numericTerm, "4");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void numericValidatorTest_BaseIsOptional() throws Exception {
        ValidationType.Validator numericValidator = new ValidationType.Validator();
        numericValidator.setType(NumericValidator.validatorName);
        rangeProperty.setValue("[-infinity, 0)[3,4)(5,10]");
        numericValidator.getProperty().add(rangeProperty);

        Term numericTerm = new Term();
        numericTerm.setUuid(UUID.randomUUID().toString());
        numericTerm.setVersion(UUID.randomUUID().toString());
        numericTerm.setAlias("NumericalItem");
        numericTerm.setName("NumericalItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(numericValidator);
        numericTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(numericTerm, "3");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void numericValidatorTest_PrecisionsAreOptional() throws Exception {
        ValidationType.Validator numericValidator = new ValidationType.Validator();
        numericValidator.setType(NumericValidator.validatorName);
        rangeProperty.setValue("[-infinity, 0)[3,4)(5,10]");
        numericValidator.getProperty().add(rangeProperty);

        Term numericTerm = new Term();
        numericTerm.setUuid(UUID.randomUUID().toString());
        numericTerm.setVersion(UUID.randomUUID().toString());
        numericTerm.setAlias("NumericalItem");
        numericTerm.setName("NumericalItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(numericValidator);
        numericTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(numericTerm, "3");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void numericValidatorTest_RangeTests() throws Exception {
        ValidationType.Validator numericValidator = new ValidationType.Validator();
        numericValidator.setType(NumericValidator.validatorName);
        rangeProperty.setValue("[-infinity, 0)[3,4)(5,10]");
        numericValidator.getProperty().add(rangeProperty);

        Term numericTerm = new Term();
        numericTerm.setUuid(UUID.randomUUID().toString());
        numericTerm.setVersion(UUID.randomUUID().toString());
        numericTerm.setAlias("NumericalItem");
        numericTerm.setName("NumericalItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(numericValidator);
        numericTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(numericTerm, "3");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        validationResult = vocabValidator.validate(numericTerm, "4");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.get(0).toString().contains("Validation failed for the value"));
    }

    @Test
    public void numericValidatorTest_PrecisionTests() throws Exception {
        ValidationType.Validator numericValidator = new ValidationType.Validator();
        numericValidator.setType(NumericValidator.validatorName);

        Term numericTerm = new Term();
        numericTerm.setUuid(UUID.randomUUID().toString());
        numericTerm.setVersion(UUID.randomUUID().toString());
        numericTerm.setAlias("NumericalItem");
        numericTerm.setName("NumericalItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(numericValidator);
        numericTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(numericTerm, "3");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
         // precision properties have been removed
        assertTrue(errors.isEmpty());

        validationResult = vocabValidator.validate(numericTerm, "3.43434");
        errors = VocabularyValidatorImpl.errorList(validationResult);
         // precision properties have been removed
        assertTrue(errors.isEmpty());

        validationResult = vocabValidator.validate(numericTerm, "3.236");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void textValidatorTest_TypeIsOptional() throws Exception {
        ValidationType.Validator textValidator = new ValidationType.Validator();
        textValidator.setType(TextValidator.validatorName);
        lengthProperty.setValue("20");
        textValidator.getProperty().add(lengthProperty);

        Term textTerm = new Term();
        textTerm.setUuid(UUID.randomUUID().toString());
        textTerm.setVersion(UUID.randomUUID().toString());
        textTerm.setAlias("TextItem");
        textTerm.setName("TextItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(textValidator);
        textTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(textTerm, "Short sentence.");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void textValidatorTest_UIVerticalLinesIsOptional() throws Exception {
        ValidationType.Validator textValidator = new ValidationType.Validator();
        textValidator.setType(TextValidator.validatorName);

        Term textTerm = new Term();
        textTerm.setUuid(UUID.randomUUID().toString());
        textTerm.setVersion(UUID.randomUUID().toString());
        textTerm.setAlias("TextItem");
        textTerm.setName("TextItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(textValidator);
        textTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(textTerm, "sentence");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void textValidatorTest_LengthIsOptional() throws Exception {
        ValidationType.Validator textValidator = new ValidationType.Validator();
        textValidator.setType(TextValidator.validatorName);
        typeProperty.setValue("alphanumeric");
        textValidator.getProperty().add(typeProperty);

        Term textTerm = new Term();
        textTerm.setUuid(UUID.randomUUID().toString());
        textTerm.setVersion(UUID.randomUUID().toString());
        textTerm.setAlias("TextItem");
        textTerm.setName("TextItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(textValidator);
        textTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(textTerm, "a3R_i9");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void textValidatorTest_UIVerticalLinesTests() throws Exception {
        ValidationType.Validator textValidator = new ValidationType.Validator();
        textValidator.setType(TextValidator.validatorName);

        Term textTerm = new Term();
        textTerm.setUuid(UUID.randomUUID().toString());
        textTerm.setVersion(UUID.randomUUID().toString());
        textTerm.setAlias("TextItem");
        textTerm.setName("TextItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(textValidator);
        textTerm.setValidation(validatorList);

        uiVerticalLinesProperty.setValue("NaN");
        textValidator.getProperty().add(uiVerticalLinesProperty);

        Object validationResult = vocabValidator.validate(textTerm, "doesn't really matter");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Non-numeric value for ui-vertical-lines property."));

        uiVerticalLinesProperty.setValue("10");
        validationResult = vocabValidator.validate(textTerm, "doesn't really matter");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void textValidatorTest_TypeTests() throws Exception {
        ValidationType.Validator textValidator = new ValidationType.Validator();
        textValidator.setType(TextValidator.validatorName);
        typeProperty.setValue("alphanumeric");
        textValidator.getProperty().add(typeProperty);

        Term textTerm = new Term();
        textTerm.setUuid(UUID.randomUUID().toString());
        textTerm.setVersion(UUID.randomUUID().toString());
        textTerm.setAlias("TextItem");
        textTerm.setName("TextItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(textValidator);
        textTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(textTerm, "spaces not allowed");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("only alphanumeric is expected"));

        validationResult = vocabValidator.validate(textTerm, "a3R_i9");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        typeProperty.setValue("printable");
        validatorList.getValidator().clear();
        validatorList.getValidator().add(textValidator);
        textTerm.setValidation(validatorList);

        validationResult = vocabValidator.validate(textTerm, "spaces are allowed. so are many printable characters such as ~,!,@,#,$,%,^,&,*,(,),-,=,+,_. And of course, numbers from 1 to 9 and combinations too!");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        typeProperty.setValue("alpha");
        validatorList.getValidator().clear();
        validatorList.getValidator().add(textValidator);
        textTerm.setValidation(validatorList);

        validationResult = vocabValidator.validate(textTerm, "only lower and uppercase english characters. no spaces even.");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("only alphabetic is expected"));

        validationResult = vocabValidator.validate(textTerm, "abcdefghijklmmopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        typeProperty.setValue("numeric");
        validatorList.getValidator().clear();
        validatorList.getValidator().add(textValidator);
        textTerm.setValidation(validatorList);

        validationResult = vocabValidator.validate(textTerm, "only numbers. no spaces even.");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("only numeric is expected"));

        validationResult = vocabValidator.validate(textTerm, "1234567890");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void textValidatorTest_DefaultTypeIsPrintable() throws Exception {
        ValidationType.Validator textValidator = new ValidationType.Validator();
        textValidator.setType(TextValidator.validatorName);

        Term textTerm = new Term();
        textTerm.setUuid(UUID.randomUUID().toString());
        textTerm.setVersion(UUID.randomUUID().toString());
        textTerm.setAlias("TextItem");
        textTerm.setName("TextItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(textValidator);
        textTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(textTerm, "spaces are allowed. so are many printable characters such as ~,!,@,#,$,%,^,&,*,(,),-,=,+,_. And of course, numbers from 1 to 9 and combinations too!");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void textValidatorTest_LengthTests() throws Exception {
        ValidationType.Validator textValidator = new ValidationType.Validator();
        textValidator.setType(TextValidator.validatorName);
        lengthProperty.setValue("20");
        textValidator.getProperty().add(lengthProperty);

        Term textTerm = new Term();
        textTerm.setUuid(UUID.randomUUID().toString());
        textTerm.setVersion(UUID.randomUUID().toString());
        textTerm.setAlias("TextItem");
        textTerm.setName("TextItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(textValidator);
        textTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(textTerm, "This sentence should definitely be longer than 20 characters!");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Length of string"));

        validationResult = vocabValidator.validate(textTerm, "Short sentence.");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void listValidatorTest_ItemIsRequired() throws Exception {
        ValidationType.Validator listValidator = new ValidationType.Validator();
        listValidator.setType(ListValidator.validatorName);
        multiSelectProperty.setValue("true");
        listValidator.getProperty().add(multiSelectProperty);

        Term listTerm = new Term();
        listTerm.setUuid(UUID.randomUUID().toString());
        listTerm.setVersion(UUID.randomUUID().toString());
        listTerm.setAlias("ListItem");
        listTerm.setName("ListItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(listValidator);
        listTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(listTerm, "Option 1");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Validation failed. The allowed values list is empty."));
    }

    @Test
    public void listValidatorTest_MultiSelectIsOptional() throws Exception {
        ValidationType.Validator listValidator = new ValidationType.Validator();
        listValidator.setType(ListValidator.validatorName);
        listItemProperty1.setValue("Option 1");
        listValidator.getProperty().add(listItemProperty1);

        Term listTerm = new Term();
        listTerm.setUuid(UUID.randomUUID().toString());
        listTerm.setVersion(UUID.randomUUID().toString());
        listTerm.setAlias("ListItem");
        listTerm.setName("ListItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(listValidator);
        listTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(listTerm, "Option 1");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void listValidatorTest_ItemTests() throws Exception {
        ValidationType.Validator listValidator = new ValidationType.Validator();
        listValidator.setType(ListValidator.validatorName);
        listItemProperty1.setValue("Option 1");
        listItemProperty2.setValue("Option 2");
        listItemProperty3.setValue("Option 3");
        listValidator.getProperty().add(listItemProperty1);
        listValidator.getProperty().add(listItemProperty2);
        listValidator.getProperty().add(listItemProperty3);

        Term listTerm = new Term();
        listTerm.setUuid(UUID.randomUUID().toString());
        listTerm.setVersion(UUID.randomUUID().toString());
        listTerm.setAlias("ListItem");
        listTerm.setName("ListItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(listValidator);
        listTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(listTerm, "Option 4");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Allowed list of values"));

        validationResult = vocabValidator.validate(listTerm, "Option 1");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(listTerm, "Option 2");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(listTerm, "Option 3");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void listValidatorTest_MultiSelectTests() throws Exception {
        ValidationType.Validator listValidator = new ValidationType.Validator();
        listValidator.setType(ListValidator.validatorName);
        listItemProperty1.setValue("Option 1");
        listItemProperty2.setValue("Option 2");
        listItemProperty3.setValue("Option 3");
        multiSelectProperty.setValue("true");
        listValidator.getProperty().add(listItemProperty1);
        listValidator.getProperty().add(listItemProperty2);
        listValidator.getProperty().add(listItemProperty3);
        listValidator.getProperty().add(multiSelectProperty);

        Term listTerm = new Term();
        listTerm.setUuid(UUID.randomUUID().toString());
        listTerm.setVersion(UUID.randomUUID().toString());
        listTerm.setAlias("ListItem");
        listTerm.setName("ListItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(listValidator);
        listTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(listTerm, "Option 4");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Allowed list of values"));

        validationResult = vocabValidator.validate(listTerm, "Option 1");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(listTerm, "Option 2");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(listTerm, "Option 3");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(listTerm, Arrays.asList("Option 1", "Option 2"));
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(listTerm, Arrays.asList("Option 2", "Option 3"));
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(listTerm, Arrays.asList("Option 1", "Option 3"));
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(listTerm, Arrays.asList("Option 1", "Option 2", "Option 3"));
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        listTerm.setList(true);
        validationResult = vocabValidator.validate(listTerm, Arrays.asList(Arrays.asList("Option 1", "Option 2"), Arrays.asList("Option 2", "Option 3"), Arrays.asList("Option 1", "Option 3"), Arrays.asList("Option 1", "Option 2", "Option 3")));
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

    }

    @Test
    public void dateTimeValidatorTest_dateTimeTests() throws Exception {
        ValidationType.Validator dateTimeValidator = new ValidationType.Validator();
        dateTimeValidator.setType(DateTimeValidator.validatorName);
        dateTimeValidator.getProperty().add(dateTimeProperty);

        Term dateTimeTerm = new Term();
        dateTimeTerm.setUuid(UUID.randomUUID().toString());
        dateTimeTerm.setVersion(UUID.randomUUID().toString());
        dateTimeTerm.setAlias("DateTimeItem");
        dateTimeTerm.setName("DateTimeItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(dateTimeValidator);
        dateTimeTerm.setValidation(validatorList);

        dateTimeProperty.setValue(DateTimeValidator.FORMAT_DATE);

        Object validationResult = vocabValidator.validate(dateTimeTerm, "2012-013-16");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("does not match the pattern"));

        validationResult = vocabValidator.validate(dateTimeTerm, "2012-03-16T11:55:55Z");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        dateTimeProperty.setValue(DateTimeValidator.FORMAT_DATETIME);

        validationResult = vocabValidator.validate(dateTimeTerm, "2012-03-1611:55:55-05:00");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("does not match the pattern"));

        validationResult = vocabValidator.validate(dateTimeTerm, "2012-03-16T11:55:55-05:00");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        validationResult = vocabValidator.validate(dateTimeTerm, "2012-03-16T11:55:55.000-05:00");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        validationResult = vocabValidator.validate(dateTimeTerm, "2012-03-16T11:55:55Z");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        validationResult = vocabValidator.validate(dateTimeTerm, "2012-03-16T11:55:55.000Z");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        dateTimeProperty.setValue(DateTimeValidator.FORMAT_TIME);

        validationResult = vocabValidator.validate(dateTimeTerm, "2014-07-14T111:55:55Z");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("does not match the pattern"));

        validationResult = vocabValidator.validate(dateTimeTerm, "2014-07-14T11:55:55Z");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        dateTimeProperty.setValue(DateTimeValidator.FORMAT_GDAY);

        validationResult = vocabValidator.validate(dateTimeTerm, "11");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        dateTimeProperty.setValue(DateTimeValidator.FORMAT_GMONTH);

        validationResult = vocabValidator.validate(dateTimeTerm, "01");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        dateTimeProperty.setValue(DateTimeValidator.FORMAT_GMONTHDAY);

        validationResult = vocabValidator.validate(dateTimeTerm, "01-13");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        dateTimeProperty.setValue(DateTimeValidator.FORMAT_GYEAR);

        validationResult = vocabValidator.validate(dateTimeTerm, "2009");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());

        dateTimeProperty.setValue(DateTimeValidator.FORMAT_GYEARMONTH);

        validationResult = vocabValidator.validate(dateTimeTerm, "2009-12");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void booleanValidatorTests() throws Exception {
        ValidationType.Validator booleanValidator = new ValidationType.Validator();
        booleanValidator.setType(BooleanValidator.validatorName);

        Term booleanTerm = new Term();
        booleanTerm.setUuid(UUID.randomUUID().toString());
        booleanTerm.setVersion(UUID.randomUUID().toString());
        booleanTerm.setAlias("BooleanItem");
        booleanTerm.setName("BooleanItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(booleanValidator);
        booleanTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(booleanTerm, "asdf");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Non-boolean value specified."));

        validationResult = vocabValidator.validate(booleanTerm, 1);
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Non-boolean value specified."));

        validationResult = vocabValidator.validate(booleanTerm, new Object());
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Non-boolean value specified."));

        validationResult = vocabValidator.validate(booleanTerm, "true");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(booleanTerm, "false");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(booleanTerm, "True");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(booleanTerm, "False");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(booleanTerm, "TRUE");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(booleanTerm, "FALSE");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(booleanTerm, "tRuE");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(booleanTerm, "faLSe");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(booleanTerm, true);
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        validationResult = vocabValidator.validate(booleanTerm, false);
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void fileValidatorTests() throws Exception {
        ValidationType.Validator fileValidator = new ValidationType.Validator();
        fileValidator.setType(FileValidator.validatorName);

        Term fileTerm = new Term();
        fileTerm.setUuid(UUID.randomUUID().toString());
        fileTerm.setVersion(UUID.randomUUID().toString());
        fileTerm.setAlias("FileItem");
        fileTerm.setName("FileItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(fileValidator);
        fileTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(fileTerm, "someFile:123123");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Invalid filename, expected: StorageFile:<number>"));

        validationResult = vocabValidator.validate(fileTerm, "StorageFile:1234");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void regexValidatorTests_RegexpIsRequired() throws Exception {
        ValidationType.Validator regexValidator = new ValidationType.Validator();
        regexValidator.setType(RegexValidator.validatorName);

        Term regexTerm = new Term();
        regexTerm.setUuid(UUID.randomUUID().toString());
        regexTerm.setVersion(UUID.randomUUID().toString());
        regexTerm.setAlias("RegexItem");
        regexTerm.setName("RegexItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(regexValidator);
        regexTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(regexTerm, "!33");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Missing required property regexp."));
    }

    @Test
    public void regexValidatorTests_RegexpMustBeValid() throws Exception {
        ValidationType.Validator regexValidator = new ValidationType.Validator();
        regexValidator.setType(RegexValidator.validatorName);
        regexpProperty.setValue("^[^]");
        regexValidator.getProperty().add(regexpProperty);

        Term regexTerm = new Term();
        regexTerm.setUuid(UUID.randomUUID().toString());
        regexTerm.setVersion(UUID.randomUUID().toString());
        regexTerm.setAlias("RegexItem");
        regexTerm.setName("RegexItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(regexValidator);
        regexTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(regexTerm, "asdf");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Invalid regular expression."));
    }

    @Test
    public void regexValidatorTests_RegexpOnlyValidProperty() throws Exception {
        ValidationType.Validator regexValidator = new ValidationType.Validator();
        regexValidator.setType(RegexValidator.validatorName);
        regexpProperty.setValue("abcd");
        lengthProperty.setValue("20");
        regexValidator.getProperty().add(regexpProperty);
        regexValidator.getProperty().add(lengthProperty);

        Term regexTerm = new Term();
        regexTerm.setUuid(UUID.randomUUID().toString());
        regexTerm.setVersion(UUID.randomUUID().toString());
        regexTerm.setAlias("RegexItem");
        regexTerm.setName("RegexItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(regexValidator);
        regexTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(regexTerm, "abcd");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("Unknown property specified for Regex validator. Expected regexp."));
    }

    @Test
    public void regexValidatorTests_RegexpTests() throws Exception {
        ValidationType.Validator regexValidator = new ValidationType.Validator();
        regexValidator.setType(RegexValidator.validatorName);
        regexpProperty.setValue("[A-Za-z]*");
        regexValidator.getProperty().add(regexpProperty);

        Term regexTerm = new Term();
        regexTerm.setUuid(UUID.randomUUID().toString());
        regexTerm.setVersion(UUID.randomUUID().toString());
        regexTerm.setAlias("RegexItem");
        regexTerm.setName("RegexItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(regexValidator);
        regexTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(regexTerm, "this is invalid");
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.toString().contains("expected [[A-Za-z]*]"));

        validationResult = vocabValidator.validate(regexTerm, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void termMustHaveListPropertyForMultipleValueValidation() throws Exception {
        ValidationType.Validator regexValidator = new ValidationType.Validator();
        regexValidator.setType(RegexValidator.validatorName);
        regexpProperty.setValue("[A-Za-z]*");
        regexValidator.getProperty().add(regexpProperty);

        Term regexTerm = new Term();
        regexTerm.setUuid(UUID.randomUUID().toString());
        regexTerm.setVersion(UUID.randomUUID().toString());
        regexTerm.setAlias("RegexItem");
        regexTerm.setName("RegexItem");
        ValidationType validatorList = new ValidationType();
        validatorList.getValidator().add(regexValidator);
        regexTerm.setValidation(validatorList);

        Object validationResult = vocabValidator.validate(regexTerm, Arrays.asList("abc", "DEF"));
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).toString().contains("To pass in multiple values, set list property to true for term."));

        regexTerm.setList(true);

        validationResult = vocabValidator.validate(regexTerm, Arrays.asList("abc", "DEF"));
        errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void comprehensiveTest_NestedTerms_Lists() throws Exception {
        /* regex term */
        ValidationType.Validator regexValidator = new ValidationType.Validator();
        regexValidator.setType(RegexValidator.validatorName);
        regexpProperty.setValue("[A-Za-z]*");
        regexValidator.getProperty().add(regexpProperty);

        Term regexTerm = new Term();
        regexTerm.setUuid(UUID.randomUUID().toString());
        regexTerm.setVersion(UUID.randomUUID().toString());
        regexTerm.setAlias("RegexItem");
        regexTerm.setName("RegexItem");
        ValidationType regexValidatorList = new ValidationType();
        regexValidatorList.getValidator().add(regexValidator);
        regexTerm.setValidation(regexValidatorList);
        /*-------------------*/

        /* File Term */
        ValidationType.Validator fileValidator = new ValidationType.Validator();
        fileValidator.setType(FileValidator.validatorName);

        Term fileTerm = new Term();
        fileTerm.setUuid(UUID.randomUUID().toString());
        fileTerm.setVersion(UUID.randomUUID().toString());
        fileTerm.setAlias("FileItem");
        fileTerm.setName("FileItem");
        ValidationType fileValidatorList = new ValidationType();
        fileValidatorList.getValidator().add(fileValidator);
        fileTerm.setValidation(fileValidatorList);
        /*-------------------*/

        /* Boolean term */
        ValidationType.Validator booleanValidator = new ValidationType.Validator();
        booleanValidator.setType(BooleanValidator.validatorName);

        Term booleanTerm = new Term();
        booleanTerm.setUuid(UUID.randomUUID().toString());
        booleanTerm.setVersion(UUID.randomUUID().toString());
        booleanTerm.setAlias("BooleanItem");
        booleanTerm.setName("BooleanItem");
        ValidationType booleanValidatorList = new ValidationType();
        booleanValidatorList.getValidator().add(booleanValidator);
        booleanTerm.setValidation(booleanValidatorList);
        /*-------------------*/

        /* Date Time term */
        ValidationType.Validator dateTimeValidator = new ValidationType.Validator();
        dateTimeValidator.setType(DateTimeValidator.validatorName);
        dateTimeValidator.getProperty().add(dateTimeProperty);

        Term dateTimeTerm = new Term();
        dateTimeTerm.setUuid(UUID.randomUUID().toString());
        dateTimeTerm.setVersion(UUID.randomUUID().toString());
        dateTimeTerm.setAlias("DateTimeItem");
        dateTimeTerm.setName("DateTimeItem");
        ValidationType dateTimeValidatorList = new ValidationType();
        dateTimeValidatorList.getValidator().add(dateTimeValidator);
        dateTimeTerm.setValidation(dateTimeValidatorList);
        dateTimeProperty.setValue(DateTimeValidator.FORMAT_DATE);
        /*-------------------*/

        /* List term */
        ValidationType.Validator listValidator = new ValidationType.Validator();
        listValidator.setType(ListValidator.validatorName);
        listItemProperty1.setValue("Option 1");
        listItemProperty2.setValue("Option 2");
        listItemProperty3.setValue("Option 3");
        listValidator.getProperty().add(listItemProperty1);
        listValidator.getProperty().add(listItemProperty2);
        listValidator.getProperty().add(listItemProperty3);

        Term listTerm = new Term();
        listTerm.setUuid(UUID.randomUUID().toString());
        listTerm.setVersion(UUID.randomUUID().toString());
        listTerm.setAlias("ListItem");
        listTerm.setName("ListItem");
        ValidationType listValidatorList = new ValidationType();
        listValidatorList.getValidator().add(listValidator);
        listTerm.setValidation(listValidatorList);
        /*-------------------*/

        /* Text term */
        ValidationType.Validator textValidator = new ValidationType.Validator();
        textValidator.setType(TextValidator.validatorName);
        typeProperty.setValue("alphanumeric");
        textValidator.getProperty().add(typeProperty);

        Term textTerm = new Term();
        textTerm.setList(true);
        textTerm.setUuid(UUID.randomUUID().toString());
        textTerm.setVersion(UUID.randomUUID().toString());
        textTerm.setAlias("TextItem");
        textTerm.setName("TextItem");
        ValidationType textValidatorList = new ValidationType();
        textValidatorList.getValidator().add(textValidator);
        textTerm.setValidation(textValidatorList);
        /*-------------------*/

        /* Numeric Term */
        ValidationType.Validator numericValidator = new ValidationType.Validator();
        numericValidator.setType(NumericValidator.validatorName);
        rangeProperty.setValue("[-infinity, 0)[3,4)(5,10]");
        numericValidator.getProperty().add(rangeProperty);

        Term numericTerm = new Term();
        numericTerm.setUuid(UUID.randomUUID().toString());
        numericTerm.setVersion(UUID.randomUUID().toString());
        numericTerm.setAlias("NumericalItem");
        numericTerm.setName("NumericalItem");
        ValidationType numericValidatorList = new ValidationType();
        numericValidatorList.getValidator().add(numericValidator);
        numericTerm.setValidation(numericValidatorList);
        /*-------------------*/

        Term complicatedTerm = new Term();
        complicatedTerm.setName("ComplicatedTerm");
        Term complexTerm1 = new Term();
        complexTerm1.setName("ComplexItem1");
        complexTerm1.setAlias("ComplexItem1");
        Term complexTerm2 = new Term();
        complexTerm2.setName("ComplexItem2");
        complexTerm2.setAlias("ComplexItem2");

        complicatedTerm.getTerm().add(listTerm);
        complicatedTerm.getTerm().add(regexTerm);
        complicatedTerm.getTerm().add(numericTerm);
        complexTerm1.getTerm().add(dateTimeTerm);
        complexTerm1.getTerm().add(booleanTerm);
        complexTerm2.getTerm().add(fileTerm);
        complexTerm2.getTerm().add(textTerm);
        complicatedTerm.getTerm().add(complexTerm1);
        complicatedTerm.getTerm().add(complexTerm2);

        Map<String, Object> valueMap = new HashMap<>();
        Map<String, Object> valueMap1 = new HashMap<>();
        Map<String, Object> valueMap2 = new HashMap<>();

        valueMap.put("ListItem", "Option 4");
        valueMap.put("RegexItem", "ai9_3");
        valueMap.put("NumericalItem", "4");

        valueMap1.put("DateTimeItem", "2013-99-01");
        valueMap1.put("BooleanItem", "flse");

        valueMap2.put("FileItem", "StorageFile:123a");
        valueMap2.put("TextItem", Arrays.asList("no spaces", "n0 5p3c!47 CH4r4ct3r5"));

        valueMap.put("ComplexItem1", valueMap1);
        valueMap.put("ComplexItem2", valueMap2);

        Object validationResult = vocabValidator.validate(complicatedTerm, valueMap);
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertFalse(errors.isEmpty());
        assertFalse(VocabularyValidatorImpl.result(validationResult));
        assertEquals("", ((HashMap) ((HashMap) validationResult).get("ComplicatedTerm")).get(ROOT_LEVEL_ERRORS_KEY).toString());
        assertEquals("[ERROR] CRIS-VOCB-109: Validation failed for [Option 4]. Allowed list of values [[Option 1, Option 2, Option 3]]\n", ((HashMap) ((HashMap) ((HashMap) validationResult).get("ComplicatedTerm")).get("ListItem")).get(ROOT_LEVEL_ERRORS_KEY).toString());
        assertEquals("[ERROR] CRIS-VOCB-125: Validation failed for [ai9_3], expected [[A-Za-z]*]\n", ((HashMap) ((HashMap) ((HashMap) validationResult).get("ComplicatedTerm")).get("RegexItem")).get(ROOT_LEVEL_ERRORS_KEY).toString());
        assertEquals("[ERROR] CRIS-VOCB-101: Validation failed for the value [4]. Expected value in the following interval [[[-infinity, 0.0), [3.0, 4.0), (5.0, 10.0]]]\n", ((HashMap) ((HashMap) ((HashMap) validationResult).get("ComplicatedTerm")).get("NumericalItem")).get(ROOT_LEVEL_ERRORS_KEY).toString());
        assertEquals("[ERROR] CRIS-VOCB-110: String [2013-99-01] does not match the pattern for Validator [date]\n", ((HashMap) ((HashMap) ((HashMap) ((HashMap) validationResult).get("ComplicatedTerm")).get("ComplexItem1")).get("DateTimeItem")).get(ROOT_LEVEL_ERRORS_KEY).toString());
        assertEquals("[ERROR] CRIS-VOCB-121: Validation failed. Non-boolean value specified.\n", ((HashMap)((HashMap)((HashMap)((HashMap) validationResult).get("ComplicatedTerm")).get("ComplexItem1")).get("BooleanItem")).get(ROOT_LEVEL_ERRORS_KEY).toString());
        assertEquals("[ERROR] CRIS-VOCB-122: Validation failed. Invalid filename, expected: StorageFile:<number>\n", ((HashMap)((HashMap)((HashMap)((HashMap) validationResult).get("ComplicatedTerm")).get("ComplexItem2")).get("FileItem")).get(ROOT_LEVEL_ERRORS_KEY).toString());
        assertEquals("[ERROR] CRIS-VOCB-117: There is a non-alphanumeric character available in the string [no spaces] when only alphanumeric is expected\n", ((HashMap) ((List) ((HashMap) ((HashMap) ((HashMap) validationResult).get("ComplicatedTerm")).get("ComplexItem2")).get("TextItem")).get(0)).get(ROOT_LEVEL_ERRORS_KEY).toString());
        assertEquals("[ERROR] CRIS-VOCB-117: There is a non-alphanumeric character available in the string [n0 5p3c!47 CH4r4ct3r5] when only alphanumeric is expected\n", ((HashMap) ((List) ((HashMap) ((HashMap) ((HashMap) validationResult).get("ComplicatedTerm")).get("ComplexItem2")).get("TextItem")).get(1)).get(ROOT_LEVEL_ERRORS_KEY).toString());
    }

    @Test
    public void comprehensiveNestedTermsTest_MustBeValid_ErrorListShouldBeEmpty() throws Exception {
        /* regex term */
        ValidationType.Validator regexValidator = new ValidationType.Validator();
        regexValidator.setType(RegexValidator.validatorName);
        regexpProperty.setValue("[A-Za-z]*");
        regexValidator.getProperty().add(regexpProperty);

        Term regexTerm = new Term();
        regexTerm.setUuid(UUID.randomUUID().toString());
        regexTerm.setVersion(UUID.randomUUID().toString());
        regexTerm.setAlias("RegexItem");
        regexTerm.setName("RegexItem");
        ValidationType regexValidatorList = new ValidationType();
        regexValidatorList.getValidator().add(regexValidator);
        regexTerm.setValidation(regexValidatorList);
        /*-------------------*/

        /* File Term */
        ValidationType.Validator fileValidator = new ValidationType.Validator();
        fileValidator.setType(FileValidator.validatorName);

        Term fileTerm = new Term();
        fileTerm.setUuid(UUID.randomUUID().toString());
        fileTerm.setVersion(UUID.randomUUID().toString());
        fileTerm.setAlias("FileItem");
        fileTerm.setName("FileItem");
        ValidationType fileValidatorList = new ValidationType();
        fileValidatorList.getValidator().add(fileValidator);
        fileTerm.setValidation(fileValidatorList);
        /*-------------------*/

        /* Boolean term */
        ValidationType.Validator booleanValidator = new ValidationType.Validator();
        booleanValidator.setType(BooleanValidator.validatorName);

        Term booleanTerm = new Term();
        booleanTerm.setUuid(UUID.randomUUID().toString());
        booleanTerm.setVersion(UUID.randomUUID().toString());
        booleanTerm.setAlias("BooleanItem");
        booleanTerm.setName("BooleanItem");
        ValidationType booleanValidatorList = new ValidationType();
        booleanValidatorList.getValidator().add(booleanValidator);
        booleanTerm.setValidation(booleanValidatorList);
        /*-------------------*/

        /* Date Time term */
        ValidationType.Validator dateTimeValidator = new ValidationType.Validator();
        dateTimeValidator.setType(DateTimeValidator.validatorName);
        dateTimeValidator.getProperty().add(dateTimeProperty);

        Term dateTimeTerm = new Term();
        dateTimeTerm.setUuid(UUID.randomUUID().toString());
        dateTimeTerm.setVersion(UUID.randomUUID().toString());
        dateTimeTerm.setAlias("DateTimeItem");
        dateTimeTerm.setName("DateTimeItem");
        ValidationType dateTimeValidatorList = new ValidationType();
        dateTimeValidatorList.getValidator().add(dateTimeValidator);
        dateTimeTerm.setValidation(dateTimeValidatorList);
        dateTimeProperty.setValue(DateTimeValidator.FORMAT_DATE);
        /*-------------------*/

        /* List term */
        ValidationType.Validator listValidator = new ValidationType.Validator();
        listValidator.setType(ListValidator.validatorName);
        listItemProperty1.setValue("Option 1");
        listItemProperty2.setValue("Option 2");
        listItemProperty3.setValue("Option 3");
        listValidator.getProperty().add(listItemProperty1);
        listValidator.getProperty().add(listItemProperty2);
        listValidator.getProperty().add(listItemProperty3);

        Term listTerm = new Term();
        listTerm.setUuid(UUID.randomUUID().toString());
        listTerm.setVersion(UUID.randomUUID().toString());
        listTerm.setAlias("ListItem");
        listTerm.setName("ListItem");
        ValidationType listValidatorList = new ValidationType();
        listValidatorList.getValidator().add(listValidator);
        listTerm.setValidation(listValidatorList);
        /*-------------------*/

        /* Text term */
        ValidationType.Validator textValidator = new ValidationType.Validator();
        textValidator.setType(TextValidator.validatorName);
        typeProperty.setValue("alphanumeric");
        textValidator.getProperty().add(typeProperty);

        Term textTerm = new Term();
        textTerm.setList(true);
        textTerm.setUuid(UUID.randomUUID().toString());
        textTerm.setVersion(UUID.randomUUID().toString());
        textTerm.setAlias("TextItem");
        textTerm.setName("TextItem");
        ValidationType textValidatorList = new ValidationType();
        textValidatorList.getValidator().add(textValidator);
        textTerm.setValidation(textValidatorList);
        /*-------------------*/

        /* Numeric Term */
        ValidationType.Validator numericValidator = new ValidationType.Validator();
        numericValidator.setType(NumericValidator.validatorName);
        rangeProperty.setValue("[-infinity, 0)[3,4)(5,10]");
        numericValidator.getProperty().add(rangeProperty);

        Term numericTerm = new Term();
        numericTerm.setUuid(UUID.randomUUID().toString());
        numericTerm.setVersion(UUID.randomUUID().toString());
        numericTerm.setAlias("NumericalItem");
        numericTerm.setName("NumericalItem");
        ValidationType numericValidatorList = new ValidationType();
        numericValidatorList.getValidator().add(numericValidator);
        numericTerm.setValidation(numericValidatorList);
        /*-------------------*/

        Term complicatedTerm = new Term();
        complicatedTerm.setName("ComplicatedTerm");
        Term complexTerm1 = new Term();
        complexTerm1.setName("ComplexItem1");
        complexTerm1.setAlias("ComplexItem1");
        Term complexTerm2 = new Term();
        complexTerm2.setName("ComplexItem2");
        complexTerm2.setAlias("ComplexItem2");

        complicatedTerm.getTerm().add(listTerm);
        complicatedTerm.getTerm().add(regexTerm);
        complicatedTerm.getTerm().add(numericTerm);
        complexTerm1.getTerm().add(dateTimeTerm);
        complexTerm1.getTerm().add(booleanTerm);
        complexTerm2.getTerm().add(fileTerm);
        complexTerm2.getTerm().add(textTerm);
        complicatedTerm.getTerm().add(complexTerm1);
        complicatedTerm.getTerm().add(complexTerm2);

        Map<String, Object> valueMap = new HashMap<>();
        Map<String, Object> valueMap1 = new HashMap<>();
        Map<String, Object> valueMap2 = new HashMap<>();

        valueMap.put("ListItem", "Option 3");
        valueMap.put("RegexItem", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        valueMap.put("NumericalItem", "3");

        valueMap1.put("DateTimeItem", "2013-12-01T11:11:11Z");
        valueMap1.put("BooleanItem", "true");

        valueMap2.put("FileItem", "StorageFile:123");
        valueMap2.put("TextItem", Arrays.asList("valid", "text"));

        valueMap.put("ComplexItem1", valueMap1);
        valueMap.put("ComplexItem2", valueMap2);

        Object validationResult = vocabValidator.validate(complicatedTerm, valueMap);
        List<Error> errors = VocabularyValidatorImpl.errorList(validationResult);
        assertTrue(errors.isEmpty());
        assertTrue(VocabularyValidatorImpl.result(validationResult));
    }

}
