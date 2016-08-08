package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "permission")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class Permission extends AbstractCrisEntity {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /*********************************************************
     * Query
     * @param item
     * @return
     *********************************************************/
    public static Permission findItem(Permission item) {
        EntityManager em = entityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Permission> cq = cb.createQuery(Permission.class);
        Root<Permission> root = cq.from(Permission.class);

        Expression<Group> groupId = (Expression<Group>) root.<Group>get("groupId");
        Predicate whereGroupId;
        if (item.getGroupId() != null) {
            whereGroupId = cb.equal(groupId, item.getGroupId());
        } else {
            whereGroupId = cb.isNull(groupId);
        }

        Expression<User> userId = (Expression<User>) root.<User>get("userId");
        Predicate whereUserId;
        if (item.getUserId() != null) {
            whereUserId = cb.equal(userId, item.getUserId());
        } else {
            whereUserId = cb.isNull(userId);
        }

        Expression<Role> roleId = (Expression<Role>) root.<Role>get("roleId");
        Predicate whereRoleId;
        if (item.getRoleId() != null) {
            whereRoleId = cb.equal(roleId, item.getRoleId());
        } else {
            whereRoleId = cb.isNull(roleId);
        }

        Expression<EnumOperation> operationId = (Expression<EnumOperation>) root.<EnumOperation>get("operationId");
        Predicate whereOperationId;
        if (item.getOperationId() != null) {
            whereOperationId = cb.equal(operationId, item.getOperationId());
        } else {
            whereOperationId = cb.isNull(operationId);
        }

        /*
        Expression<Asset> assetId = (Expression<Asset>) root.<Asset>get("assetId");
        Predicate whereAsset;
        if (item.getAssetId() != null) {
            whereAsset = cb.equal(assetId, item.getAssetId());
        } else {
            whereAsset = cb.isNull(assetId);
        }
        *
        */

        cq.where(whereGroupId, whereUserId, whereRoleId, whereOperationId/*, whereAsset*/);

        List<Permission> permissions = em.createQuery(cq).setMaxResults(1).getResultList();

        return permissions.isEmpty() ? null : permissions.get(0);
    }

    public static List<Permission> findRoleTypeId(User user) {
        List<Permission> permissions = findRoleTypeIdByUser(user);

        if (permissions.isEmpty()) {
            List<Group> groups = user.getMemberGroups();
            if (groups != null) {
                for (Group group : groups) {
                    List<Permission> gPermissions = Permission.findRoleTypeIdByGroup(group);
                    permissions.addAll(gPermissions);
                }
            }
        }

        return permissions;
    }

    public static List<Permission> findRoleTypeIdByUser(User user) {
        List<Permission> permissions = entityManager().createQuery("select o from Permission o where o.userId = :userId and o.roleTypeId is not null", Permission.class).setParameter("userId", user).getResultList();

        return permissions;
    }

    public static List<Permission> findRoleTypeIdByGroup(Group group) {
        List<Permission> permissions = entityManager().createQuery("select o from Permission o where o.groupId = :groupId and o.roleTypeId is not null", Permission.class).setParameter("groupId", group).getResultList();

        return permissions;
    }

    public static List<Permission> findByUser(User user) {
        List<Permission> permissions = entityManager().createQuery("select o from Permission o where o.userId = :userId", Permission.class).setParameter("userId", user).getResultList();

        return permissions;
    }

    /*********************************************************
     * Json
     * @param ctxPath
     * @return
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(Group.class, fjr);
        useClasses.put(Role.class, fjr);
        useClasses.put(EnumRoleType.class, fjr);
        useClasses.put(EnumOperation.class, fjr);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    public static long countPermissions() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Permission o", Long.class).getSingleResult();
    }

    public static List<Permission> findAllPermissions() {
        return entityManager().createQuery("SELECT o FROM Permission o", Permission.class).getResultList();
    }

    public static Permission findPermission(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Permission.class, id);
    }

    public static List<Permission> findPermissionEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Permission o", Permission.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @ManyToOne
    @JoinColumn(name = "operation_id", referencedColumnName = "id")
    private EnumOperation operationId;

    @ManyToOne
    @JoinColumn(name = "role_type_id", referencedColumnName = "id")
    private EnumRoleType roleTypeId;

    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group groupId;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private Role roleId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User userId;

    @Column(name = "asset_type_id")
    private Integer assetTypeId;

    @Column(name = "asset_id")
    private Integer assetId;

    public Integer getAssetTypeId() {
        return assetTypeId;
    }

    public void setAssetTypeId(Integer assetTypeId) {
        this.assetTypeId = assetTypeId;
    }

    public EnumOperation getOperationId() {
        return operationId;
    }

    public void setOperationId(EnumOperation operationId) {
        this.operationId = operationId;
    }

    public EnumRoleType getRoleTypeId() {
        return roleTypeId;
    }

    public void setRoleTypeId(EnumRoleType roleTypeId) {
        this.roleTypeId = roleTypeId;
    }

    public Group getGroupId() {
        return groupId;
    }

    public void setGroupId(Group groupId) {
        this.groupId = groupId;
    }

    public Role getRoleId() {
        return roleId;
    }

    public void setRoleId(Role roleId) {
        this.roleId = roleId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public Integer getAssetId() {
        return assetId;
    }

    public void setAssetId(Integer assetId) {
        this.assetId = assetId;
    }
}
