/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.repository;

import edu.purdue.cybercenter.dm.domain.Session;
import org.springframework.stereotype.Repository;

/**
 *
 * @author xu222
 */
@Repository
public interface SessionRepository extends CrisRepository<Session> {
    public Session findByJsessionid(String jsessionId);
}
