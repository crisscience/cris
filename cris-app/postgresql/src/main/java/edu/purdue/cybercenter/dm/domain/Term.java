package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisAssetListener;
import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.EnumAssetType;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
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
import javax.persistence.Query;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "mostRecentUpdatedTermFilter"),
    @Filter(name = "assetStatusFilter"),
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "term")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAssetListener.class})
@Configurable
public class Term extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @Column(name = "vocabulary_uuid")
    @Type(type = "pg-uuid")
    private UUID vocabularyUuid;

    @Column(name = "uuid")
    @Type(type = "pg-uuid")
    private UUID uuid;

    @Column(name = "version_number")
    @Type(type = "pg-uuid")
    @NotNull
    private UUID versionNumber;

    @Override
    public String toString() {
        return this.getName();
    }

    @PrePersist
    void prePersist() {
        setAssetTypeId(EnumAssetType.Term.getIndex());
    }

    public static UUID getLatestVersionNumber(UUID uuid) {
        Query query = DomainObjectHelper.createNamedQuery("Term.findByUuid").setParameter("uuid", uuid).setMaxResults(1);
        UUID versionNumber;
        try {
            Term term = (Term) query.getSingleResult();
            versionNumber = term.getVersionNumber();
        } catch (Exception ex) {
            versionNumber = null;
        }
        return versionNumber;
    }

    public static Term findByUuidAndVersionNumber(String uuid, String versionNumber) {
        Term term;
        try {
            term = findByUuidAndVersionNumber(UUID.fromString(uuid), UUID.fromString(versionNumber));
        } catch (Exception ex) {
            term = null;
        }

        return term;
    }

    public static Term findByUuidAndVersionNumber(UUID uuid, UUID versionNumber) {
        Query query = DomainObjectHelper.createNamedQuery("Term.findByUuidAndVersionNumber").setParameter("uuid", uuid).setParameter("versionNumber", versionNumber).setMaxResults(1);
        List<edu.purdue.cybercenter.dm.domain.Term> terms = query.getResultList();
        Term term;
        if (terms != null && terms.size() > 0) {
            term = terms.get(0);
        } else {
            term = null;
        }

        return term;
    }

    public static List<Term> findAllTemplates() {
        Query query = DomainObjectHelper.createNamedQuery("Term.findTemplateAll");
        List<edu.purdue.cybercenter.dm.domain.Term> terms = query.getResultList();
        return terms;
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

    public UUID getVocabularyUuid() {
        return this.vocabularyUuid;
    }

    public void setVocabularyUuid(UUID vocabularyUuid) {
        this.vocabularyUuid = vocabularyUuid;
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

    public static long countTerms() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Term o", Long.class).getSingleResult();
    }

    public static List<Term> findAllTerms() {
        return entityManager().createQuery("SELECT o FROM Term o", Term.class).getResultList();
    }

    public static Term findTerm(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Term.class, id);
    }

    public static List<Term> findTermEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Term o", Term.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @ManyToOne
    @JoinColumn(name = "vocabulary_id", referencedColumnName = "id")
    private Vocabulary vocabularyId;

    @Column(name = "is_template")
    private Boolean isTemplate;

    @Column(name = "type", length = 250)
    private String type;

    @Column(name = "unit", length = 250)
    private String unit;

    @Column(name = "key", length = 250)
    private String key;

    @Column(name = "content")
    @NotNull
    private String content;

    public Vocabulary getVocabularyId() {
        return vocabularyId;
    }

    public void setVocabularyId(Vocabulary vocabularyId) {
        this.vocabularyId = vocabularyId;
    }

    public Boolean getIsTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(Boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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
}
