/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.threadlocal;

/**
 *
 * @author xu222
 */
public class GroupId {

    private static final ThreadLocal<Integer> groupId = new ThreadLocal<>();

    public static Integer get() {
        return GroupId.groupId.get();
    }

    public static void set(Integer userId) {
        GroupId.groupId.set(userId);
    }
}
