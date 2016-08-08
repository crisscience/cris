/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * @author xu222
 */
@Service
public class MessagingService {

    private static JmsTemplate jmsTemplate;

    @Autowired
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        MessagingService.jmsTemplate = jmsTemplate;
    }

    public static void sendMessage(Map<String, Object> message) {
        MessagingService.jmsTemplate.convertAndSend("queueTaskEngine", message);
    }
}
