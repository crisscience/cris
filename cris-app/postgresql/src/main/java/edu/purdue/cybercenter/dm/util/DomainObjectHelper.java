/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import edu.purdue.cybercenter.dm.domain.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.hibernate.Session;

/**
 *
 * @author xu222
 */
public class DomainObjectHelper {

    public static final String DOJO_FILTER_OP = "op";
    public static final String DOJO_FILTER_DATA = "data";
    public static final String DOJO_FILTER_IS_COL = "isCol";

    public static EntityManager getEntityManager() {
        return User.entityManager();
    }

    public static Session getHbmSession() {
        EntityManager em = User.entityManager();
        return (Session) em.getDelegate();
    }

    public static Query createNamedQuery(String queryName) {
        EntityManager em = DomainObjectHelper.getEntityManager();
        return em.createNamedQuery(queryName);
    }

    public static <T> TypedQuery<T> createNamedQuery(String queryName, Class<T> clazz) {
        EntityManager em = DomainObjectHelper.getEntityManager();
        return em.createNamedQuery(queryName, clazz);
    }

    public static Order convertToJpaOrder(Root root, Entry<String, String> orderBy) {
        Order order = null;

        if (orderBy != null) {
            Path path = getJpaPath(orderBy.getKey(), root);
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            if ("asc".equals(orderBy.getValue())) {
                order = cb.asc(path);
            } else {
                order = cb.desc(path);
            }
        }

        return order;
    }

    public static List<Order> convertToJpaOrders(Root root, List<Entry<String, String>> orderBys) {
        List<Order> orders = new ArrayList<>();
        orderBys.stream().forEach((orderBy) -> {
            orders.add(convertToJpaOrder(root, orderBy));
        });
        return orders;
    }

