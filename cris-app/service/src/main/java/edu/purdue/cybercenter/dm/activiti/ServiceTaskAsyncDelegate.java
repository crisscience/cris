/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.activiti;

import edu.purdue.cybercenter.dm.domain.Tenant;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.security.UserDetailsAdapter;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import java.util.Map;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author xu222
 */
@Configurable
public class ServiceTaskAsyncDelegate extends ServiceTaskDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ServiceTaskAsyncDelegate.class.getName());

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void execute(DelegateExecution de) throws Exception {
        String processInstanceId = de.getProcessInstanceId();
        String processBusinessKey = de.getProcessBusinessKey();

        Map<String, Object> context = buildContext(de);
        placeFiles(context, de);

        Integer tenantId = edu.purdue.cybercenter.dm.threadlocal.TenantId.get();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();

        Runnable task = new Runnable() {
            @Transactional
            @Override
            public void run() {
                try {
                    // set database tenant filter
                    org.hibernate.Session hSession = DomainObjectHelper.getHbmSession();
                    org.hibernate.Filter tenantFilter = hSession.enableFilter("tenantFilter");
                    tenantFilter.setParameter("tenantId", tenantId);

                    Tenant tenant = Tenant.findTenant(tenantId);
                    edu.purdue.cybercenter.dm.threadlocal.TenantId.set(tenantId);
                    edu.purdue.cybercenter.dm.threadlocal.UserId.set(userId);

                    // create the credentials used by spring security
                    User user = User.findUser(userId);
                    UserDetails userDetails = new UserDetailsAdapter(user);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    Thread.sleep(1000);
                    Map<String, Object> mergedJsonOut = execute(context);
                    collectFiles(context);
                    saveResult(mergedJsonOut, context, null, processInstanceId);
                } catch (Exception ex) {
                    logger.error("failed to execute job: " + processBusinessKey + ex.getMessage(), ex);
                }

                // Signal the completion of the task
                Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).singleResult();
                runtimeService.signal(execution.getId());
            }
        };

        taskExecutor.execute(task);
    }

}
