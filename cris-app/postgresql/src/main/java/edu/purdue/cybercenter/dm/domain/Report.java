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

@Filters(
        { @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "report")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAssetListener.class})
@Configurable
public class Report extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @Column(name = "uuid")
    @Type(type = "pg-uuid")
    private UUID uuid;

    @Override
    public String toString() {
        return this.getName();
    }

    @PrePersist
    void prePersist() {
        this.setAssetTypeId(EnumAssetType.Report.getIndex());
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

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Column(name = "version_number")
    @NotNull
    private Integer versionNumber;

    @Column(name = "email", length = 250)
    private String email;

    @Column(name = "key", length = 250)
    private String key;

    @Column(name = "content")
    @NotNull
    private String content;

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static long countReports() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Report o", Long.class).getSingleResult();
    }

    public static List<Report> findAllReports() {
        return entityManager().createQuery("SELECT o FROM Report o", Report.class).getResultList();
    }

    public static Report findReport(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Report.class, id);
    }

    public static List<Report> findReportEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Report o", Report.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
