/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.vocabulary.validators;

/**
 *
 * @author xu222
 */
public interface EnumProperty {

    public String getId();

    public String getName();

    public Class getType();

    public Object getValue();

    public Boolean isRequired();

}
