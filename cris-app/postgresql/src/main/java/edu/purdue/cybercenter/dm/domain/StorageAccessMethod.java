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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(schema = "public", name = "storage_access_method")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class StorageAccessMethod extends AbstractCrisEntity {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return this.getName();
    }

    public static StorageAccessMethod findOneByType(String type) {
        return entityManager().createQuery("select o from StorageAccessMethod o where o.type = :type", StorageAccessMethod.class).setParameter("type", type).getSingleResult();
    }

    public static List<StorageAccessMethod> findByType(String type) {
        return entityManager().createQuery("select o from StorageAccessMethod o where o.type = :type", StorageAccessMethod.class).setParameter("type", type).getResultList();
    }

    /*********************************************************
     * Json
     * @param ctxPath
     * @return
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(StorageAccessMethod.class, fjr);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    public static long countStorageAccessMethods() {
        return entityManager().createQuery("SELECT COUNT(o) FROM StorageAccessMethod o", Long.class).getSingleResult();
    }

    public static List<StorageAccessMethod> findAllStorageAccessMethods() {
        return entityManager().createQuery("SELECT o FROM StorageAccessMethod o", StorageAccessMethod.class).getResultList();
    }

    public static StorageAccessMethod findStorageAccessMethod(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(StorageAccessMethod.class, id);
    }

    public static List<StorageAccessMethod> findStorageAccessMethodEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM StorageAccessMethod o", StorageAccessMethod.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @ManyToOne
    @JoinColumn(name = "storage_id", referencedColumnName = "id")
    private Storage storageId;

    @Column(name = "type", length = 256)
    @NotNull
    private String type;

    @Column(name = "name", length = 256)
    @NotNull
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "uri", length = 256)
    @NotNull
    private String uri;

    @Column(name = "root")
    private String root;

    public Storage getStorageId() {
        return storageId;
    }

    public void setStorageId(Storage storageId) {
        this.storageId = storageId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }
}
