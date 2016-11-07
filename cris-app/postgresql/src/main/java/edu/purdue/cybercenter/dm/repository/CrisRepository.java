/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.repository;

import edu.purdue.cybercenter.dm.domain.AbstractCrisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 *
 * @author xu222
 * @param <T>
 */
@NoRepositoryBean
public interface CrisRepository<T extends AbstractCrisEntity> extends JpaRepository<T, Integer>, QueryDslPredicateExecutor<T> {

}
