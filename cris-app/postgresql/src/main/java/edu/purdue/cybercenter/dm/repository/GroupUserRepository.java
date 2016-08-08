/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.repository;

import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.GroupUser;
import edu.purdue.cybercenter.dm.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 *
 * @author xu222
 */
@Repository
public interface GroupUserRepository extends JpaRepository<GroupUser, Integer>, QueryDslPredicateExecutor<GroupUser> {

    GroupUser findByGroupIdAndUserId(Group groupId, User userId);
}
