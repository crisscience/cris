/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.threadlocal.TenantId;
import edu.purdue.cybercenter.dm.threadlocal.UserId;
import edu.purdue.cybercenter.dm.util.SecurityHelper;
import edu.purdue.cybercenter.dm.util.TermName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.access.AccessDeniedException;
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
public class PermissionServiceIntegrationTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final int TENANT_ID_1 = 1;
    private static final int USER_ID_4 = 4;
    private static final int USER_ID_5 = 5;

    private static final String TEMPLATE_UUID = "85833b40-73d3-11e2-bcfd-0800200c9a66";

    @Autowired
    private DatasetService datasetService;

    @Before
    public void testSetup() {
        TenantId.set(TENANT_ID_1);
    }

    @Test
    public void testCreateDataset() {
        Map<String, Object> context = new HashMap<>();
        context.put(MetaField.ProjectId, 5001);
        context.put(MetaField.ExperimentId, 7001);
        context.put(MetaField.JobId, null);
        context.put(MetaField.ContextId, 12345);

        Map<String, Object> value = new HashMap<>();
        value.put("hplc_id", "c1");
        value.put("hplc_name", "c2");

        Map result = null;

        /*
         * create
         */
        // test create without create permission
        UserId.set(USER_ID_5);
        SecurityHelper.setAuthentication(USER_ID_5);
        value.putAll(context);
        try {
            result = datasetService.putValue(UUID.fromString(TEMPLATE_UUID), null, value);
            throw new RuntimeException("shoudn't be able to create a dataset");
        } catch (AccessDeniedException ex) {
            // expect to be denied
        }
        Assert.assertNull("document is not null", result);

        // test create with permission
        UserId.set(USER_ID_4);
        SecurityHelper.setAuthentication(USER_ID_4);
        value.putAll(context);
        result = datasetService.putValue(UUID.fromString(TEMPLATE_UUID), null, value);

        Assert.assertNotNull("document is null", result);
        Assert.assertNotNull("field _id:", result.get("_id"));
        Assert.assertEquals("field tenantId", TENANT_ID_1, (int) result.get(MetaField.TenantId));
        Assert.assertNotNull("field timeCreated", result.get(MetaField.TimeCreated));
        Assert.assertNotNull("field timeUpdated", result.get(MetaField.TimeUpdated));
        Assert.assertEquals("field createdBy", USER_ID_4, (int) result.get(MetaField.CreatorId));
        Assert.assertEquals("field updatedBy", USER_ID_4, (int) result.get(MetaField.UpdaterId));
        Assert.assertEquals("timeCreated == timeUpdated", result.get(MetaField.TimeCreated), result.get(MetaField.TimeUpdated));
        Assert.assertEquals("createdBy == updatedBy", result.get(MetaField.CreatorId), result.get(MetaField.UpdaterId));

        /*
         * update
         */
        // test update without permission
        UserId.set(USER_ID_5);
        SecurityHelper.setAuthentication(USER_ID_5);
        result.put("hplc_weight", "c3");
        result.put("hplc_volume", "c4");
        Map resultUpdate = null;
        try {
            resultUpdate = datasetService.putValue(UUID.fromString(TEMPLATE_UUID), null, result);
            throw new RuntimeException("shoudn't be able to update a dataset");
        } catch (AccessDeniedException ex) {
            // expect to be denied
        }
        Assert.assertNull("document is not null", resultUpdate);

        // test update with permission
        UserId.set(USER_ID_4);
        SecurityHelper.setAuthentication(USER_ID_4);
        resultUpdate = datasetService.putValue(UUID.fromString(TEMPLATE_UUID), null, result);

        Assert.assertNotNull("document is not null", resultUpdate);
        Assert.assertNotNull("field _id:", resultUpdate.get("_id"));
        Assert.assertNotNull("field timeCreated", resultUpdate.get(MetaField.TimeCreated));
        Assert.assertNotNull("field timeUpdated", resultUpdate.get(MetaField.TimeUpdated));
        Assert.assertEquals("field createdBy", USER_ID_4, (int) resultUpdate.get(MetaField.CreatorId));
        Assert.assertEquals("field updatedBy", USER_ID_4, (int) resultUpdate.get(MetaField.UpdaterId));
        Assert.assertNotSame("timeCreated != timeUpdated", resultUpdate.get(MetaField.TimeCreated), resultUpdate.get(MetaField.TimeUpdated));
        Assert.assertSame("createdBy == updatedBy", resultUpdate.get(MetaField.CreatorId), resultUpdate.get(MetaField.UpdaterId));

        /*
         * delete
         */
        TermName termName = new TermName(TEMPLATE_UUID);
        UUID termUuid = termName.getUuid();

        // test delete without permission
        UserId.set(USER_ID_5);
        SecurityHelper.setAuthentication(USER_ID_5);

        try {
            datasetService.delete(termUuid, resultUpdate);
            throw new RuntimeException("shoudn't be able to delete a dataset");
        } catch (AccessDeniedException ex) {
            // expect to be denied
        }

        // test delete with permission
        UserId.set(USER_ID_4);
        SecurityHelper.setAuthentication(USER_ID_4);

        datasetService.delete(termUuid, resultUpdate);
        Map<String, Object> aggregators = new HashMap<>();
        aggregators.put(DocumentService.AGGREGATOR_MATCH, resultUpdate);
        Object objectFound = datasetService.find(termUuid, aggregators);
        Assert.assertTrue("object is not null", ((List) objectFound).isEmpty());
    }

}
