/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.jms;

import edu.purdue.cybercenter.dm.domain.Configuration;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.repository.ConfigurationRepository;
import edu.purdue.cybercenter.dm.repository.UserRepository;
import edu.purdue.cybercenter.dm.threadlocal.TenantId;
import java.io.Serializable;
import java.util.Map;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 *
 * @author xu222
 */
@Service
public class LoginFailureMessageDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFailureMessageDelegate.class.getName());

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConfigurationRepository configurationRepository;

    public void handleMessage(Serializable sMessage) {
        try {
            Map<String, Object> message = (Map<String, Object>) sMessage;
            Integer tenantId = (Integer) message.get("tenantId");
            TenantId.set(tenantId);
            String username = (String) message.get("username");
            User user = userRepository.findByUsernameAndTenantId(username, tenantId);
            if (user != null && !user.isEnabled()) {
                // send a memmage to admin that the suer is disbaled
                System.out.println(user.getUsername() + " is disabled");
                Configuration configuration = configurationRepository.findByNameAndTenantId("wsEmailAccountProblem", tenantId);
                String from = user.getEmail();
                String to = configuration.getValueText();
                String subject = "User Disabled: " + user.getEmail();
                String text = "Dear " + to + ":\n\n" + "A user tried to login the following disabled account:\n" + "    user: " + from + "\n    Date: " + message.get("date") + "\n    client: " + message.get("remote_address") + "\nregards,\ncris team";
                sendEmail(from, to, subject, text);
            }
        } finally {
            TenantId.set(null);
        }
    }

    private void sendEmail(String from, String to, String subject, String text) {
        MimeMessage mailMessage = mailSender.createMimeMessage();
        try {
            mailMessage.addRecipients(Message.RecipientType.TO, to);
            mailMessage.setFrom(new InternetAddress(from));
            mailMessage.setSubject(subject);
            mailMessage.setText(text);
            mailSender.send(mailMessage);
        } catch (MessagingException | MailException ex) {
            LOGGER.error("unable to send message from user: " + from + " to user: " + to + " with text: " + text + ": " + ex.getMessage());
        }
    }
}
