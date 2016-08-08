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
public class RoleIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private RoleDataOnDemand dod;

	@Test
    public void testCountRoles() {
        org.junit.Assert.assertNotNull("Data on demand for 'Role' failed to initialize correctly", dod.getRandomRole());
        long count = edu.purdue.cybercenter.dm.domain.Role.countRoles();
        org.junit.Assert.assertTrue("Counter for 'Role' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindRole() {
        edu.purdue.cybercenter.dm.domain.Role obj = dod.getRandomRole();
        org.junit.Assert.assertNotNull("Data on demand for 'Role' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Role' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Role.findRole(id);
        org.junit.Assert.assertNotNull("Find method for 'Role' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Role' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllRoles() {
        org.junit.Assert.assertNotNull("Data on demand for 'Role' failed to initialize correctly", dod.getRandomRole());
        long count = edu.purdue.cybercenter.dm.domain.Role.countRoles();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Role', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Role> result = edu.purdue.cybercenter.dm.domain.Role.findAllRoles();
        org.junit.Assert.assertNotNull("Find all method for 'Role' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Role' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindRoleEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Role' failed to initialize correctly", dod.getRandomRole());
        long count = edu.purdue.cybercenter.dm.domain.Role.countRoles();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Role> result = edu.purdue.cybercenter.dm.domain.Role.findRoleEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Role' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Role' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Role' failed to initialize correctly", dod.getRandomRole());
        edu.purdue.cybercenter.dm.domain.Role obj = dod.getNewTransientRole(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Role' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Role' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Role' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Role obj = dod.getRandomRole();
        org.junit.Assert.assertNotNull("Data on demand for 'Role' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Role' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Role.findRole(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Role' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Role.findRole(id));
    }
}
