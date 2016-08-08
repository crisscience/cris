/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.threadlocal.TenantId;
import edu.purdue.cybercenter.dm.threadlocal.UserId;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.SecurityHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author xu222
 */
@Transactional
@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
public class CqlServiceIntegrationTest {

    final private int tenantId1 = 1;
    final private int tenantId2 = 2;
    final private int userId1 = 1;
    final private int userId2 = 2;

    @Autowired
    private CqlService cqlService;

    @Test
    public void testEvalSystemVariableWithCurrent() {
        String expression = "{_job_id : #{current_job_id}, _task_id : #{current_task_id}}";
        Map<String, Object> context = new HashMap<>();
        context.put(MetaField.JobId, 8);
        context.put(MetaField.TaskId, "abc");

        String result = cqlService.eval(expression, context);

        assertEquals("result", "{_job_id : 8, _task_id : \"abc\"}", result);
    }

    @Test
    public void testEvalSystemVariable() {
        String expression = "{\"project\" : #{projects({_id : 5001})}}";
        Map<String, Object> context = new HashMap<>();

        String result = cqlService.eval(expression, context);
        Map<String, Object> mapResult = (Map<String, Object>) DatasetUtils.deserialize(result);

        Map project = (Map) mapResult.get("project");
        assertNotNull("project", project);
        assertEquals("project id", 5001, project.get("id"));
        assertEquals("name", "NSF Fund 12345678", project.get("name"));
        assertEquals("description", "Purdue Center for Cancer Research", project.get("description"));

        context.put("_project", Project.findProject(5001));
        expression = "{\"projectId\" : #{current_project.id}, \"projectName\" : #{current_project.name}, \"timeCreated\" : #{current_project.timeCreated}}";
        result = cqlService.eval(expression, context);
        mapResult = (Map<String, Object>) DatasetUtils.deserialize(result);
        assertEquals("wrong project id", 5001, mapResult.get("projectId"));
        assertEquals("wrong project name", "NSF Fund 12345678", mapResult.get("projectName"));
        assertEquals("wrong time created", null, mapResult.get("timeCreated"));
    }

    @Test
    public void testEvalTemplateVariable() {
        TenantId.set(tenantId1);
        UserId.set(userId1);
        SecurityHelper.setAuthentication(userId1);

        String expression = "{\"hplc_data\" : ${85833b40-73d3-11e2-bcfd-0800200c9a66[]({_context_id : 2147483647})}}";
        Map<String, Object> context = new HashMap<>();

        String result = cqlService.eval(expression, context);
        Map<String, Object> mapResult = (Map<String, Object>) DatasetUtils.deserialize(result);

        List<Map<String, Object>> hplcData = (List<Map<String, Object>>) mapResult.get("hplc_data");
        assertNotNull("hplc_data", hplcData);
        assertEquals("hplc data records", 3, hplcData.size());

        TenantId.set(tenantId2);
        UserId.set(userId2);
        SecurityHelper.setAuthentication(userId2);

        result = cqlService.eval(expression, context);
        mapResult = (Map<String, Object>) DatasetUtils.deserialize(result);

        hplcData = (List<Map<String, Object>>) mapResult.get("hplc_data");
        assertNotNull("hplc_data", hplcData);
        assertEquals("hplc data records", 0, hplcData.size());
    }

    @Test
    public void testEvalTemplateVariableField() {
        TenantId.set(tenantId1);
        UserId.set(userId1);
        SecurityHelper.setAuthentication(userId1);

        String expression = "{\"hplc_data\" : ${85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_name[]({_context_id : 2147483647})}}";
        Map<String, Object> context = new HashMap<>();

        String result = cqlService.eval(expression, context);
        Map<String, Object> mapResult = (Map<String, Object>) DatasetUtils.deserialize(result);

        List<String> hplcData = (List<String>) mapResult.get("hplc_data");
        assertNotNull("hplc_data", hplcData);
        assertEquals("hplc data records", 3, hplcData.size());

        TenantId.set(tenantId2);

        result = cqlService.eval(expression, context);
        mapResult = (Map<String, Object>) DatasetUtils.deserialize(result);

        hplcData = (List<String>) mapResult.get("hplc_data");
        assertNotNull("hplc_data", hplcData);
        assertEquals("hplc data records", 0, hplcData.size());
    }

    @Test
    public void testEvalMixedVariable() {
        TenantId.set(tenantId1);
        UserId.set(userId1);
        SecurityHelper.setAuthentication(userId1);

        String expression = "{\"project\" : #{projects({_id : 5001})}, \"hplc_data\" : ${85833b40-73d3-11e2-bcfd-0800200c9a66[]({_context_id : 2147483647})}}";
        Map<String, Object> context = new HashMap<>();

        String result = cqlService.eval(expression, context);
        Map<String, Object> mapResult = (Map<String, Object>) DatasetUtils.deserialize(result);

        Map project = (Map) mapResult.get("project");
        assertNotNull("project", project);
        assertEquals("project id", 5001, project.get("id"));
        assertEquals("name", "NSF Fund 12345678", project.get("name"));
        assertEquals("description", "Purdue Center for Cancer Research", project.get("description"));

        List<Map<String, Object>> hplcData = (List<Map<String, Object>>) mapResult.get("hplc_data");
        assertNotNull("hplc_data", hplcData);
        assertEquals("hplc data records", 3, hplcData.size());
    }

    @Test
    public void testEvalNestedVariable() {
        TenantId.set(tenantId1);
        UserId.set(userId1);
        SecurityHelper.setAuthentication(userId1);

        String expression = "{\"hplc_data\" : ${85833b40-73d3-11e2-bcfd-0800200c9a66[]({_context_id : #{current_context_id}})}}";
        Map<String, Object> context = new HashMap<>();
        context.put(MetaField.ContextId, 2147483647);

        String result = cqlService.eval(expression, context);
        Map<String, Object> mapResult = (Map<String, Object>) DatasetUtils.deserialize(result);

        List<Map<String, Object>> hplcData = (List<Map<String, Object>>) mapResult.get("hplc_data");
        assertNotNull("hplc_data", hplcData);
        assertEquals("hplc data records", 3, hplcData.size());

        Map<String, Object> data = hplcData.get(0);
        assertEquals(MetaField.TenantId, 1, data.get(MetaField.TenantId));
        assertEquals(MetaField.ContextId, 2147483647, data.get(MetaField.ContextId));
    }
}
