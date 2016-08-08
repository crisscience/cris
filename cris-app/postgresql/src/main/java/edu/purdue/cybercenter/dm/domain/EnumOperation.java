package edu.purdue.cybercenter.dm.domain;

import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
@Entity
@Table(schema = "public",name = "enum_operation")
@Audited
public class EnumOperation extends AbstractCrisEntity{
    private static final long serialVersionUID = 1L;

    @OneToMany(mappedBy = "operationId", cascade = CascadeType.REMOVE)
    private Set<Permission> permissions;

    @OneToMany(mappedBy = "operationId", cascade = CascadeType.REMOVE)
    private Set<RoleOperation> roleOperations;

    @Column(name = "code")
    @NotNull
    private Integer code;

    @Column(name = "name", length = 100)
    @NotNull
    private String name;

    @Column(name = "description")
    private String description;

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<RoleOperation> getRoleOperations() {
        return roleOperations;
    }

    public void setRoleOperations(Set<RoleOperation> roleOperations) {
        this.roleOperations = roleOperations;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
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

    public static long countEnumOperations() {
        return entityManager().createQuery("SELECT COUNT(o) FROM EnumOperation o", Long.class).getSingleResult();
    }

    public static List<EnumOperation> findAllEnumOperations() {
        return entityManager().createQuery("SELECT o FROM EnumOperation o", EnumOperation.class).getResultList();
    }

    public static EnumOperation findEnumOperation(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(EnumOperation.class, id);
    }

    public static List<EnumOperation> findEnumOperationEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM EnumOperation o", EnumOperation.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Override
    public String toString() {
        return this.getName();
    }

    /*********************************************************
     * Json
     * @param ctxPath
     * @return
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        return useClasses;
    }

}
