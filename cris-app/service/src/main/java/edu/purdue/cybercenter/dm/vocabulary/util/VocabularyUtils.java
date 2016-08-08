package edu.purdue.cybercenter.dm.vocabulary.util;

import edu.purdue.cybercenter.dm.vocabulary.validators.AdvancedValidator;
import edu.purdue.cybercenter.dm.vocabulary.validators.BooleanValidator;
import edu.purdue.cybercenter.dm.vocabulary.validators.CompositeValidator;
import edu.purdue.cybercenter.dm.vocabulary.validators.DateTimeValidator;
import edu.purdue.cybercenter.dm.vocabulary.validators.EnumProperty;
import edu.purdue.cybercenter.dm.vocabulary.validators.FileValidator;
import edu.purdue.cybercenter.dm.vocabulary.validators.ListValidator;
import edu.purdue.cybercenter.dm.vocabulary.validators.NumericValidator;
import edu.purdue.cybercenter.dm.vocabulary.validators.TextValidator;
import edu.purdue.cybercenter.dm.xml.vocabulary.AttachTo;
import edu.purdue.cybercenter.dm.xml.vocabulary.Property;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType;
import edu.purdue.cybercenter.dm.xml.vocabulary.Vocabulary;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

public class VocabularyUtils {

    public static boolean isSame(String v1, String v2) {
        boolean isSame;

        if (v1 == null && v2 == null) {
            isSame = true;
        } else if (v1 == null || v2 == null) {
            isSame = false;
        } else {
            isSame = v1.equals(v2);
        }

        return isSame;
    }

    public static boolean isSame(Boolean v1, Boolean v2) {
        boolean isSame;

        if (v1 == null && v2 == null) {
            isSame = true;
        } else if (v1 == null || v2 == null) {
            isSame = false;
        } else {
            isSame = v1.equals(v2);
        }

        return isSame;
    }

    public static boolean isSame(BigInteger v1, BigInteger v2) {
        boolean isSame;

        if (v1 == null && v2 == null) {
            isSame = true;
        } else if (v1 == null || v2 == null) {
            isSame = false;
        } else {
            isSame = v1.equals(v2);
        }

        return isSame;
    }

    public static boolean isSame(Property v1, Property v2) {
        boolean isSame;

        if (!isSame(v1.getValue(), v2.getValue())) {
            isSame = false;
        } else if (!isSame(v1.getName(), v2.getName())) {
            isSame = false;
        } else if ("item".equals(v1.getName()) && (v1.getId() != null && !v1.getId().equals(v2.getId())) || (v2.getId() != null && !v2.getId().equals(v1.getId()))) {
            isSame = false;
        } else {
            isSame = isSame(v1.isRequired(), v2.isRequired());
        }

        return isSame;
    }

    public static boolean isSame(AttachTo v1, AttachTo v2) {
        boolean isSame;

        if (!isSame(v1.getIdField(), v2.getIdField())) {
            isSame = false;
        } else if (!isSame(v1.getNameField(), v2.getNameField())) {
            isSame = false;
        } else if (!isSame(v1.getUseAlias(), v2.getUseAlias())) {
            isSame = false;
        } else if (!isSame(v1.getVersionName(), v2.getVersionName())) {
            isSame = false;
        } else if (!isSame(v1.getVersion(), v2.getVersion())) {
            isSame = false;
        } else if (!isSame(v1.getDescription(), v2.getDescription())) {
            isSame = false;
        } else if (!isSame(v1.getValue(), v2.getValue())) {
            isSame = false;
        } else if (!isSame(v1.getShowExpression(), v2.getShowExpression())) {
            isSame = false;
        } else if (!isSame(v1.getQuery(), v2.getQuery())) {
            isSame = false;
        } else if (!isSame(v1.getUuid(), v2.getUuid())) {
            isSame = false;
        } else if (!isSame(v1.isRequired(), v2.isRequired())) {
            isSame = false;
        } else if (!isSame(v1.getRequiredExpression(), v2.getRequiredExpression())) {
            isSame = false;
        } else if (!isSame(v1.isReadOnly(), v2.isReadOnly())) {
            isSame = false;
        } else if (!isSame(v1.getReadOnlyExpression(), v2.getReadOnlyExpression())) {
            isSame = false;
        } else if (!isSame(v1.isGrid(), v2.isGrid())) {
            isSame = false;
        } else {
            isSame = isSame(v1.isList(), v2.isList());
        }

        return isSame;
    }

