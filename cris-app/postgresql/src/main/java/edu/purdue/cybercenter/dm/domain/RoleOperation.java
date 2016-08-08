package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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
@Table(schema = "public", name = "role_operation")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class RoleOperation extends AbstractCrisEntity {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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

    public static long countRoleOperations() {
        return entityManager().createQuery("SELECT COUNT(o) FROM RoleOperation o", Long.class).getSingleResult();
    }

    public static List<RoleOperation> findAllRoleOperations() {
        return entityManager().createQuery("SELECT o FROM RoleOperation o", RoleOperation.class).getResultList();
    }

    public static RoleOperation findRoleOperation(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(RoleOperation.class, id);
    }

    public static List<RoleOperation> findRoleOperationEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM RoleOperation o", RoleOperation.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @ManyToOne
    @JoinColumn(name = "operation_id", referencedColumnName = "id", nullable = false)
    private EnumOperation operationId;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private Role roleId;

    public EnumOperation getOperationId() {
        return operationId;
    }

    public void setOperationId(EnumOperation operationId) {
        this.operationId = operationId;
    }

    public Role getRoleId() {
        return roleId;
    }

    public void setRoleId(Role roleId) {
        this.roleId = roleId;
    }
}
