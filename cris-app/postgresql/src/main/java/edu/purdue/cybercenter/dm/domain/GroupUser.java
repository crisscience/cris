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
@Table(schema = "public", name = "group_user")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class GroupUser extends AbstractCrisEntity {
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id", nullable = false)
    private Group groupId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User userId;

    public Group getGroupId() {
        return groupId;
    }

    public void setGroupId(Group groupId) {
        this.groupId = groupId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    /*********************************************************
     * Query
     * @param gid
     * @param uid
     * @return
     *********************************************************/
    public static GroupUser findGroupUser(Integer gid, Integer uid) {
        Group groupId = Group.findGroup(gid);
        User userId = User.findUser(uid);
        return entityManager().createQuery("select o from GroupUser o where o.groupId = :groupId and o.userId = :userId", GroupUser.class).setParameter("groupId", groupId).setParameter("userId", userId).getSingleResult();
    }

    public static long countGroupUsers() {
        return entityManager().createQuery("SELECT COUNT(o) FROM GroupUser o", Long.class).getSingleResult();
    }

    public static List<GroupUser> findAllGroupUsers() {
        return entityManager().createQuery("SELECT o FROM GroupUser o", GroupUser.class).getResultList();
    }

    public static GroupUser findGroupUser(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(GroupUser.class, id);
    }

    public static List<GroupUser> findGroupUserEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM GroupUser o", GroupUser.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