    public static boolean isSame(ValidationType v1, ValidationType v2) {
        boolean isSame;

        if (v1 == null && v2 == null) {
            isSame = true;
        } else if (v1 == null || v2 == null) {
            isSame = false;
        } else {
            isSame = isSame(v1.getValidator(), v2.getValidator());
        }

        return isSame;
    }

    public static boolean isSame(Vocabulary.Contributors v1, Vocabulary.Contributors v2) {
        boolean isSame;

        if (v1 == null && v2 == null) {
            isSame = true;
        } else if (v1 == null || v2 == null) {
            isSame = false;
        } else {
            isSame = isSame(v1.getContributor(), v2.getContributor());
        }

        return isSame;
    }

    public static boolean isSame(ValidationType.Validator v1, ValidationType.Validator v2) {
        boolean isSame;

        if (!isSame(v1.getProperty(), v2.getProperty())) {
            isSame = false;
        } else if (!isSame(v1.getType(), v2.getType())) {
            isSame = false;
        } else {
            isSame = isSame(v1.getUuid(), v2.getUuid());
        }

        return isSame;
    }

    public static boolean isSame(Term v1, Term v2) {
        boolean isSame;

        if (!isSame(v1.getName(), v2.getName())) {
            isSame = false;
        } else if (!isSame(v1.getType(), v2.getType())) {
            isSame = false;
        } else if (!isSame(v1.getDescription(), v2.getDescription())) {
            isSame = false;
        } else if (!isSame(v1.getUnit(), v2.getUnit())) {
            isSame = false;
        } else if (!isSame(v1.getScale(), v2.getScale())) {
            isSame = false;
        } else if (!isSame(v1.getLength(), v2.getLength())) {
            isSame = false;
        } else if (!isSame(v1.getValue(), v2.getValue())) {
            isSame = false;
        } else if (!isSame(v1.getShowExpression(), v2.getShowExpression())) {
            isSame = false;
        } else if (!isSame(v1.getAlias(), v2.getAlias())) {
            isSame = false;
        } else if (!isSame(v1.isRequired(), v2.isRequired())) {
            isSame = false;
        } else if (!isSame(v1.getRequiredExpression(), v2.getRequiredExpression())) {
            isSame = false;
        } else if (!isSame(v1.isReadOnly(), v2.isReadOnly())) {
            isSame = false;
        } else if (!isSame(v1.getReadOnlyExpression(), v2.getReadOnlyExpression())) {
            isSame = false;
        } else if (!isSame(v1.isList(), v2.isList())) {
            isSame = false;
        } else if (!isSame(v1.getUiDisplayOrder(), v2.getUiDisplayOrder())) {
            isSame = false;
        } else if (!isSame(v1.isGrid(), v2.isGrid())) {
            isSame = false;
        } else /* Version should not be part of the comparison
         if (!isSame(v1.getVersionName(), v2.getVersionName())) {
         return false;
         }
         if (!isSame(v1.getVersion(), v2.getVersion())) {
         return false;
         }
         */ if (!isSame(v1.getUuid(), v2.getUuid())) {
            isSame = false;
        } else if (!isSame(v1.getTerm(), v2.getTerm())) {
            isSame = false;
        } else if (!isSame(v1.getProperty(), v2.getProperty())) {
            isSame = false;
        } else if (!isSame(v1.getAttachTo(), v2.getAttachTo())) {
            isSame = false;
        } else {
            isSame = isSame(v1.getValidation(), v2.getValidation());
        }

        return isSame;
    }

    public static boolean isSame(Vocabulary v1, Vocabulary v2) {
        boolean isSame;

        if (!isSame(v1.getUuid(), v2.getUuid())) {
            isSame = false;
        } else if (!isSame(v1.getName(), v2.getName())) {
            isSame = false;
        } else if (!isSame(v1.getDescription(), v2.getDescription())) {
            isSame = false;
        } else if (!isSame(v1.getDomain(), v2.getDomain())) {
            isSame = false;
        } else if (!isSame(v1.getContributors(), v2.getContributors())) {
            isSame = false;
        } else {
            isSame = isSame(v1.getCopyright(), v2.getCopyright());
        }

        return isSame;
    }

