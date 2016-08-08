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
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "storage_path")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class StoragePath extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    /*********************************************************
     * Json
     * @param ctxPath
     * @return
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(StoragePath.class, fjr);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    public static long countStoragePaths() {
        return entityManager().createQuery("SELECT COUNT(o) FROM StoragePath o", Long.class).getSingleResult();
    }

    public static List<StoragePath> findAllStoragePaths() {
        return entityManager().createQuery("SELECT o FROM StoragePath o", StoragePath.class).getResultList();
    }

    public static StoragePath findStoragePath(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(StoragePath.class, id);
    }

    public static List<StoragePath> findStoragePathEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM StoragePath o", StoragePath.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Column(name = "ancestor_type")
    private String ancestorType;

    @Column(name = "ancestor_id")
    private Integer ancestorId;

    @Column(name = "decendant_type")
    private String decendantType;

    @Column(name = "decendant_id")
    private Integer decendantId;

    public String getAncestorType() {
        return ancestorType;
    }

    public void setAncestorType(String ancestorType) {
        this.ancestorType = ancestorType;
    }

    public Integer getAncestorId() {
        return ancestorId;
    }

    public void setAncestorId(Integer ancestorId) {
        this.ancestorId = ancestorId;
    }

    public String getDecendantType() {
        return decendantType;
    }

    public void setDecendantType(String decendantType) {
        this.decendantType = decendantType;
    }

    public Integer getDecendantId() {
        return decendantId;
    }

    public void setDecendantId(Integer decendantId) {
        this.decendantId = decendantId;
    }
}
