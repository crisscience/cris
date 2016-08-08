/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.threadlocal.TenantId;
import edu.purdue.cybercenter.dm.threadlocal.UserId;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author xu222
 */
@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
public class DocumentServiceTest {

    @Autowired
    private DocumentService documentService;

    final private UUID uuid1 = UUID.fromString("ef3eaa00-1c7c-11e2-892e-0800200c9a66");
    final private UUID uuid2 = UUID.fromString("85833b40-73d3-11e2-bcfd-0800200c9a66");
    final private UUID version1 = UUID.fromString("1fd77b80-c7c0-11e2-8b8b-0800200c9a66");
    final private UUID version2 = UUID.fromString("f3b8abe0-c7cf-11e2-8b8b-0800200c9a66");
    final private Integer tenantId1 = 8;
    final private Integer tenantId2 = 9;
    final private Integer userId1 = 8;
    final private Integer userId2 = 9;

    @Before
    public void testSetup() {
        TenantId.set(null);
        UserId.set(null);
        documentService.delete(uuid1, null, null);

        TenantId.set(tenantId2);
        UserId.set(userId2);
        documentService.delete(uuid1, null, null);

        TenantId.set(tenantId1);
        UserId.set(userId1);
        documentService.delete(uuid1, null, null);
    }

    @After
    public void tearDown() {
        TenantId.set(null);
        UserId.set(null);
        documentService.delete(uuid1, null, null);

        TenantId.set(tenantId2);
        UserId.set(userId2);
        documentService.delete(uuid1, null, null);

        TenantId.set(tenantId1);
        UserId.set(userId1);
        documentService.delete(uuid1, null, null);
    }

    @Test
    public void testAutoWired() {
        Assert.assertNotNull("Auto-wire datasetService failed", documentService);
    }

    @Test
    public void testMissingTenantId() {
        TenantId.set(null);

        Map<String, Object> map = new HashMap<>();

        Map document = save(map);
        Assert.assertEquals("document should be null", null, document);
    }

    @Test
    public void testMissingUserId() {
        UserId.set(null);

        Map<String, Object> map = new HashMap<>();

        Map document = save(map);
        Assert.assertEquals("document should be null", null, document);
    }

    @Test
    public void testNullDocument() {
        Map document = save(null);
        Assert.assertNull("document should be null", document);
    }

    @Test
    public void testEmptyDocument() {
        Map<String, Object> map = new HashMap<>();

        Map document = save(map);
        Assert.assertEquals("number of fields", 8, document.size());
    }

    @Test
    public void testCreateDataset() {
        Map<String, Object> map = new HashMap<>();

        Map<String, Object> document = save(map);

        Assert.assertNotNull("document is null", document);
        Assert.assertNotNull("field _id:", document.get("_id"));

        Assert.assertEquals("field version:", version1, document.get(MetaField.TemplateVersion));

        Assert.assertEquals("field tenantId", tenantId1, document.get(MetaField.TenantId));
        Assert.assertNotNull("field timeCreated", document.get(MetaField.TimeCreated));
        Assert.assertNotNull("field timeUpdated", document.get(MetaField.TimeUpdated));
        Assert.assertEquals("field createdBy", userId1, document.get(MetaField.CreatorId));
        Assert.assertEquals("field updatedBy", userId1, document.get(MetaField.UpdaterId));
        Assert.assertEquals("timeCreated == timeUpdated", document.get(MetaField.TimeCreated), document.get(MetaField.TimeUpdated));
        Assert.assertEquals("createdBy == updatedBy", document.get(MetaField.CreatorId), document.get(MetaField.UpdaterId));
    }

    @Test
    public void testUpdateDataset() {
        Map<String, Object> map = new HashMap<>();

        map.put("f1", "1");
        Map<String, Object> savedMap = save(map);

        Map<String, Object> document1 = documentService.findById(uuid1, (ObjectId) savedMap.get(MetaField.Id));
        Assert.assertNotNull("document is null", document1);

        Map<String, Object> document2 = new HashMap<>();
        document2.put(MetaField.Id, document1.get(MetaField.Id));
        document2.put("f2", "2");
        Map<String, Object> document = documentService.save(uuid1, version2, document2);

        Assert.assertNotNull("document is null", document);
        Assert.assertNotNull("field _id:", document.get("_id"));

        Assert.assertEquals("field version:", version2, document.get(MetaField.TemplateVersion));

        Assert.assertEquals("field tenantId", tenantId1, document.get(MetaField.TenantId));
        Assert.assertNotNull("field timeCreated", document.get(MetaField.TimeCreated));
        Assert.assertNotNull("field timeUpdated", document.get(MetaField.TimeUpdated));
        Assert.assertEquals("field createdBy", userId1, document.get(MetaField.CreatorId));
        Assert.assertEquals("field updatedBy", userId1, document.get(MetaField.UpdaterId));
        Assert.assertNotSame("timeCreated != timeUpdated", document.get(MetaField.TimeCreated), document.get(MetaField.TimeUpdated));
        Assert.assertSame("createdBy == updatedBy", document.get(MetaField.CreatorId), document.get(MetaField.UpdaterId));
        Assert.assertEquals("field f1", "1", document.get("f1"));
        Assert.assertEquals("field f2", "2", document.get("f2"));
    }

