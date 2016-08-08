package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.ArrayList;
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
    @Filter(name = "enabledFilter"),
    @Filter(name = "userInGroupFilter"),
    @Filter(name = "userNotInGroupFilter"),
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "user")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class User extends AbstractCrisEntity {
    private static final long serialVersionUID = 1L;

    @Column(name = "external_source", length = 100, unique = true)
    private String externalSource;

    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    @Column(name = "username", length = 100, unique = true)
    @NotNull
    private String username;

    @Column(name = "password", length = 100)
    @NotNull
    private String password;

    @Column(name = "salt", length = 100)
    @NotNull
    private String salt;

    @Column(name = "first_name", length = 100)
    @NotNull
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", length = 100)
    @NotNull
    private String lastName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "account_non_expired")
    @NotNull
    private boolean accountNonExpired;

    @Column(name = "account_non_locked")
    @NotNull
    private boolean accountNonLocked;

    @Column(name = "credentials_non_expired")
    @NotNull
    private boolean credentialsNonExpired;

    @Column(name = "enabled")
    @NotNull
    private boolean enabled;

    @ManyToOne
    @JoinColumn(name = "image_id", referencedColumnName = "id")
    private SmallObject imageId;

    @OneToMany(mappedBy = "ownerId")
    private Set<Group> groups;

    @OneToMany(mappedBy = "userId", cascade = CascadeType.REMOVE)
    private Set<GroupUser> groupUsers;

    public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public Set<GroupUser> getGroupUsers() {
        return groupUsers;
    }

    public void setGroupUsers(Set<GroupUser> groupUsers) {
        this.groupUsers = groupUsers;
    }

    public SmallObject getImageId() {
        return imageId;
    }

    public void setImageId(SmallObject imageId) {
        this.imageId = imageId;
    }

    public String getExternalSource() {
        return externalSource;
    }

    public void setExternalSource(String externalSource) {
        this.externalSource = externalSource;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static boolean isExist(String username) {
        return findByUsername(username) != null;
    }

    public Boolean isAdmin() {
        Boolean isAdmin = false;

        List<Group> groups = Group.findGroupsByUserId(this.getId());
        for (Group group : groups) {
            if (group.getName().equals("Admin Group")) {
                isAdmin = true;
                break;
            }
        }
        return isAdmin;
    }

    public List<Group> getMemberGroups() {
        List<Group> memberGroups = new ArrayList<>();

        Set<GroupUser> groupUsers = this.getGroupUsers();
        if (groupUsers != null) {
            for (GroupUser groupUser : groupUsers) {
                memberGroups.add(groupUser.getGroupId());
            }
        }

        return memberGroups;
    }

    /*********************************************************
     * Query
     * @param username
     * @return
     *********************************************************/
    public static User findByUsername(String username) {
        User user;
        try {
            user = entityManager().createQuery("select o from User o where o.username=:username", User.class).setParameter("username", username).getSingleResult();
        } catch (Exception e) {
            user = null;
        }
        return user;
    }

    public static User findByEmail(String email) {
        User user;
        try {
            user = entityManager().createQuery("select o from User o where o.email=:email", User.class).setParameter("email", email).getSingleResult();
        } catch (Exception e) {
            user = null;
        }
        return user;
    }

    public static User findByExternal(String source, String id) {
        User user;
        try {
            user = entityManager().createQuery("select o from User o where o.externalSource=:source and o.externalId = :id", User.class).setParameter("source", source).setParameter("id", id).getSingleResult();
        } catch (Exception e) {
            user = null;
        }
        return user;
    }

    public static long countUsers() {
        return entityManager().createQuery("SELECT COUNT(o) FROM User o", Long.class).getSingleResult();
    }

    public static List<User> findAllUsers() {
        return entityManager().createQuery("SELECT o FROM User o", User.class).getResultList();
    }

    public static User findUser(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(User.class, id);
    }

    public static List<User> findUserEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM User o", User.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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

    @Override
    public String toString() {
        return this.getUsername();
    }

}
