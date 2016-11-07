package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisAssetListener;
import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.EnumAssetType;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "timeBetweenFilter"),
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "shortcut")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAssetListener.class})
@Configurable
public class Shortcut extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @Column(name = "uuid")
    @Type(type = "pg-uuid")
    private UUID uuid;

    @Column(name = "version_number")
    @Type(type = "pg-uuid")
    private UUID versionNumber;


    @Override
    public String toString() {
        return this.getName();
    }

    @PrePersist
    void prePersist() {
        if (this.getUuid() == null) {
            this.setUuid(UUID.randomUUID());
        }

        setAssetTypeId(EnumAssetType.Shortcut.getIndex());
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

    @Column(name = "url", length = 500)
    @NotNull
    private String url;

    @Column(name = "parameters", length = 1500)
    private String parameters;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(UUID versionNumber) {
        this.versionNumber = versionNumber;
    }

    public static long countShortcuts() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Shortcut o", Long.class).getSingleResult();
    }

    public static List<Shortcut> findAllShortcuts() {
        return entityManager().createQuery("SELECT o FROM Shortcut o", Shortcut.class).getResultList();
    }

    public static Shortcut findShortcut(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Shortcut.class, id);
    }

    public static List<Shortcut> findShortcutEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Shortcut o", Shortcut.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
