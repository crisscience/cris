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
@Table(schema = "public",name = "enum_role_type")
@Audited
public class EnumRoleType extends AbstractCrisEntity{
	private static final long serialVersionUID = 1L;

    @OneToMany(mappedBy = "roleTypeId", cascade = CascadeType.REMOVE)
    private Set<Permission> permissions;

    @OneToMany(mappedBy = "roleTypeId", cascade = CascadeType.REMOVE)
    private Set<Role> roles;

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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
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

    public static long countEnumRoleTypes() {
        return entityManager().createQuery("SELECT COUNT(o) FROM EnumRoleType o", Long.class).getSingleResult();
    }

    public static List<EnumRoleType> findAllEnumRoleTypes() {
        return entityManager().createQuery("SELECT o FROM EnumRoleType o", EnumRoleType.class).getResultList();
    }

    public static EnumRoleType findEnumRoleType(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(EnumRoleType.class, id);
    }

    public static List<EnumRoleType> findEnumRoleTypeEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM EnumRoleType o", EnumRoleType.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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

    @Override
    public String toString() {
        return this.getName();
    }

}
