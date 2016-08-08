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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "mostRecentUpdatedVocabularyFilter"),
    @Filter(name = "assetStatusFilter"),
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "vocabulary")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAssetListener.class})
@Configurable
public class Vocabulary extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @Column(name = "uuid")
    @Type(type = "pg-uuid")
    private UUID uuid;

    @Column(name = "version_number")
    @Type(type = "pg-uuid")
    @NotNull
    private UUID versionNumber;

    @Column(name = "domain", length = 250)
    @NotNull
    private String domain;

    @Column(name = "key", length = 250)
    private String key;

    @Column(name = "content")
    @NotNull
    private String content;

    @ManyToMany
    @JoinTable(name = "vocabulary_term", joinColumns = {@JoinColumn(name = "vocabulary_id")}, inverseJoinColumns = {@JoinColumn(name = "term_id")})
    private List<Term> terms;

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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @PrePersist
    void prePersist() {
        setAssetTypeId(EnumAssetType.Vocabulary.getIndex());
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

    public static long countVocabularys() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Vocabulary o", Long.class).getSingleResult();
    }

    public static List<Vocabulary> findAllVocabularys() {
        return entityManager().createQuery("SELECT o FROM Vocabulary o", Vocabulary.class).getResultList();
    }

    public static Vocabulary findVocabulary(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Vocabulary.class, id);
    }

    public static List<Vocabulary> findVocabularyEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Vocabulary o", Vocabulary.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
