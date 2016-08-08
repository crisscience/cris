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

@Filters({
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "classification")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class Classification extends AbstractCrisEntity {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return this.getName();
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

    public static long countClassifications() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Classification o", Long.class).getSingleResult();
    }

    public static List<Classification> findAllClassifications() {
        return entityManager().createQuery("SELECT o FROM Classification o", Classification.class).getResultList();
    }

    public static Classification findClassification(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Classification.class, id);
    }

    public static List<Classification> findClassificationEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Classification o", Classification.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Column(name = "code", length = 100, unique = true)
    @NotNull
    private String code;

    @Column(name = "name", length = 100, unique = true)
    @NotNull
    private String name;

    @Column(name = "description")
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

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
}
