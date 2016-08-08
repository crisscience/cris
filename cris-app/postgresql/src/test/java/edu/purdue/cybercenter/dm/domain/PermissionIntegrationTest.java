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
public class PermissionIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private PermissionDataOnDemand dod;

	@Test
    public void testCountPermissions() {
        org.junit.Assert.assertNotNull("Data on demand for 'Permission' failed to initialize correctly", dod.getRandomPermission());
        long count = edu.purdue.cybercenter.dm.domain.Permission.countPermissions();
        org.junit.Assert.assertTrue("Counter for 'Permission' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindPermission() {
        edu.purdue.cybercenter.dm.domain.Permission obj = dod.getRandomPermission();
        org.junit.Assert.assertNotNull("Data on demand for 'Permission' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Permission' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Permission.findPermission(id);
        org.junit.Assert.assertNotNull("Find method for 'Permission' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Permission' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllPermissions() {
        org.junit.Assert.assertNotNull("Data on demand for 'Permission' failed to initialize correctly", dod.getRandomPermission());
        long count = edu.purdue.cybercenter.dm.domain.Permission.countPermissions();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Permission', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Permission> result = edu.purdue.cybercenter.dm.domain.Permission.findAllPermissions();
        org.junit.Assert.assertNotNull("Find all method for 'Permission' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Permission' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindPermissionEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Permission' failed to initialize correctly", dod.getRandomPermission());
        long count = edu.purdue.cybercenter.dm.domain.Permission.countPermissions();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Permission> result = edu.purdue.cybercenter.dm.domain.Permission.findPermissionEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Permission' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Permission' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Permission' failed to initialize correctly", dod.getRandomPermission());
        edu.purdue.cybercenter.dm.domain.Permission obj = dod.getNewTransientPermission(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Permission' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Permission' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Permission' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Permission obj = dod.getRandomPermission();
        org.junit.Assert.assertNotNull("Data on demand for 'Permission' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Permission' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Permission.findPermission(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Permission' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Permission.findPermission(id));
    }
}
