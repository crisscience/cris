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
public class JobContextIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private JobContextDataOnDemand dod;

	@Test
    public void testCountJobContexts() {
        org.junit.Assert.assertNotNull("Data on demand for 'JobContext' failed to initialize correctly", dod.getRandomJobContext());
        long count = edu.purdue.cybercenter.dm.domain.JobContext.countJobContexts();
        org.junit.Assert.assertTrue("Counter for 'JobContext' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindJobContext() {
        edu.purdue.cybercenter.dm.domain.JobContext obj = dod.getRandomJobContext();
        org.junit.Assert.assertNotNull("Data on demand for 'JobContext' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'JobContext' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.JobContext.findJobContext(id);
        org.junit.Assert.assertNotNull("Find method for 'JobContext' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'JobContext' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindJobContextEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'JobContext' failed to initialize correctly", dod.getRandomJobContext());
        long count = edu.purdue.cybercenter.dm.domain.JobContext.countJobContexts();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.JobContext> result = edu.purdue.cybercenter.dm.domain.JobContext.findJobContextEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'JobContext' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'JobContext' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'JobContext' failed to initialize correctly", dod.getRandomJobContext());
        edu.purdue.cybercenter.dm.domain.JobContext obj = dod.getNewTransientJobContext(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'JobContext' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'JobContext' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'JobContext' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.JobContext obj = dod.getRandomJobContext();
        org.junit.Assert.assertNotNull("Data on demand for 'JobContext' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'JobContext' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.JobContext.findJobContext(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'JobContext' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.JobContext.findJobContext(id));
    }
}
