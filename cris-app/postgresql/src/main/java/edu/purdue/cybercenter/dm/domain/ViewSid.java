package edu.purdue.cybercenter.dm.domain;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Filters({
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "view_sid")
@Configurable
public class ViewSid implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "sid", unique = true, columnDefinition = "varchar", length = 256, insertable = false, updatable = false)
    private String sid;

    @Override
    public String toString() {
        return this.getSid();
    }

    public boolean isUser() {
        return sid.startsWith("U");
    }

    public boolean isGroup() {
        return sid.startsWith("G");
    }

    public User toUser() {
        if (sid.startsWith("U")) {
            Integer id = Integer.parseInt(sid.substring(1));
            return User.findUser(id);
        }

        return null;
    }

    public Group toGroup() {
        if (sid.startsWith("G")) {
            Integer id = Integer.parseInt(sid.substring(1));
            return Group.findGroup(id);
        }

        return null;
    }

    public static String toSid(User user) {
        return "U" + user.getId();
    }

    public static String toSid(Group group) {
        return "G" + group.getId();
    }

    public static ViewSid toViewSid(User user) {
        return ViewSid.findViewSid(toSid(user));
    }

    public static ViewSid toViewSid(Group group) {
        return ViewSid.findViewSid(toSid(group));
    }

    @PersistenceContext
    transient EntityManager entityManager;

    public static final EntityManager entityManager() {
        EntityManager em = new ViewSid().entityManager;
        if (em == null) {
            throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        }
        return em;
    }

    public static long countViewSids() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ViewSid o", Long.class).getSingleResult();
    }

    public static List<ViewSid> findAllViewSids() {
        return entityManager().createQuery("SELECT o FROM ViewSid o", ViewSid.class).getResultList();
    }

    public static ViewSid findViewSid(String sid) {
        if (sid == null) {
            return null;
        }
        return entityManager().find(ViewSid.class, sid);
    }

    public static List<ViewSid> findViewSidEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ViewSid o", ViewSid.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Transactional
    public void refresh() {
        if (this.entityManager == null) {
            this.entityManager = entityManager();
        }
        this.entityManager.refresh(this);
    }

    public String getSid() {
        return this.sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}
