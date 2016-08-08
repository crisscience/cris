/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.activiti;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author xu222
 */
public class CrisGroupManager extends GroupEntityManager {

    @Override
    public Group createNewGroup(String groupId) {
        throw new ActivitiException("Cris group manager doesn't support creating a new group");
    }

    @Override
    public void insertGroup(Group group) {
        throw new ActivitiException("Cris group manager doesn't support inserting a new group");
    }

    @Override
    public void updateGroup(Group updatedGroup) {
        throw new ActivitiException("Cris group manager doesn't support updating a group");
    }

    @Override
    public void deleteGroup(String groupId) {
        throw new ActivitiException("Cris group manager doesn't support deleting a group");
    }

    public long findGroupCountByQueryCriteria(Object query) {
        return findGroupByQueryCriteria(query, null).size();
    }

    public List<Group> findGroupByQueryCriteria(Object query, Page page) {
        List<Group> groupList = new ArrayList<>();
        GroupQueryImpl groupQuery = (GroupQueryImpl) query;
        if (StringUtils.isNotEmpty(groupQuery.getId())) {
            GroupEntity singleGroup = findGroupById(groupQuery.getId());
            groupList.add(singleGroup);
            return groupList;
        } else if (StringUtils.isNotEmpty(groupQuery.getName())) {
            GroupEntity singleGroup = findGroupById(groupQuery.getId());
            groupList.add(singleGroup);
            return groupList;
        } else if (StringUtils.isNotEmpty(groupQuery.getUserId())) {
            return findGroupsByUser(groupQuery.getUserId());
        } else {
            //TODO: get all groups from your identity domain and convert them to List<Group>
            return null;
        }
        //TODO: you can add other search criteria that will allow extended support using the Activiti engine API
    }

    public GroupEntity findGroupById(String activitiGroupId) {
        GroupEntity groupEntity = new GroupEntity();
        edu.purdue.cybercenter.dm.domain.Group group = edu.purdue.cybercenter.dm.domain.Group.findGroup(Integer.parseInt(activitiGroupId));
        groupEntity.setId(activitiGroupId);
        groupEntity.setName(group.getName());
        groupEntity.setType(activitiGroupId);
        groupEntity.setRevision(1);
        return groupEntity;
    }

    @Override
    public List<Group> findGroupsByUser(String userLogin) {
        List<Group> activitiGroups = new ArrayList<>();

        edu.purdue.cybercenter.dm.domain.User user = edu.purdue.cybercenter.dm.domain.User.findByUsername(userLogin);
        if (user != null) {
            List<edu.purdue.cybercenter.dm.domain.Group> groups = user.getMemberGroups();

            for (edu.purdue.cybercenter.dm.domain.Group group : groups) {
                Group activitiGroup = new GroupEntity();
                activitiGroup.setId(group.getName());
                activitiGroup.setName(group.getName());
                activitiGroup.setType("");

                activitiGroups.add(activitiGroup);
            }
        }

        return activitiGroups;
    }

}
