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
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Entity
@Table(schema = "public", name = "session")
@Audited
@Configurable
public class Session extends AbstractCrisEntity {
    private static final long serialVersionUID = 1L;

    @OneToMany(mappedBy = "sessionId", cascade = CascadeType.REMOVE)
    private Set<UserSession> userSessions;

    @Column(name = "jsessionid", length = 500)
    @NotNull
    private String jsessionid;

    @Column(name = "host")
    private String host;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "request_url")
    private String requestUrl;

    @Column(name = "referer")
    private String referer;

    public Set<UserSession> getUserSessions() {
        return userSessions;
    }

    public void setUserSessions(Set<UserSession> userSessions) {
        this.userSessions = userSessions;
    }

    public String getJsessionid() {
        return jsessionid;
    }

    public void setJsessionid(String jsessionid) {
        this.jsessionid = jsessionid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    /*********************************************************
     * Query
     * @param sessionId
     * @return
     *********************************************************/
    public static Session findBySessionId(String sessionId) {
        List<Session> sessions = entityManager().createQuery("select s from Session s where s.jsessionid=:sessionId", Session.class).setParameter("sessionId", sessionId).getResultList();
        if (!sessions.isEmpty() && sessions.size() == 1) {
            return sessions.get(0);
        } else {
            return null;
        }
    }

    public static long countSessions() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Session o", Long.class).getSingleResult();
    }

    public static List<Session> findAllSessions() {
        return entityManager().createQuery("SELECT o FROM Session o", Session.class).getResultList();
    }

    public static Session findSession(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Session.class, id);
    }

    public static List<Session> findSessionEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Session o", Session.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
