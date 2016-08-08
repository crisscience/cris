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
@Table(schema = "public", name = "user_session")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class UserSession extends AbstractCrisEntity {
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "session_id", referencedColumnName = "id", nullable = false)
    private Session sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User userId;

    public Session getSessionId() {
        return sessionId;
    }

    public void setSessionId(Session sessionId) {
        this.sessionId = sessionId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public static long countUserSessions() {
        return entityManager().createQuery("SELECT COUNT(o) FROM UserSession o", Long.class).getSingleResult();
    }

    public static List<UserSession> findAllUserSessions() {
        return entityManager().createQuery("SELECT o FROM UserSession o", UserSession.class).getResultList();
    }

    public static UserSession findUserSession(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(UserSession.class, id);
    }

    public static List<UserSession> findUserSessionEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM UserSession o", UserSession.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
