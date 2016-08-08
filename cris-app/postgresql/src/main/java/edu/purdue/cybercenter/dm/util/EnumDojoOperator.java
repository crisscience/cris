/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author xu222
 */
public enum EnumDojoOperator {

    and(0, "and", ""),
    or(1, "or", ""),
    not(2, "not", ""),
    all(3, "all", ""),
    any(4, "any", ""),

    isEmpty(10, "isEmpty", ""),
    contains(11, "contains", ""),
    startsWith(12, "startsWith", ""),
    endsWith(13, "endsWith", ""),
    equal(14, "equal", ""),
    less(15, "less", ""),
    lessEqual(16, "lessEqual", ""),
    larger(17, "larger", ""),
    largerEqual(18, "largerEqual", ""),
    between(19, "between", ""),
    notEmpty(20, "notEmpty", ""),
    notContains(21, "notContains", ""),
    notStartsWith(22, "notStartsWith", ""),
    notEndsWith(23, "notEndsWith", ""),
    notEqual(24, "notEqual", ""),

    typeBoolean(100, "boolean", ""),
    typeNumber(101, "number", ""),
    typeString(102, "string", ""),
    typeUuid(103, "uuid", ""),
    typeDate(104, "date", "");

    private final Integer index;
    private final String name;
    private final String description;

    EnumDojoOperator(Integer index, String name, String description) {
        this.index = index;
        this.name = name;
        this.description = description;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isLogicalOp() {
        return (this.getIndex() < 10);
    }

    public boolean isRelationalOp() {
        return (this.getIndex() >= 10 || this.getIndex() < 100);
    }

    public boolean isDataTypeOp() {
        return (this.getIndex() >= 100);
    }

    public static EnumDojoOperator toTypeOp(String op) {
        EnumDojoOperator operator = EnumDojoOperator.valueOf("type" + StringUtils.capitalize(op));
        return operator;
    }

}
