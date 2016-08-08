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
public class SessionIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private SessionDataOnDemand dod;

	@Test
    public void testCountSessions() {
        org.junit.Assert.assertNotNull("Data on demand for 'Session' failed to initialize correctly", dod.getRandomSession());
        long count = edu.purdue.cybercenter.dm.domain.Session.countSessions();
        org.junit.Assert.assertTrue("Counter for 'Session' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindSession() {
        edu.purdue.cybercenter.dm.domain.Session obj = dod.getRandomSession();
        org.junit.Assert.assertNotNull("Data on demand for 'Session' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Session' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Session.findSession(id);
        org.junit.Assert.assertNotNull("Find method for 'Session' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Session' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllSessions() {
        org.junit.Assert.assertNotNull("Data on demand for 'Session' failed to initialize correctly", dod.getRandomSession());
        long count = edu.purdue.cybercenter.dm.domain.Session.countSessions();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Session', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Session> result = edu.purdue.cybercenter.dm.domain.Session.findAllSessions();
        org.junit.Assert.assertNotNull("Find all method for 'Session' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Session' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindSessionEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Session' failed to initialize correctly", dod.getRandomSession());
        long count = edu.purdue.cybercenter.dm.domain.Session.countSessions();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Session> result = edu.purdue.cybercenter.dm.domain.Session.findSessionEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Session' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Session' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Session' failed to initialize correctly", dod.getRandomSession());
        edu.purdue.cybercenter.dm.domain.Session obj = dod.getNewTransientSession(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Session' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Session' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Session' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Session obj = dod.getRandomSession();
        org.junit.Assert.assertNotNull("Data on demand for 'Session' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Session' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Session.findSession(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Session' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Session.findSession(id));
    }
}
