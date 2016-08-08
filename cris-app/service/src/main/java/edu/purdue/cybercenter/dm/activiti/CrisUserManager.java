/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.activiti;

import java.util.ArrayList;
import java.util.List;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author xu222
 */
public class CrisUserManager extends UserEntityManager {

    @Override
    public User createNewUser(String userId) {
        throw new ActivitiException("Cris user manager doesn't support creating a new user");
    }

    @Override
    public void insertUser(User user) {
        throw new ActivitiException("Cris user manager doesn't support inserting a new user");
    }

    @Override
    public void updateUser(User updatedUser) {
        throw new ActivitiException("Cris user manager doesn't support updating a user");
    }

    @Override
    public void deleteUser(String userId) {
        throw new ActivitiException("Cris user manager doesn't support deleting a user");
    }

    @Override
    public UserEntity findUserById(String username) {
        UserEntity userEntity = new UserEntity();
        edu.purdue.cybercenter.dm.domain.User user = edu.purdue.cybercenter.dm.domain.User.findByUsername(username);
        userEntity.setId(username);
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        userEntity.setRevision(1);
        return userEntity;
    }

    public List<User> findUserByQueryCriteria(Object query, Page page) {
        List<User> userList = new ArrayList<>();
        UserQueryImpl userQuery = (UserQueryImpl) query;

        if (StringUtils.isNotEmpty(userQuery.getId())) {
            userList.add(findUserById(userQuery.getId()));
            return userList;
        } else if (StringUtils.isNotEmpty(userQuery.getLastName())) {
            userList.add(findUserById(userQuery.getLastName()));
            return userList;
        } else {
            //TODO: get all users from your identity domain and convert them to List<User>
            return null;
        } //TODO: you can add other search criteria that will allow extended support using the Activiti engine API

    }

    public long findUserCountByQueryCriteria(Object query) {
        return findUserByQueryCriteria(query, null).size();
    }

}
