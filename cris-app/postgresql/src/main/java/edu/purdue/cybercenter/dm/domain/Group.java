package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "group")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class Group extends AbstractCrisEntity {
    private static final long serialVersionUID = 1L;

    @Column(name = "name", length = 100, unique = true)
    @NotNull
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "enabled")
    @NotNull
    private boolean enabled;

    @ManyToOne
    @JoinColumn(name = "classification_id", referencedColumnName = "id")
    private Classification classificationId;

    @ManyToOne
    @JoinColumn(name = "image_id", referencedColumnName = "id")
    private SmallObject imageId;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User ownerId;

    @ManyToMany(mappedBy = "memberGroups")
    @NotAudited
    private Set<User> users;

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        if (this.users == null) {
            this.users = new HashSet<>();
        }
        this.users.add(user);
    }

    @Column(name = "is_group_owner")
    private Boolean isGroupOwner;

    public Boolean getIsGroupOwner() {
        return isGroupOwner;
    }

    public void setIsGroupOwner(Boolean isGroupOwner) {
        this.isGroupOwner = isGroupOwner;
    }

    public Classification getClassificationId() {
        return classificationId;
    }

    public void setClassificationId(Classification classificationId) {
        this.classificationId = classificationId;
    }

    public SmallObject getImageId() {
        return imageId;
    }

    public void setImageId(SmallObject imageId) {
        this.imageId = imageId;
    }

    public User getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(User ownerId) {
        this.ownerId = ownerId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean isAdmin() {
        Boolean isAdmin = false;

        if (this.getName().equals("Admin Group")) {
            isAdmin = true;
        }

        return isAdmin;
    }

    /*********************************************************
     * Query
     * @param id
     * @return
     *********************************************************/
    public static List<Group> findGroupsByUserId(Integer id) {
        List<Group> groups;
        groups = entityManager().createQuery("select g from Group g, GroupUser gu where g.id=gu.groupId.id and gu.userId.id=:id", Group.class).setParameter("id", id).getResultList();
        return groups;
    }

    public Boolean isMember(User user) {
        List<GroupUser> groupUsers = entityManager().createQuery("select gu from GroupUser gu where gu.groupId = :group and gu.userId = :user", GroupUser.class).setParameter("group", this).setParameter("user", user).getResultList();

        if (groupUsers.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public static TypedQuery<Group> findGroupsByOwnerId(User ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("The ownerId argument is required");
        }
        EntityManager em = Group.entityManager();
        TypedQuery<Group> q = em.createQuery("SELECT o FROM Group AS o WHERE o.ownerId = :ownerId", Group.class);
        q.setParameter("ownerId", ownerId);
        return q;
    }

    public static long countGroups() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Group o", Long.class).getSingleResult();
    }

    public static List<Group> findAllGroups() {
        return entityManager().createQuery("SELECT o FROM Group o", Group.class).getResultList();
    }

    public static Group findGroup(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Group.class, id);
    }

    public static List<Group> findGroupEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Group o", Group.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    /*********************************************************
     * Json
     * @param ctxPath
     * @return
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(Classification.class, fjr);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    @Override
    public String toString() {
        return this.getName();
    }

}
