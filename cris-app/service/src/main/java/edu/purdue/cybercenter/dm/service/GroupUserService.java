/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.GroupUser;
import edu.purdue.cybercenter.dm.domain.User;

/**
 *
 * @author xu222
 */
public interface GroupUserService {

    public boolean isUserInGroup(Integer groupId, Integer userId);

    public boolean isUserInGroup(Group group, User user);

    public boolean isUserInGroup(Integer groupUserId);

    public GroupUser getGroupUser(Integer groupId, Integer userId);

    public GroupUser getGroupUser(Group group, User user);

    public GroupUser getGroupUser(Integer groupUserId);

    public GroupUser createGroupUser(Integer groupId, Integer userId);

    public GroupUser createGroupUser(Group group, User user);

    public GroupUser createGroupUser(GroupUser groupUser);

    public void deleteGroupUser(Integer groupId, Integer userId);

    public void deleteGroupUser(Group group, User user);

    public void deleteGroupUser(Integer groupUserId);
}
