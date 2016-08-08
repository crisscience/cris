package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
@Table(schema = "public", name = "role")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class Role extends AbstractCrisEntity {
    private static final long serialVersionUID = 1L;

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
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    @OneToMany(mappedBy = "roleId", cascade = CascadeType.REMOVE)
    private Set<Permission> permissions;

    @OneToMany(mappedBy = "roleId", cascade = CascadeType.REMOVE)
    private Set<RoleOperation> roleOperations;

    @ManyToOne
    @JoinColumn(name = "role_type_id", referencedColumnName = "id", nullable = false)
    private EnumRoleType roleTypeId;

    @Column(name = "name", length = 100, unique = true)
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

    public EnumRoleType getRoleTypeId() {
        return roleTypeId;
    }

    public void setRoleTypeId(EnumRoleType roleTypeId) {
        this.roleTypeId = roleTypeId;
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

    public static long countRoles() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Role o", Long.class).getSingleResult();
    }

    public static List<Role> findAllRoles() {
        return entityManager().createQuery("SELECT o FROM Role o", Role.class).getResultList();
    }

    public static Role findRole(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Role.class, id);
    }

    public static List<Role> findRoleEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Role o", Role.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
