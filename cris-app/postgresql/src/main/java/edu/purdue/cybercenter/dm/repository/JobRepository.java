/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.repository;

import edu.purdue.cybercenter.dm.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 *
 * @author xu222
 */
public interface JobRepository extends JpaRepository<Job, Integer>, QueryDslPredicateExecutor<Job> {

}