    public static Predicate convertToJpaWhere(Root root, Map<String, Object> where) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        return convertToJpaWhere(root, cb, where);
    }

    private static Path getJpaPath(String strProperties, Root root) {
        String[] properties = strProperties.split("\\.");
        Path path = root;

        Path jpaPath;
        switch (properties.length) {
            case 0:
                jpaPath = path;
                break;
            case 1:
                jpaPath = path.get(properties[0]);
                break;
            default:
                Join join = root.join(properties[0], JoinType.LEFT);
                for (int i = 1; i < properties.length - 1; i++) {
                    join = join.join(properties[i], JoinType.LEFT);
                }
                jpaPath = join.get(properties[properties.length - 1]);
                break;
        }

        return jpaPath;
    }

    private static Object convertValue(Map<String, Object> data) {
        if (data == null) {
            return null;
        }

        EnumDojoOperator typeOp = EnumDojoOperator.toTypeOp((String) data.get(DOJO_FILTER_OP));
        Object object = data.get(DOJO_FILTER_DATA);
        Object value;
        if (typeOp!= null) {
            switch (typeOp) {
                case typeBoolean:
                    value = object;
                    break;
                case typeNumber:
                    value = object;
                    break;
                case typeUuid:
                    value = UUID.fromString((String) object);
                    break;
                case typeDate:
                    value = new Date((Long) object);
                    break;
                default:
                    // default to itself
                    value = object;
                    break;
            }
        } else {
            value = object;
        }

        return value;
    }

    private static Predicate convertToJpaWhere(Root root, CriteriaBuilder cb, Map<String, Object> where) {
        Predicate predicate = null;

        if (where == null) {
            return predicate;
        }

        EnumDojoOperator op = EnumDojoOperator.valueOf((String) where.get(DOJO_FILTER_OP));
        if (op == null) {
            return predicate;
        }

        if (op.isLogicalOp()) {
            // logical
            predicate = convertLogicalOp(root, cb, where);
        } else if (op.isRelationalOp()) {
            // relational
            predicate = convertRelationalOp(root, cb, where);
        } else {
            // data type: data types are processed inside relational operators
            // it shoul never reach here
            throw new RuntimeException("data type op at wrong place: " + where.toString());
        }

        return predicate;
    }

    private static Predicate convertLogicalOp(Root root, CriteriaBuilder cb, Map<String, Object> where) {
        Predicate predicate = null;

        if (where == null) {
            return predicate;
        }

        EnumDojoOperator op = EnumDojoOperator.valueOf((String) where.get(DOJO_FILTER_OP));
        if (op == null) {
            return predicate;
        }

        List<Map<String, Object>> operands = (List) where.get(DOJO_FILTER_DATA);
        if (operands == null || operands.isEmpty()) {
            return predicate;
        }

        Map<String, Object> left = operands.get(0);
        Predicate leftPredicate = convertToJpaWhere(root, cb, left);
        Predicate[] predicates = new Predicate[operands.size()];
        int i = 0;
        for (Map<String, Object> operand : operands) {
            predicates[i++] = (convertToJpaWhere(root, cb, operand));
        }

        switch (op) {
            case and:
            case all:
                predicate = cb.and(predicates);
                break;
            case or:
            case any:
                predicate = cb.or(predicates);
                break;
            case not:
                predicate = cb.not(leftPredicate);
                break;
            default:
                break;
        }

        return predicate;
    }

    private static Predicate convertRelationalOp(Root root, CriteriaBuilder cb, Map<String, Object> where) {
        Predicate predicate = null;

        if (where == null) {
            return predicate;
        }

        EnumDojoOperator op = EnumDojoOperator.valueOf((String) where.get(DOJO_FILTER_OP));
        if (op == null) {
            return predicate;
        }

        List<Map<String, Object>> operands = (List) where.get(DOJO_FILTER_DATA);
        Map<String, Object> left = null;
        Map<String, Object> right1 = null;
        Map<String, Object> right2 = null;
        int rightCount = 0;
        for (Map<String, Object> operand: operands) {
            if (operand.get(DOJO_FILTER_IS_COL) != null && (Boolean) operand.get(DOJO_FILTER_IS_COL)) {
                left = operand;
            } else {
                rightCount++;
            }

            if (rightCount == 1) {
                right1 = operand;
            } else if (rightCount == 2) {
                right2 = operand;
            }
        }

        if (left == null) {
            return predicate;
        }

        String fieldName = (String) left.get(DOJO_FILTER_DATA);
        Object fieldValue1 = convertValue(right1);
        Object fieldValue2 = convertValue(right2);

        Path path = getJpaPath(fieldName, root);
        switch (op) {
            case contains:
                predicate = cb.like(path, "%" + (String) fieldValue1 + "%");
                break;
            case startsWith:
                predicate = cb.like(path, "" + fieldValue1 + "%");
                break;
            case endsWith:
                predicate = cb.like(path, "%" + fieldValue1 + "");
                break;
            case notContains:
                predicate = cb.notLike(path, "%" + fieldValue1 + "%");
                break;
            case notStartsWith:
                predicate = cb.notLike(path, "" + fieldValue1 + "%");
                break;
            case notEndsWith:
                predicate = cb.notLike(path, "%" + fieldValue1 + "");
                break;
            case equal:
                predicate = cb.equal(path, fieldValue1);
                break;
            case notEqual:
                predicate = cb.notEqual(path, fieldValue1);
                break;
            case isEmpty:
                predicate = cb.isNull(path);
                break;
            case notEmpty:
                predicate = cb.isNotNull(path);
                break;
            case less:
                if (fieldValue1 instanceof Date) {
                    predicate = cb.lessThan(path, (Date) fieldValue1);
                } else {
                    predicate = cb.lessThan(path, (Integer) fieldValue1);
                }
                break;
            case lessEqual:
                if (fieldValue1 instanceof Date) {
                    predicate = cb.lessThanOrEqualTo(path, (Date) fieldValue1);
                } else {
                    predicate = cb.lessThanOrEqualTo(path, (Integer) fieldValue1);
                }
                break;
            case larger:
                if (fieldValue1 instanceof Date) {
                    predicate = cb.greaterThan(path, (Date) fieldValue1);
                } else {
                    predicate = cb.greaterThan(path, (Integer) fieldValue1);
                }
                break;
            case largerEqual:
                if (fieldValue1 instanceof Date) {
                    predicate = cb.greaterThanOrEqualTo(path, (Date) fieldValue1);
                } else {
                    predicate = cb.greaterThanOrEqualTo(path, (Integer) fieldValue1);
                }
                break;
            case between:
                if (fieldValue1 instanceof Date) {
                    predicate = cb.between(path, (Date) fieldValue1, (Date) fieldValue2);
                } else {
                    predicate = cb.between(path, (Integer) fieldValue1, (Integer) fieldValue2);
                }
                break;
            default:
                predicate = null;
                break;
        }

        return predicate;
    }

}
