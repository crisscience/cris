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
public class GroupIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private GroupDataOnDemand dod;

	@Test
    public void testCountGroups() {
        org.junit.Assert.assertNotNull("Data on demand for 'Group' failed to initialize correctly", dod.getRandomGroup());
        long count = edu.purdue.cybercenter.dm.domain.Group.countGroups();
        org.junit.Assert.assertTrue("Counter for 'Group' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindGroup() {
        edu.purdue.cybercenter.dm.domain.Group obj = dod.getRandomGroup();
        org.junit.Assert.assertNotNull("Data on demand for 'Group' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Group' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Group.findGroup(id);
        org.junit.Assert.assertNotNull("Find method for 'Group' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Group' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllGroups() {
        org.junit.Assert.assertNotNull("Data on demand for 'Group' failed to initialize correctly", dod.getRandomGroup());
        long count = edu.purdue.cybercenter.dm.domain.Group.countGroups();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Group', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Group> result = edu.purdue.cybercenter.dm.domain.Group.findAllGroups();
        org.junit.Assert.assertNotNull("Find all method for 'Group' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Group' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindGroupEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Group' failed to initialize correctly", dod.getRandomGroup());
        long count = edu.purdue.cybercenter.dm.domain.Group.countGroups();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Group> result = edu.purdue.cybercenter.dm.domain.Group.findGroupEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Group' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Group' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Group' failed to initialize correctly", dod.getRandomGroup());
        edu.purdue.cybercenter.dm.domain.Group obj = dod.getNewTransientGroup(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Group' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Group' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Group' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Group obj = dod.getRandomGroup();
        org.junit.Assert.assertNotNull("Data on demand for 'Group' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Group' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Group.findGroup(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Group' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Group.findGroup(id));
    }
}
