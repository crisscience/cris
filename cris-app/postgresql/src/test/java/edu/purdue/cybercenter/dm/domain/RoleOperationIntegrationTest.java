package edu.purdue.cybercenter.dm.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
@Transactional
public class RoleOperationIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private RoleOperationDataOnDemand dod;

	@Test
    public void testCountRoleOperations() {
        org.junit.Assert.assertNotNull("Data on demand for 'RoleOperation' failed to initialize correctly", dod.getRandomRoleOperation());
        long count = edu.purdue.cybercenter.dm.domain.RoleOperation.countRoleOperations();
        org.junit.Assert.assertTrue("Counter for 'RoleOperation' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindRoleOperation() {
        edu.purdue.cybercenter.dm.domain.RoleOperation obj = dod.getRandomRoleOperation();
        org.junit.Assert.assertNotNull("Data on demand for 'RoleOperation' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'RoleOperation' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.RoleOperation.findRoleOperation(id);
        org.junit.Assert.assertNotNull("Find method for 'RoleOperation' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'RoleOperation' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllRoleOperations() {
        org.junit.Assert.assertNotNull("Data on demand for 'RoleOperation' failed to initialize correctly", dod.getRandomRoleOperation());
        long count = edu.purdue.cybercenter.dm.domain.RoleOperation.countRoleOperations();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'RoleOperation', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.RoleOperation> result = edu.purdue.cybercenter.dm.domain.RoleOperation.findAllRoleOperations();
        org.junit.Assert.assertNotNull("Find all method for 'RoleOperation' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'RoleOperation' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindRoleOperationEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'RoleOperation' failed to initialize correctly", dod.getRandomRoleOperation());
        long count = edu.purdue.cybercenter.dm.domain.RoleOperation.countRoleOperations();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.RoleOperation> result = edu.purdue.cybercenter.dm.domain.RoleOperation.findRoleOperationEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'RoleOperation' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'RoleOperation' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'RoleOperation' failed to initialize correctly", dod.getRandomRoleOperation());
        edu.purdue.cybercenter.dm.domain.RoleOperation obj = dod.getNewTransientRoleOperation(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'RoleOperation' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'RoleOperation' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'RoleOperation' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.RoleOperation obj = dod.getRandomRoleOperation();
        org.junit.Assert.assertNotNull("Data on demand for 'RoleOperation' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'RoleOperation' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.RoleOperation.findRoleOperation(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'RoleOperation' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.RoleOperation.findRoleOperation(id));
    }
}
