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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "myToolsFilter"),
    @Filter(name = "timeBetweenFilter"),
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "tool")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAssetListener.class})
@Configurable
public class Tool extends AbstractCrisAsset {

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
        setAssetTypeId(EnumAssetType.Tool.getIndex());

        if (this.getKey() == null) {
            this.setKey(this.getName());
        }

        if (this.getUuid() == null) {
            this.setUuid(UUID.randomUUID());
        }
        if (this.getVersionNumber() == null) {
            this.setVersionNumber(UUID.randomUUID());
        }
    }

    /*********************************************************
     * Json
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(Term.class, fjr);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    public static long countTools() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Tool o", Long.class).getSingleResult();
    }

    public static List<Tool> findAllTools() {
        return entityManager().createQuery("SELECT o FROM Tool o", Tool.class).getResultList();
    }

    public static Tool findTool(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Tool.class, id);
    }

    public static List<Tool> findToolEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Tool o", Tool.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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

    @ManyToOne
    @JoinColumn(name = "template_id", referencedColumnName = "id")
    private Term templateId;

    @Column(name = "key", length = 250, unique = true)
    private String key;

    @Column(name = "content")
    private String content;

    @Column(name = "email", length = 250)
    private String email;

    @Column(name = "pre_filter")
    private String preFilter;

    @Column(name = "command_line")
    private String commandLine;

    @Column(name = "post_filter")
    private String postFilter;

    public Term getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Term templateId) {
        this.templateId = templateId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPreFilter() {
        return preFilter;
    }

    public void setPreFilter(String preFilter) {
        this.preFilter = preFilter;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public String getPostFilter() {
        return postFilter;
    }

    public void setPostFilter(String postFilter) {
        this.postFilter = postFilter;
    }
}
