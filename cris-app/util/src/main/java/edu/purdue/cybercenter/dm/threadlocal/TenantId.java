/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.threadlocal;

/**
 *
 * @author xu222
 */
public class TenantId {

    private static final ThreadLocal<Integer> tenantId = new ThreadLocal<>();

    public static Integer get() {
        return TenantId.tenantId.get();
    }

    public static void set(Integer tenantId) {
        TenantId.tenantId.set(tenantId);
    }
}
