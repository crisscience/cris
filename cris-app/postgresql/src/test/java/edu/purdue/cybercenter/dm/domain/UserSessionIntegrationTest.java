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
public class UserSessionIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private UserSessionDataOnDemand dod;

	@Test
    public void testCountUserSessions() {
        org.junit.Assert.assertNotNull("Data on demand for 'UserSession' failed to initialize correctly", dod.getRandomUserSession());
        long count = edu.purdue.cybercenter.dm.domain.UserSession.countUserSessions();
        org.junit.Assert.assertTrue("Counter for 'UserSession' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindUserSession() {
        edu.purdue.cybercenter.dm.domain.UserSession obj = dod.getRandomUserSession();
        org.junit.Assert.assertNotNull("Data on demand for 'UserSession' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'UserSession' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.UserSession.findUserSession(id);
        org.junit.Assert.assertNotNull("Find method for 'UserSession' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'UserSession' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllUserSessions() {
        org.junit.Assert.assertNotNull("Data on demand for 'UserSession' failed to initialize correctly", dod.getRandomUserSession());
        long count = edu.purdue.cybercenter.dm.domain.UserSession.countUserSessions();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'UserSession', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.UserSession> result = edu.purdue.cybercenter.dm.domain.UserSession.findAllUserSessions();
        org.junit.Assert.assertNotNull("Find all method for 'UserSession' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'UserSession' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindUserSessionEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'UserSession' failed to initialize correctly", dod.getRandomUserSession());
        long count = edu.purdue.cybercenter.dm.domain.UserSession.countUserSessions();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.UserSession> result = edu.purdue.cybercenter.dm.domain.UserSession.findUserSessionEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'UserSession' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'UserSession' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'UserSession' failed to initialize correctly", dod.getRandomUserSession());
        edu.purdue.cybercenter.dm.domain.UserSession obj = dod.getNewTransientUserSession(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'UserSession' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'UserSession' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'UserSession' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.UserSession obj = dod.getRandomUserSession();
        org.junit.Assert.assertNotNull("Data on demand for 'UserSession' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'UserSession' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.UserSession.findUserSession(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'UserSession' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.UserSession.findUserSession(id));
    }
}
