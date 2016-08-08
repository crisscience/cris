/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.threadlocal;

/**
 *
 * @author xu222
 */
public class UserId {

    private static final ThreadLocal<Integer> userId = new ThreadLocal<>();

    public static Integer get() {
        return UserId.userId.get();
    }

    public static void set(Integer userId) {
        UserId.userId.set(userId);
    }
}
