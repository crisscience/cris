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
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "storage")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class Storage extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return this.getName();
    }

    @PrePersist
    void prePersist() {
        setAssetTypeId(EnumAssetType.Storage.getIndex());
    }

    /*********************************************************
     * Json
     * @param ctxPath
     * @return
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(StorageFile.class, fjr);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    public static long countStorages() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Storage o", Long.class).getSingleResult();
    }

    public static List<Storage> findAllStorages() {
        return entityManager().createQuery("SELECT o FROM Storage o", Storage.class).getResultList();
    }

    public static Storage findStorage(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Storage.class, id);
    }

    public static List<Storage> findStorageEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Storage o", Storage.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Column(name = "type", length = 256)
    @NotNull
    private String type;

    @Column(name = "location", length = 256)
    @NotNull
    private String location;

    @Column(name = "capacity")
    private Integer capacity;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
