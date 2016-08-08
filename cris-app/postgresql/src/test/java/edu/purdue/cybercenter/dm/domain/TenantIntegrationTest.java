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
public class TenantIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private TenantDataOnDemand dod;

	@Test
    public void testCountTenants() {
        org.junit.Assert.assertNotNull("Data on demand for 'Tenant' failed to initialize correctly", dod.getRandomTenant());
        long count = edu.purdue.cybercenter.dm.domain.Tenant.countTenants();
        org.junit.Assert.assertTrue("Counter for 'Tenant' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindTenant() {
        edu.purdue.cybercenter.dm.domain.Tenant obj = dod.getRandomTenant();
        org.junit.Assert.assertNotNull("Data on demand for 'Tenant' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Tenant' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Tenant.findTenant(id);
        org.junit.Assert.assertNotNull("Find method for 'Tenant' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Tenant' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllTenants() {
        org.junit.Assert.assertNotNull("Data on demand for 'Tenant' failed to initialize correctly", dod.getRandomTenant());
        long count = edu.purdue.cybercenter.dm.domain.Tenant.countTenants();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Tenant', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Tenant> result = edu.purdue.cybercenter.dm.domain.Tenant.findAllTenants();
        org.junit.Assert.assertNotNull("Find all method for 'Tenant' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Tenant' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindTenantEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Tenant' failed to initialize correctly", dod.getRandomTenant());
        long count = edu.purdue.cybercenter.dm.domain.Tenant.countTenants();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Tenant> result = edu.purdue.cybercenter.dm.domain.Tenant.findTenantEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Tenant' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Tenant' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Tenant' failed to initialize correctly", dod.getRandomTenant());
        edu.purdue.cybercenter.dm.domain.Tenant obj = dod.getNewTransientTenant(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Tenant' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Tenant' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Tenant' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Tenant obj = dod.getRandomTenant();
        org.junit.Assert.assertNotNull("Data on demand for 'Tenant' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Tenant' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Tenant.findTenant(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Tenant' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Tenant.findTenant(id));
    }
}
