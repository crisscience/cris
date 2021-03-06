package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisAssetListener;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "storageFileFilter"),
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "storage_file")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAssetListener.class})
@Configurable
public class StorageFile extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @PrePersist
    void prePersist() {
        setAssetTypeId(EnumAssetType.StorageFile.getIndex());
    }

    public static Integer toStorageFileId(String storageFileId) {
        String[] fields = storageFileId.split(":");
        String sId;
        if (fields.length == 1) {
            sId = fields[0];
        } else if (fields.length == 2) {
            sId = fields[1];
        } else {
            throw new RuntimeException("Invalid StorageFile ID: " + storageFileId);
        }

        Integer id;
        id = Integer.parseInt(sId);
        return id;
    }

    /*********************************************************
     * Json
     * @param ctxPath
     * @return
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(Storage.class, fjr);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    @ManyToOne
    @JoinColumn(name = "storage_id", referencedColumnName = "id")
    private Storage storageId;

    @Column(name = "source", length = 256)
    private String source;

    @Column(name = "file_name", length = 256)
    private String fileName;

    @Column(name = "location", length = 256)
    @NotNull
    private String location;

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "experiment_id")
    private Integer experimentId;

    @Column(name = "job_id")
    private Integer jobId;

    public Storage getStorageId() {
        return storageId;
    }

    public void setStorageId(Storage storageId) {
        this.storageId = storageId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public static long countStorageFiles() {
        return entityManager().createQuery("SELECT COUNT(o) FROM StorageFile o", Long.class).getSingleResult();
    }

    public static List<StorageFile> findAllStorageFiles() {
        return entityManager().createQuery("SELECT o FROM StorageFile o", StorageFile.class).getResultList();
    }

    public static StorageFile findStorageFile(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(StorageFile.class, id);
    }

    public static List<StorageFile> findStorageFileEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM StorageFile o", StorageFile.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
