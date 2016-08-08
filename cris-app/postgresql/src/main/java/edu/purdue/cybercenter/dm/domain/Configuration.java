package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.dao.EmptyResultDataAccessException;

@Filters({
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "configuration")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class Configuration extends AbstractCrisEntity {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return this.getName();
    }

    /*********************************************************
     * Query
     * @param name
     * @return
     *********************************************************/
    public static String findProperty(String name) {
        String value;
        try {
            Configuration config = entityManager().createQuery("select c from Configuration c where c.name=:name", Configuration.class).setParameter("name", name).getSingleResult();
            value = config.getValueText();
        } catch (EmptyResultDataAccessException ex) {
            value = null;
        }
        return value;
    }

    /*********************************************************
     * Json
     * @param ctxPath
     * @return
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    public static long countConfigurations() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Configuration o", Long.class).getSingleResult();
    }

    public static List<Configuration> findAllConfigurations() {
        return entityManager().createQuery("SELECT o FROM Configuration o", Configuration.class).getResultList();
    }

    public static Configuration findConfiguration(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Configuration.class, id);
    }

    public static List<Configuration> findConfigurationEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Configuration o", Configuration.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Column(name = "name", length = 100)
    @NotNull
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "type", length = 100)
    @NotNull
    private String type;

    @Column(name = "value_text")
    private String valueText;

    @Column(name = "value_integer")
    private Integer valueInteger;

    @Column(name = "value_bool")
    private Boolean valueBool;

    @Column(name = "value_binary")
    private byte[] valueBinary;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValueText() {
        return valueText;
    }

    public void setValueText(String valueText) {
        this.valueText = valueText;
    }

    public Integer getValueInteger() {
        return valueInteger;
    }

    public void setValueInteger(Integer valueInteger) {
        this.valueInteger = valueInteger;
    }

    public Boolean getValueBool() {
        return valueBool;
    }

    public void setValueBool(Boolean valueBool) {
        this.valueBool = valueBool;
    }

    public byte[] getValueBinary() {
        return valueBinary;
    }

    public void setValueBinary(byte[] valueBinary) {
        this.valueBinary = valueBinary;
    }

}
