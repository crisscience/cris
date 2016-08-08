package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.EnumAssetType;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "resource")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class Resource extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return this.getName();
    }

    @PrePersist
    void prePersist() {
        setAssetTypeId(EnumAssetType.Resource.getIndex());
    }

    /*********************************************************
     * Query
     *********************************************************/

    /*********************************************************
     * Json
     * @param ctxPath
     * @return
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(Group.class, fjr);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    @Column(name = "email", length = 250)
    private String email;

    @Column(name = "physical_location_label", length = 250)
    private String physicalLocationLabel;

    @Column(name = "physical_location", length = 250)
    private String physicalLocation;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhysicalLocationLabel() {
        return physicalLocationLabel;
    }

    public void setPhysicalLocationLabel(String physicalLocationLabel) {
        this.physicalLocationLabel = physicalLocationLabel;
    }

    public String getPhysicalLocation() {
        return physicalLocation;
    }

    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    public static long countResources() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Resource o", Long.class).getSingleResult();
    }

    public static List<Resource> findAllResources() {
        return entityManager().createQuery("SELECT o FROM Resource o", Resource.class).getResultList();
    }

    public static Resource findResource(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Resource.class, id);
    }

    public static List<Resource> findResourceEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Resource o", Resource.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