    @Test
    public void testDeleteDataset() {
        Map<String, Object> map = new HashMap<>();

        save(map);
        save(map);
        save(map);

        List<Map<String, Object>> documents = documentService.find(uuid1, version1, null);
        Assert.assertEquals("document size:", 3, documents.size());

        Map<String, Object> document = documents.get(1);

        Map<String, Object> aggregators = new HashMap<>();
        aggregators.put(DocumentService.AGGREGATOR_MATCH, document);

        List<Map<String, Object>> documentsSelected = documentService.find(uuid1, version1, aggregators);
        Assert.assertEquals("selected document size:", 1, documentsSelected.size());

        documentService.delete(uuid1, version1, document);
        documentsSelected = documentService.find(uuid1, version1, aggregators);
        Assert.assertEquals("selected document size after deletion:", 0, documentsSelected.size());

        documents = documentService.find(uuid1, version1, null);
        Assert.assertEquals("document size after deletion:", 2, documents.size());
    }

    @Test
    public void testFieldsToReturn() {
        TenantId.set(1);
        UserId.set(1);

        Map<String, Object> query = new HashMap<>();
        query.put("hplc_id", "a1");
        Map<String, Object> aggregators = new HashMap<>();
        aggregators.put(DocumentService.AGGREGATOR_MATCH, query);
        List<Map<String, Object>> documents = documentService.find(uuid2, null, aggregators);
        Assert.assertEquals("documents size", 2, documents.size());
        Map<String, Object> document = documents.get(0);
        Assert.assertEquals("fields", 20, document.size());

        // project
        Map<String, Boolean> project = new HashMap<>();
        project.put("hplc_id", true);
        project.put("hplc_name", true);
        aggregators.clear();
        aggregators.put(DocumentService.AGGREGATOR_DISTINCT, project);
        documents = documentService.find(uuid2, null, aggregators);
        int less = documents.size();
        document = documents.get(0);
        Assert.assertEquals("fields", 2, document.size());

        // include _id filed
        project.put("_id", true);
        documents = documentService.find(uuid2, null, aggregators);
        int more = documents.size();
        document = documents.get(0);
        Assert.assertEquals("fields", 2 + 1, document.size());
        Assert.assertTrue("de-duplicate failed", more > less);

        // exclusion
        /* no longer supported by MongoDB. 11/07/2014
        fieldsToReturn.put("hplc_id", 0);
        fieldsToReturn.put("hplc_name", 0);
        documents = documentService.find(uuid2, null, query);
        document = documents.get(0);
        Assert.assertEquals("fields", 22 - 2, document.size());
        */
    }

    @Test
    public void testToFile() throws IOException {
        TenantId.set(1);
        UserId.set(1);

        File file = new File("target/dataset.json");
        Map<String, Object> query = new HashMap<>();
        query.put("hplc_id", "a1");
        Map<String, Object> aggregators = new HashMap<>();
        aggregators.put(DocumentService.AGGREGATOR_MATCH, query);
        List<Map<String, Object>> documents = documentService.find(uuid2, null, aggregators, file);
        Assert.assertEquals("documents size", 0, documents.size());

        try (FileReader fileReader = new FileReader(file)) {
            // read in the ToFile
            String json = IOUtils.toString(fileReader);
            documents = (List<Map<String, Object>>) DatasetUtils.deserialize(json);
            Map<String, Object> document = documents.get(0);
            Assert.assertEquals("fields", 20, document.size());
        }

    }

    @Test
    public void testSerializer() {
        String result = DatasetUtils.serialize(null);
        Assert.assertEquals(" null ", result);

        result = DatasetUtils.serialize(true);
        Assert.assertEquals("true", result);

        result = DatasetUtils.serialize(false);
        Assert.assertEquals("false", result);

        result = DatasetUtils.serialize(1234.5678);
        Assert.assertEquals("1234.5678", result);


        result = DatasetUtils.serialize("1234abcd");
        Assert.assertEquals("\"1234abcd\"", result);
    }

    @Test
    public void testDeserializer() {
        Object result = DatasetUtils.deserialize(" null ");
        Assert.assertEquals(null, result);

        result = DatasetUtils.deserialize("true");
        Assert.assertEquals(Boolean.TRUE, result);

        result = DatasetUtils.deserialize("false");
        Assert.assertEquals(Boolean.FALSE, result);

        result = DatasetUtils.deserialize("1234.5678");
        Assert.assertEquals(1234.5678, result);

        result = DatasetUtils.deserialize("\"1234abcd\"");
        Assert.assertEquals("1234abcd", result);
    }

    private Map<String, Object> save(Map map) {
        Map document = documentService.save(uuid1, version1, map);

        return document;
    }

}
