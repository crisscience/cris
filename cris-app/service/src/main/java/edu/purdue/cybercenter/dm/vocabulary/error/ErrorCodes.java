package edu.purdue.cybercenter.dm.vocabulary.error;

/**
 * Class contains constants defining possible product code, Module code and
 * OperationError codes. Also it contains utility methods which can create
 * a complete error code in the format <ProductCode>-<ModuleCode>-<OperationCode>
 *
 * @author agarwa50
 *
 */
public class ErrorCodes
{
    /***
     * PC - Product Code
     * MC - Module Code
     * OC - Operation Code
     */

    // Product Code
    public static String PRODUCT_CODE_CRIS = "CRIS";

    // Module Code
    public static String MODULE_CODE_VOCAB = "VOCB";
    public static String MODULE_CODE_STORAGE = "STRG";
    public static String MODULE_CODE_WORKFLOW = "WKFL";

    // Operation Code: Warning
    public static String WARN_VOCB_VALIDATOR_ADDITIONAL_PROPERTY = "001";
    public static String WARN_VOCB_VALIDATOR_ADDITIONAL_VALUE = "002";


    // Operation Code: Errors
    public static String ERR_VOCB_VALIDATOR_NUMERIC_RANGE_FAILED = "101";
    public static String ERR_VOCB_VALIDATOR_NUMERIC_NOT_A_NUMBER = "102";
    public static String ERR_VOCB_VALIDATOR_NUMERIC_MIN_PRECISION = "103";
    public static String ERR_VOCB_VALIDATOR_NUMERIC_MAX_PRECISION = "104";
    public static String ERR_VOCB_VALIDATOR_NUMERIC_BASE_MISMATCH = "105";
    public static String ERR_VOCB_VALIDATOR_TEXT_NON_ALPHA_CHAR = "106";
    public static String ERR_VOCB_VALIDATOR_TEXT_INVALID_LENGTH = "107";
    public static String ERR_VOCB_VALIDATOR_DOES_NOT_EXISTS = "108";
    public static String ERR_VOCB_VALIDATOR_LIST_INVALID_CONTENTS = "109";
    public static String ERR_VOCB_VALIDATOR_DATETIME_INVALID_DATE = "110";
    public static String ERR_VOCB_VALIDATOR_PREDEFINED_NON_EXISTENT = "111";
    public static String ERR_VOCB_VALIDATOR_PREDEFINED_INVALID_VALUE = "112";
    public static String ERR_VOCB_VALIDATOR_ADVANCED_INVALID_VALUE = "113";
    public static String ERR_VOCB_VALIDATOR_TYPE_NOT_FOUND = "114";
    public static String ERR_VOCB_VALIDATOR_REQUIRED_PROP_MISSING = "115";
    public static String ERR_VOCB_VALIDATOR_TEXT_NON_NUMERIC_CHAR = "116";
    public static String ERR_VOCB_VALIDATOR_TEXT_NON_ALPHANUMERIC_CHAR = "117";
    public static String ERR_VOCB_VALIDATOR_TEXT_NON_PRINTABLE_CHAR = "118";
    public static String ERR_VOCB_VALIDATOR_LIST_NO_ITEMS = "119";
    public static String ERR_VOCB_VALIDATOR_TEXT_NON_NUMERIC_UI_LINES = "120";
    public static String ERR_VOCB_VALIDATOR_BOOLEAN_VALIDATOR = "121";
    public static String ERR_VOCB_VALIDATOR_FILE_VALIDATOR_INVALID_FILENAME = "122";
    public static String ERR_VOCB_VALIDATOR_REGEX_VALIDATOR_REGEXP_REQUIRED = "123";
    public static String ERR_VOCB_VALIDATOR_REGEX_VALIDATOR_INVALID_REGEX = "124";
    public static String ERR_VOCB_VALIDATOR_REGEX_VALIDATOR_MISMATCH = "125";
    public static String ERR_VOCB_VALIDATOR_REGEX_VALIDATOR_UNKNOWN_PROPERTY = "126";

    public static String ERR_VOCB_TEMPLATE_TERM_DOES_NOT_EXISTS = "301";
    public static String ERR_VOCB_TEMPLATE_TERM_INVALID_UUID = "302";
    public static String ERR_VOCB_TEMPLATE_TERM_INVALID_VERSION = "303";
    public static String ERR_VOCB_TEMPLATE_MISSING_REQUIRED_TERM = "304";
    public static String ERR_VOCB_VALUE_MAP_EXPECTED = "305";
    public static String ERR_VOCB_VALUE_LIST_EXPECTED = "308";
    public static String ERR_VOCB_VALUE_STRING_EXPECTED = "309";
    public static String ERR_VOCB_LIST_PROPERTY_NOT_SET = "306";
    public static String ERR_VOCB_NO_MATCHING_VALIDATOR = "307";

}
