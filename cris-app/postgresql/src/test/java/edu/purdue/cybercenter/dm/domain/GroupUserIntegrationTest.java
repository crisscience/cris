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
public class GroupUserIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testPersist() {
        /* TODO: to randomly generated combination of user id and group id sometimes matches existing ones and cause test failure.
        org.junit.Assert.assertNotNull("Data on demand for 'GroupUser' failed to initialize correctly", dod.getRandomGroupUser());
        edu.purdue.cybercenter.dm.domain.GroupUser obj = dod.getNewTransientGroupUser(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'GroupUser' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'GroupUser' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'GroupUser' identifier to no longer be null", obj.getId());
         */
    }
    

	@Autowired
    private GroupUserDataOnDemand dod;

	@Test
    public void testCountGroupUsers() {
        org.junit.Assert.assertNotNull("Data on demand for 'GroupUser' failed to initialize correctly", dod.getRandomGroupUser());
        long count = edu.purdue.cybercenter.dm.domain.GroupUser.countGroupUsers();
        org.junit.Assert.assertTrue("Counter for 'GroupUser' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindGroupUser() {
        edu.purdue.cybercenter.dm.domain.GroupUser obj = dod.getRandomGroupUser();
        org.junit.Assert.assertNotNull("Data on demand for 'GroupUser' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'GroupUser' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.GroupUser.findGroupUser(id);
        org.junit.Assert.assertNotNull("Find method for 'GroupUser' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'GroupUser' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllGroupUsers() {
        org.junit.Assert.assertNotNull("Data on demand for 'GroupUser' failed to initialize correctly", dod.getRandomGroupUser());
        long count = edu.purdue.cybercenter.dm.domain.GroupUser.countGroupUsers();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'GroupUser', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.GroupUser> result = edu.purdue.cybercenter.dm.domain.GroupUser.findAllGroupUsers();
        org.junit.Assert.assertNotNull("Find all method for 'GroupUser' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'GroupUser' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindGroupUserEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'GroupUser' failed to initialize correctly", dod.getRandomGroupUser());
        long count = edu.purdue.cybercenter.dm.domain.GroupUser.countGroupUsers();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.GroupUser> result = edu.purdue.cybercenter.dm.domain.GroupUser.findGroupUserEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'GroupUser' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'GroupUser' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.GroupUser obj = dod.getRandomGroupUser();
        org.junit.Assert.assertNotNull("Data on demand for 'GroupUser' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'GroupUser' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.GroupUser.findGroupUser(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'GroupUser' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.GroupUser.findGroupUser(id));
    }
}
