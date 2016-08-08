/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.repository;

import com.mysema.query.types.Predicate;
import edu.purdue.cybercenter.dm.domain.QProject;
import edu.purdue.cybercenter.dm.domain.User;

/**
 *
 * @author xu222
 */
public class PermissionPredicates {

    public static <T> Predicate hasPermission(User user, T clazz, Long id) {
        QProject entry = QProject.project;
        return entry.id.eq(5001);
    }

}
