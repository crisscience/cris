/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.repository;

import com.mysema.codegen.StringUtils;
import com.mysema.query.jpa.JPQLQuery;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.querydsl.EntityPathResolver;

public class CrisRepositoryImpl<T> extends QueryDslJpaRepository<T, Integer> {

    public CrisRepositoryImpl(JpaEntityInformation<T, Integer> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    public CrisRepositoryImpl(JpaEntityInformation<T, Integer> entityInformation, EntityManager entityManager, EntityPathResolver resolver) {
        super(entityInformation, entityManager, resolver);
    }

    @Override
    protected JPQLQuery createQuery(Predicate... predicate) {
        Predicate filter = createFilter();
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(filter);
        predicates.addAll(Arrays.asList(predicate));
        JPQLQuery query = super.createQuery(predicates.toArray(new Predicate[predicates.size()]));
        return query;
    }

    private Predicate createFilter() {
        Integer tenantId = edu.purdue.cybercenter.dm.threadlocal.TenantId.get();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        if (tenantId == null) {
            throw new RuntimeException("Invalid tenant");
        }
        if (userId == null) {
            throw new RuntimeException("Invalid user");
        }

        Class<T> clazz = super.getDomainClass();
        PathBuilder<T> entityPath = new PathBuilder<>(clazz, StringUtils.uncapitalize(clazz.getSimpleName()));
        NumberPath<Integer> tenantIdPath = entityPath.getNumber("tenantId", Integer.class);
        Predicate tenantFilter = tenantIdPath.eq(tenantId);

        return tenantFilter;
    }
}
