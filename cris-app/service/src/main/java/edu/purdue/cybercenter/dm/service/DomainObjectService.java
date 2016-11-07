/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.CrisEntity;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 *
 * @author xu222
 */
@Component
public class DomainObjectService {

    static final private String POST_FILTER_RAED =    "isAdmin() or hasDomainObjectPermission(filterObject, 'read') or hasDomainObjectPermission(filterObject, 'execute')";
    static final private String POST_AUTHORIZE_READ = "isAdmin() or hasDomainObjectPermission(returnObject, 'read') or hasDomainObjectPermission(returnObject, 'execute')";
    static final private String PRE_AUTORIZE_CREATE = "isAdmin() or hasDomainObjectPermission(#item, 'create')";
    static final private String PRE_AUTORIZE_UPDATE = "isAdmin() or hasDomainObjectPermission(#item, 'write')";
    static final private String PRE_AUTORIZE_DELETE = "isAdmin() or hasDomainObjectPermission(#item, 'delete')";

    private final Validator validator;

    public DomainObjectService() {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @PostAuthorize(POST_AUTHORIZE_READ)
    public <T> T executeTypedQueryWithSingleResult(TypedQuery<T> query) {
        return query.getSingleResult();
    }

    public <T> T executeTypedQueryWithSingleResultUnsecured(TypedQuery<T> query) {
        return query.getSingleResult();
    }

    @PostFilter(POST_FILTER_RAED)
    public <T> List<T> executeTypedQueryWithResultList(TypedQuery<T> query) {
        return query.getResultList();
    }

    @PostAuthorize(POST_AUTHORIZE_READ)
    public <T> T findById(Integer id, Class<T> clazz) {
        if (id == null || clazz == null) {
            return null;
        }

        EntityManager em = DomainObjectHelper.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(clazz);
        Root<T> root = cq.from(clazz);

        cq.where(cb.equal(root.get("id"), id));

        TypedQuery<T> typedQuery = em.createQuery(cq);

        return typedQuery.getSingleResult();
    }

    @PostFilter(POST_FILTER_RAED)
    public <T> List<T> findAll(Class<T> clazz) {
        return findEntries(null, null, null, null, clazz);
    }

    @PostFilter(POST_FILTER_RAED)
    public <T> List<T> findAll(Entry<String, String> orderBy, Class<T> clazz) {
        return findEntries(null, null, orderBy, null, clazz);
    }

    @PostFilter(POST_FILTER_RAED)
    public <T> List<T> findBy(Entry<String, String> orderBy, Map<String, Object> where, Class<T> clazz) {
        return findEntries(null, null, orderBy, where, clazz);
    }

    @PostFilter(POST_FILTER_RAED)
    public <T> List<T> findBy(Map<String, Object> where, Class<T> clazz) {
        return findEntries(null, null, null, where, clazz);
    }

    @PostFilter(POST_FILTER_RAED)
    public <T> List<T> findEntries(Integer firstResult, Integer maxResults, Entry<String, String> orderBy, Map<String, Object> where, Class<T> clazz) {
        EntityManager em = DomainObjectHelper.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(clazz);
        Root<T> root = cq.from(clazz);

        List<Order> orders = new ArrayList<>();
        if (orderBy != null) {
            Order order = DomainObjectHelper.convertToJpaOrder(root, orderBy);
            if (order != null) {
                orders.add(order);
            }
        }
        // sort on the id field. this should be the last order by field:
        Order orderById = DomainObjectHelper.convertToJpaOrder(root, new AbstractMap.SimpleEntry<>("id", "asc"));
        orders.add(orderById);

        cq.orderBy(orders);

        if (where != null) {
            cq.where(DomainObjectHelper.convertToJpaWhere(root, where));
        }

        TypedQuery<T> typedQuery = em.createQuery(cq);

        if (firstResult != null) {
            typedQuery.setFirstResult(firstResult);
        }

        if (maxResults != null) {
            typedQuery.setMaxResults(maxResults);
        }

        List<T> result = typedQuery.getResultList();

        return result;
    }

    public <T> List<T> findEntriesNoPermissionCheck(Integer firstResult, Integer maxResults, Entry<String, String> orderBy, Map<String, Object> where, Class<T> clazz) {
        return findEntries(firstResult, maxResults, orderBy, where, clazz);
    }

    public <T> Long countEntries(Map<String, Object> where, Class<T> clazz) {
        EntityManager em = DomainObjectHelper.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(clazz);

        cq.select(cb.count(root));

        if (where != null) {
            cq.where(DomainObjectHelper.convertToJpaWhere(root, where));
        }

        return em.createQuery(cq).getSingleResult();
    }

    @PreAuthorize(PRE_AUTORIZE_CREATE)
    public <T extends CrisEntity> void persist(T item, Class<T> clazz) {
        item.persist();
    }

    @PreAuthorize(PRE_AUTORIZE_UPDATE)
    public <T extends CrisEntity> T merge(T item, Class<T> clazz) {
        T merged = (T) item.merge();
        return merged;
    }

    @PreAuthorize(PRE_AUTORIZE_DELETE)
    public <T extends CrisEntity> void remove(T item, Class<T> clazz) {
        item.remove();
    }

    @PreAuthorize(PRE_AUTORIZE_DELETE)
    public <T extends CrisEntity> void flush(T item, Class<T> clazz) {
        item.flush();
    }

    @PreAuthorize(PRE_AUTORIZE_DELETE)
    public <T extends CrisEntity> void clear(T item, Class<T> clazz) {
        item.clear();
    }

    public <T extends CrisEntity> Set<ConstraintViolation<T>> validate(T item) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(item);
        return constraintViolations;
    }

    public <T extends CrisEntity> String get$ref(T item, Class<T> clazz) {
        String ref = item.get$ref();
        return ref;
    }

}
