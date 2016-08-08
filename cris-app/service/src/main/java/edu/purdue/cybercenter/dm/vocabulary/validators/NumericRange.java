package edu.purdue.cybercenter.dm.vocabulary.validators;

public class NumericRange {

    enum RangeType {
        RANGE_MIN_INCLUDE,
        RANGE_MIN_EXCLUDE,
        RANGE_MAX_INCLUDE,
        RANGE_MAX_EXCLUDE
    }

    public static final String INFINITY_POSITIVE = "+infinity";
    public static final String INFINITY_NEGATIVE = "-infinity";

    private Double rangeMinValue;
    private Double rangeMaxValue;
    private RangeType rangeMinType;
    private RangeType rangeMaxType;
    private Boolean isRangeMinInfinity;
    private Boolean isRangeMaxInfinity;

    public NumericRange(String rangeString, RangeType rangeMinType, RangeType rangeMaxType) {
        this.rangeMaxType = rangeMaxType;
        this.rangeMinType = rangeMinType;
        if (rangeString != null) {
            String[] ranges = rangeString.split(",");
            if (ranges.length != 2) {
                throw new RuntimeException("Template problem: range definition is wrong: " + rangeString);
            }
            if (ranges[0].trim().equalsIgnoreCase(INFINITY_NEGATIVE)) {
                isRangeMinInfinity = true;
            } else {
                isRangeMinInfinity = false;
                rangeMinValue = safeConvertStrToDouble(ranges[0].trim());
            }

            if (ranges[1].trim().equalsIgnoreCase(INFINITY_POSITIVE)) {
                isRangeMaxInfinity = true;
            } else {
                isRangeMaxInfinity = false;
                rangeMaxValue = safeConvertStrToDouble(ranges[1].trim());
            }
        }
    }

    public Boolean isRangeMinInfinity() {
        return isRangeMinInfinity;
    }

    public void setIsRangeMinInfinity(Boolean isRangeMinInfinity) {
        this.isRangeMinInfinity = isRangeMinInfinity;
    }

    public Boolean isRangeMaxInfinity() {
        return isRangeMaxInfinity;
    }

    public void setIsRangeMaxInfinity(Boolean isRangeMaxInfinity) {
        this.isRangeMaxInfinity = isRangeMaxInfinity;
    }

    public Double getRangeMinValue() {
        return rangeMinValue;
    }

    public void setRangeMinValue(Double rangeMinValue) {
        this.rangeMinValue = rangeMinValue;
    }

    public Double getRangeMaxValue() {
        return rangeMaxValue;
    }

    public void setRangeMaxValue(Double rangeMaxValue) {
        this.rangeMaxValue = rangeMaxValue;
    }

    public RangeType getRangeMinType() {
        return rangeMinType;
    }

    public void setRangeMinType(RangeType rangeMinType) {
        this.rangeMinType = rangeMinType;
    }

    public RangeType getRangeMaxType() {
        return rangeMaxType;
    }

    public void setRangeMaxType(RangeType rangeMaxType) {
        this.rangeMaxType = rangeMaxType;
    }

    @Override
    public String toString() {

        String range = "";
        switch (rangeMinType) {
            case RANGE_MIN_INCLUDE:
                range += "[";
                break;
            case RANGE_MIN_EXCLUDE:
                range += "(";
                break;
        }

        if (isRangeMinInfinity()) {
            range += INFINITY_NEGATIVE;
        } else {
            range += rangeMinValue;
        }

        range += ", ";
        if (isRangeMaxInfinity()) {
            range += INFINITY_POSITIVE;
        } else {
            range += rangeMaxValue;
        }

        switch (rangeMaxType) {
            case RANGE_MAX_INCLUDE:
                range += "]";
                break;
            case RANGE_MAX_EXCLUDE:
                range += ")";
                break;
        }

        return range;
    }

    //TODO: consider use parseDouble() directly
    private Double safeConvertStrToDouble(String doubleStr) {
        Double number = null;
        try {
            number = Double.parseDouble(doubleStr);
        } catch (NumberFormatException e) {
            // ignore;
        }

        return number;
    }

}