    public static <T> boolean isSame(T v1, T v2) {
        boolean isSame;

        if (v1 == null && v2 == null) {
            isSame = true;
        } else if (v1 == null || v2 == null) {
            isSame = false;
        } else if (v1 instanceof Boolean) {
            isSame = isSame((Boolean) v1, (Boolean) v2);
        } else if (v1 instanceof BigInteger) {
            isSame = isSame((BigInteger) v1, (BigInteger) v2);
        } else if (v1 instanceof String) {
            isSame = isSame((String) v1, (String) v2);
        } else if (v1 instanceof Term) {
            isSame = isSame((Term) v1, (Term) v2);
        } else if (v1 instanceof Property) {
            isSame = isSame((Property) v1, (Property) v2);
        } else if (v1 instanceof AttachTo) {
            isSame = isSame((AttachTo) v1, (AttachTo) v2);
        } else if (v1 instanceof ValidationType) {
            isSame = isSame((ValidationType) v1, (ValidationType) v2);
        } else if (v1 instanceof ValidationType.Validator) {
            isSame = isSame((ValidationType.Validator) v1, (ValidationType.Validator) v2);
        } else if (v1 instanceof Vocabulary) {
            isSame = isSame((Vocabulary) v1, (Vocabulary) v2);
        } else {
            isSame = false;
        }

        return isSame;
    }

