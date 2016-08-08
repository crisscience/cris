/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.Constant;
import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.GroupUser;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.repository.GroupRepository;
import edu.purdue.cybercenter.dm.repository.GroupUserRepository;
import edu.purdue.cybercenter.dm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class GroupUserServiceImpl implements GroupUserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupUserRepository groupUserRepository;

    @Override
    public boolean isUserInGroup(Integer groupId, Integer userId) {
        Group group = groupRepository.findOne(groupId);
        User user = userRepository.findOne(userId);
        GroupUser groupUser = groupUserRepository.findByGroupIdAndUserId(group, user);
        boolean isUserInGroup = groupUser != null;
        return isUserInGroup;
    }

    @Override
    public boolean isUserInGroup(Group group, User user) {
        boolean isUserInGroup = isUserInGroup(group.getId(), user.getId());
        return isUserInGroup;
    }

    @Override
    public boolean isUserInGroup(Integer groupUserId) {
        GroupUser groupUser = groupUserRepository.findOne(groupUserId);
        boolean isUserInGroup = groupUser != null;
        return isUserInGroup;
    }

    @Override
    public GroupUser getGroupUser(Integer groupId, Integer userId) {
        Group group = groupRepository.findOne(groupId);
        User user = userRepository.findOne(userId);
        GroupUser groupUser = groupUserRepository.findByGroupIdAndUserId(group, user);
        return groupUser;
    }

    @Override
    public GroupUser getGroupUser(Group group, User user) {
        GroupUser groupUser = getGroupUser(group.getId(), user.getId());
        return groupUser;
    }

    @Override
    public GroupUser getGroupUser(Integer groupUserId) {
        GroupUser groupUser = groupUserRepository.findOne(groupUserId);
        return groupUser;
    }

    public GroupUser createGroupUser(Integer groupId, Integer userId) {
        User user = userRepository.findOne(userId);
        Group group = groupRepository.findOne(groupId);
        String username = user.getUsername();
        if (Constant.AdminUsername.equals(username) || Constant.PublicUsername.equals(username)) {
            throw new RuntimeException("You cannot change the membership of user: " + username);
        }

        if (isUserInGroup(groupId, userId)) {
            throw new RuntimeException(String.format("User: %s already in group: %s", username, group.getName()));
        }

        GroupUser groupUser = new GroupUser();
        groupUser.setUserId(user);
        groupUser.setGroupId(group);
        return groupUserRepository.save(groupUser);
    }

    @Override
    public GroupUser createGroupUser(Group group, User user) {
        String username = user.getUsername();
        if (Constant.AdminUsername.equals(username) || Constant.PublicUsername.equals(username)) {
            throw new RuntimeException("You cannot change the membership of user: " + username);
        }

        if (isUserInGroup(group, user)) {
            throw new RuntimeException(String.format("User: %s already in group: %s", username, group.getName()));
        }

        GroupUser groupUser = new GroupUser();
        groupUser.setUserId(user);
        groupUser.setGroupId(group);
        return groupUserRepository.save(groupUser);
    }

    @Override
    public GroupUser createGroupUser(GroupUser groupUser) {
        User user = groupUser.getUserId();
        Group group = groupUser.getGroupId();
        String username = user.getUsername();
        if (Constant.AdminUsername.equals(username) || Constant.PublicUsername.equals(username)) {
            throw new RuntimeException("You cannot change the membership of user: " + username);
        }

        if (isUserInGroup(group.getId(), user.getId())) {
            throw new RuntimeException(String.format("User: %s already in group: %s", username, group.getName()));
        }

        return groupUserRepository.save(groupUser);
    }

    @Override
    public void deleteGroupUser(Integer groupId, Integer userId) {
        User user = userRepository.findOne(userId);
        Group group = groupRepository.findOne(groupId);
        deleteGroupUser(group, user);
    }

    @Override
    public void deleteGroupUser(Group group, User user) {
        String username = user.getUsername();
        if (Constant.AdminUsername.equals(username) || Constant.PublicUsername.equals(username)) {
            throw new RuntimeException("You cannot change the membership of user: " + username);
        }

        if (!isUserInGroup(group, user)) {
            throw new RuntimeException(String.format("User: %s not in group: %s", username, group.getName()));
        }

        GroupUser groupUser = groupUserRepository.findByGroupIdAndUserId(group, user);
        groupUserRepository.delete(groupUser);
    }

    @Override
    public void deleteGroupUser(Integer groupUserId) {
        GroupUser groupUser = groupUserRepository.findOne(groupUserId);
        User user = groupUser.getUserId();
        String username = user.getUsername();
        if (Constant.AdminUsername.equals(username) || Constant.PublicUsername.equals(username)) {
            throw new RuntimeException("You cannot change the membership of user: " + username);
        }

        groupUserRepository.delete(groupUser);
    }

}
