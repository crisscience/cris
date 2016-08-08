package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.vocabulary.error.Errors;
import edu.purdue.cybercenter.dm.xml.vocabulary.Property;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType.Validator;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorCodes;
import edu.purdue.cybercenter.dm.vocabulary.error.ErrorType;
import edu.purdue.cybercenter.dm.vocabulary.error.ModuleCode;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestTemplate;

public class SpecialValidator implements BaseValidator {

    static enum Type {
        SERVICE("service");

        String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    };

    public static final String validatorName = "special";
    private Type name = null;
    public Map<String, String> params = new HashMap<>();

    public SpecialValidator(Validator validator, Errors errors, String fieldName, Term term) {
        List<Property> validatorProps = validator.getProperty();
        for (Property property : validatorProps) {
            String propName = property.getName();
            if (propName.equalsIgnoreCase("name")) {
                String propValue = property.getValue();
                if (propValue != null && !propValue.equals("")) {
                    if (propValue.equalsIgnoreCase(Type.SERVICE.getName())) {
                        name = Type.SERVICE;
                        break;
                    }
                }
            }
        }

        for (Property property : validatorProps) {
            params.put(property.getName(), property.getValue());
        }

        // Verify Parameters for the supported Types
        if (name == null) {
            // Unknown special validator specified.
            errors.setValid(false);
            errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_TYPE_NOT_FOUND, fieldName,
                    new Object[]{params.containsKey("name") ? params.get("name") : ""});
        } else if (!params.containsKey("uri")) {
            // URI property missing
            errors.setValid(false);
            errors.add(ModuleCode.VOCABULARY, ErrorType.ERROR, ErrorCodes.ERR_VOCB_VALIDATOR_REQUIRED_PROP_MISSING, fieldName, new Object[]{"uri"});
        }

    }

    @Override
    public Object validate(Object value, String fieldName, Errors errors) {
        if (value != null) {
            if (value instanceof String) {
                String valueStr = (String) value;
                if (name != null) {
                    RestTemplate restTemplate = new RestTemplate();

                    // Remove parameters like username, password, and uri from the request.
                    String uri = params.get("uri");
                    Map<String, String> copyParams = new HashMap<>();
                    copyParams.putAll(params);
                    copyParams.remove("uri");
                    copyParams.remove("username");
                    copyParams.remove("password");

                    // Add value parameter
                    copyParams.put("value", valueStr);

                    // Prepare Request and Post it.
                    String xmlRequest = buildXMLRequest(copyParams);
                    String xmlResponse = restTemplate.postForObject(uri, xmlRequest, String.class);
                    System.out.println(xmlResponse);
                    if (xmlResponse.contains("fail")) {
                        errors.setValid(false);
                    }
                }
            }
        }
        return errors;
    }

    public static String buildXMLRequest(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        if (params != null) {
            for (String type : params.keySet()) {
                String value = params.get(type);
                sb.append("<").append(type).append(">");
                sb.append(value);
                sb.append("</").append(type).append(">" + "\n");
            }
        }
        return sb.toString();
    }

}
