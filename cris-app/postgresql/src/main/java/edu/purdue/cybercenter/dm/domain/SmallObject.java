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
@Table(schema = "public", name = "small_object")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAsset.class})
@Configurable
public class SmallObject extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @PrePersist
    void prePersist() {
        setAssetTypeId(EnumAssetType.SmallObject.getIndex());
    }

    @Column(name = "filename", length = 250)
    private String filename;

    @Column(name = "content")
    @NotNull
    private byte[] content;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /*********************************************************
     * query
     * @return
     *********************************************************/
    public static long countSmallObjects() {
        return entityManager().createQuery("SELECT COUNT(o) FROM SmallObject o", Long.class).getSingleResult();
    }

    public static List<SmallObject> findAllSmallObjects() {
        return entityManager().createQuery("SELECT o FROM SmallObject o", SmallObject.class).getResultList();
    }

    public static SmallObject findSmallObject(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(SmallObject.class, id);
    }

    public static List<SmallObject> findSmallObjectEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM SmallObject o", SmallObject.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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

}
