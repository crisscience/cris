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
public class JobIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private JobDataOnDemand dod;

	@Test
    public void testCountJobs() {
        org.junit.Assert.assertNotNull("Data on demand for 'Job' failed to initialize correctly", dod.getRandomJob());
        long count = edu.purdue.cybercenter.dm.domain.Job.countJobs();
        org.junit.Assert.assertTrue("Counter for 'Job' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindJob() {
        edu.purdue.cybercenter.dm.domain.Job obj = dod.getRandomJob();
        org.junit.Assert.assertNotNull("Data on demand for 'Job' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Job' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Job.findJob(id);
        org.junit.Assert.assertNotNull("Find method for 'Job' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Job' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllJobs() {
        org.junit.Assert.assertNotNull("Data on demand for 'Job' failed to initialize correctly", dod.getRandomJob());
        long count = edu.purdue.cybercenter.dm.domain.Job.countJobs();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Job', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Job> result = edu.purdue.cybercenter.dm.domain.Job.findAllJobs();
        org.junit.Assert.assertNotNull("Find all method for 'Job' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Job' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindJobEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Job' failed to initialize correctly", dod.getRandomJob());
        long count = edu.purdue.cybercenter.dm.domain.Job.countJobs();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Job> result = edu.purdue.cybercenter.dm.domain.Job.findJobEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Job' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Job' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Job' failed to initialize correctly", dod.getRandomJob());
        edu.purdue.cybercenter.dm.domain.Job obj = dod.getNewTransientJob(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Job' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Job' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Job' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Job obj = dod.getRandomJob();
        org.junit.Assert.assertNotNull("Data on demand for 'Job' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Job' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Job.findJob(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Job' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Job.findJob(id));
    }
}