    public static <T> boolean isSame(List<T> list1, List<T> list2) {
        boolean isSame;

        if (list1.size() != list2.size()) {
            isSame = false;
        } else if (list1.isEmpty()) {
            isSame = true;
        } else {
            boolean found = true;
            for (T item1 : list1) {
                found = false;
                for (T item2 : list2) {
                    if (isSame(item1, item2)) {
                        found = true;
                    }
                }
                if (!found) {
                    break;
                }
            }

            if (found) {
                for (T item1 : list2) {
                    found = false;
                    for (T item2 : list1) {
                        if (isSame(item1, item2)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        break;
                    }
                }
            }

            isSame = found;
        }

        return isSame;
    }

    public static boolean isMultipleFiles(Term term) {
        boolean multipleFiles = false;

        ValidationType validation = term.getValidation();
        if (validation != null) {
            List<ValidationType.Validator> validators = validation.getValidator();
            if (validators.size() == 1) {
                ValidationType.Validator validator = validators.get(0);
                boolean isFileValidator = FileValidator.validatorName.equals(validator.getType());
                if (isFileValidator) {
                    List<Property> properties = validator.getProperty();
                    for (Property property : properties) {
                        if ("multiple".equals(property.getName()) && "true".equals(property.getValue())) {
                            multipleFiles = true;
                        }
                    }
                }
            }
        }

        return multipleFiles;
    }

    public static void fixTerm(Term term) {
        // fix validation
        fixValidation(term);

        term.getTerm().stream().forEach((t) -> {
            fixTerm(t);
        });
    }

    public static ValidationType fixValidation(Term term) {
        if (term.getValidation() == null || term.getValidation().getValidator().isEmpty()) {
            // default to composite
            if (term.getValidation() == null) {
                ValidationType validationType = new ValidationType();
                term.setValidation(validationType);
            }

            if (term.getValidation().getValidator().isEmpty()) {
                ValidationType.Validator validator = new ValidationType.Validator();
                validator.setType(CompositeValidator.validatorName);
                term.getValidation().getValidator().add(validator);
            }
        }

        ValidationType validation = term.getValidation();

        fixValidationProperties(validation);

        return validation;
    }

    private static void fixValidationProperties(ValidationType validation) {
        if (validation != null) {
            List<ValidationType.Validator> validators = validation.getValidator();
            if (!validators.isEmpty()) {
                ValidationType.Validator validator = validators.get(0);
                String type = validator.getType();
                List<Property> properties = validator.getProperty();
                if (type != null && properties != null) {
                    removeUnknownProperties(type, properties);
                    addMissingProperties(type, properties);
                }
            }
        }
    }

    private static void addMissingProperties(String type, List<Property> properties) {
        switch (type) {
            case BooleanValidator.validatorName:
                // boolean does not have any property
                // nothing to add
                break;
            case NumericValidator.validatorName:
                for (NumericValidator.ValidatorProperty vp : NumericValidator.ValidatorProperty.values()) {
                    boolean exists = properties.stream().anyMatch((Property p) -> p.getName().equals(vp.getName()));
                    if (!exists) {
                        Property newProperty = createProperty(vp);
                        properties.add(newProperty);
                    }
                }
                break;
            case TextValidator.validatorName:
                for (TextValidator.ValidatorProperty vp : TextValidator.ValidatorProperty.values()) {
                    boolean exists = properties.stream().anyMatch((Property p) -> p.getName().equals(vp.getName()));
                    if (!exists) {
                        Property newProperty = createProperty(vp);
                        properties.add(newProperty);
                    }
                }
                break;
            case DateTimeValidator.validatorName:
                for (DateTimeValidator.ValidatorProperty vp : DateTimeValidator.ValidatorProperty.values()) {
                    boolean exists = properties.stream().anyMatch((Property p) -> p.getName().equals(vp.getName()));
                    if (!exists) {
                        Property newProperty = createProperty(vp);
                        properties.add(newProperty);
                    }
                }
                break;
            case ListValidator.validatorName:
                for (ListValidator.ValidatorProperty vp : ListValidator.ValidatorProperty.values()) {
                    boolean exists = properties.stream().anyMatch((Property p) -> p.getName().equals(vp.getName()));
                    if (!exists) {
                        Property newProperty = createProperty(vp);
                        properties.add(newProperty);
                    }
                }
                break;
            case FileValidator.validatorName:
                for (FileValidator.ValidatorProperty vp : FileValidator.ValidatorProperty.values()) {
                    boolean exists = properties.stream().anyMatch((Property p) -> p.getName().equals(vp.getName()));
                    if (!exists) {
                        Property newProperty = createProperty(vp);
                        properties.add(newProperty);
                    }
                }
                break;
            case AdvancedValidator.validatorName:
                for (AdvancedValidator.ValidatorProperty vp : AdvancedValidator.ValidatorProperty.values()) {
                    boolean exists = properties.stream().anyMatch((Property p) -> p.getName().equals(vp.getName()));
                    if (!exists) {
                        Property newProperty = createProperty(vp);
                        properties.add(newProperty);
                    }
                }
                break;
            case CompositeValidator.validatorName:
                for (CompositeValidator.ValidatorProperty vp : CompositeValidator.ValidatorProperty.values()) {
                    boolean exists = properties.stream().anyMatch((Property p) -> p.getName().equals(vp.getName()));
                    if (!exists) {
                        Property newProperty = createProperty(vp);
                        properties.add(newProperty);
                    }
                }
                break;
            default:
                break;
        }
    }

    private static void removeUnknownProperties(String type, List<Property> properties) {
        for (Iterator<Property> it = properties.iterator(); it.hasNext();) {
            Property property = it.next();
            String enumName = toEnumName(property.getName());
            try {
                switch (type) {
                    case BooleanValidator.validatorName:
                        properties.clear();
                        break;
                    case NumericValidator.validatorName:
                        NumericValidator.ValidatorProperty.valueOf(enumName);
                        break;
                    case TextValidator.validatorName:
                        TextValidator.ValidatorProperty.valueOf(enumName);
                        break;
                    case DateTimeValidator.validatorName:
                        DateTimeValidator.ValidatorProperty.valueOf(enumName);
                        break;
                    case ListValidator.validatorName:
                        ListValidator.ValidatorProperty.valueOf(enumName);
                        break;
                    case FileValidator.validatorName:
                        FileValidator.ValidatorProperty.valueOf(enumName);
                        break;
                    case AdvancedValidator.validatorName:
                        AdvancedValidator.ValidatorProperty.valueOf(enumName);
                        break;
                    default:
                        break;
                }
            } catch (IllegalArgumentException ex) {
                it.remove();
            }
        }
    }

    private static Property createProperty(EnumProperty ep) {
        Object value = ep.getValue();

        Property property = new Property();
        property.setId(ep.getId());
        property.setName(ep.getName());
        property.setValue(value == null ? "" : value.toString());
        property.setRequired(ep.isRequired());

        return property;
    }

    private static String toEnumName(String name) {
        if (name == null) {
            return name;
        }

        return name.toUpperCase().replaceAll("-", "");
    }
}
