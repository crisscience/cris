package edu.purdue.cybercenter.dm.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
@Transactional
@Configurable
public class UserIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private UserDataOnDemand dod;

	@Test
    public void testCountUsers() {
        org.junit.Assert.assertNotNull("Data on demand for 'User' failed to initialize correctly", dod.getRandomUser());
        long count = edu.purdue.cybercenter.dm.domain.User.countUsers();
        org.junit.Assert.assertTrue("Counter for 'User' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindUser() {
        edu.purdue.cybercenter.dm.domain.User obj = dod.getRandomUser();
        org.junit.Assert.assertNotNull("Data on demand for 'User' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'User' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.User.findUser(id);
        org.junit.Assert.assertNotNull("Find method for 'User' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'User' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllUsers() {
        org.junit.Assert.assertNotNull("Data on demand for 'User' failed to initialize correctly", dod.getRandomUser());
        long count = edu.purdue.cybercenter.dm.domain.User.countUsers();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'User', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.User> result = edu.purdue.cybercenter.dm.domain.User.findAllUsers();
        org.junit.Assert.assertNotNull("Find all method for 'User' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'User' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindUserEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'User' failed to initialize correctly", dod.getRandomUser());
        long count = edu.purdue.cybercenter.dm.domain.User.countUsers();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.User> result = edu.purdue.cybercenter.dm.domain.User.findUserEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'User' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'User' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'User' failed to initialize correctly", dod.getRandomUser());
        edu.purdue.cybercenter.dm.domain.User obj = dod.getNewTransientUser(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'User' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'User' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'User' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.User obj = dod.getRandomUser();
        org.junit.Assert.assertNotNull("Data on demand for 'User' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'User' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.User.findUser(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'User' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.User.findUser(id));
    }
}
