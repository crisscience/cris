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
@Table(schema = "public", name = "computational_node")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAssetListener.class})
@Configurable
public class ComputationalNode extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @PrePersist
    void prePersist() {
        setAssetTypeId(EnumAssetType.ComputationalNode.getIndex());
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

    @Column(name = "type", length = 256)
    @NotNull
    private String type;

    @Column(name = "ip_address", length = 256)
    @NotNull
    private String ipAddress;

    @Column(name = "location", length = 256)
    private String location;

    @Column(name = "capacity", length = 256)
    private String capacity;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public static long countComputationalNodes() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ComputationalNode o", Long.class).getSingleResult();
    }

    public static List<ComputationalNode> findAllComputationalNodes() {
        return entityManager().createQuery("SELECT o FROM ComputationalNode o", ComputationalNode.class).getResultList();
    }

    public static ComputationalNode findComputationalNode(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(ComputationalNode.class, id);
    }

    public static List<ComputationalNode> findComputationalNodeEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ComputationalNode o", ComputationalNode.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

}
