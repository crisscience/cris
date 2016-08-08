/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author xu222
 */
public class HelperTest {

    public HelperTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of streamToFile method, of class Helper.
     */
    @Test
    public void testStreamToFile() throws Exception {
    }

    /**
     * Test of fileToString method, of class Helper.
     */
    @Test
    public void testFileToString() {
    }

    /**
     * Test of streamToString method, of class Helper.
     */
    @Test
    public void testStreamToString() {
    }

    /**
     * Test of serialize method, of class Helper.
     */
    @Test
    public void testSerialize() {
        String result = Helper.serialize(null);
        Assert.assertEquals("null", result);

        result = Helper.serialize(true);
        Assert.assertEquals("true", result);

        result = Helper.serialize(false);
        Assert.assertEquals("false", result);

        result = Helper.serialize(1234.5678);
        Assert.assertEquals("1234.5678", result);


        result = Helper.serialize("1234abcd");
        Assert.assertEquals("\"1234abcd\"", result);
    }

    /**
     * Test of deepSerialize method, of class Helper.
     */
    @Test
    public void testDeepSerialize() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("null", null);
        nested.put("true", true);
        nested.put("false", false);
        nested.put("double", 1234.5678);
        nested.put("string", "1234abcd");

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(nested);

        Map<String, Object> mapRegular = new HashMap<>();
        mapRegular.put("null", null);
        mapRegular.put("true", true);
        mapRegular.put("false", false);
        mapRegular.put("double", 1234.5678);
        mapRegular.put("string", "1234abcd");
        mapRegular.put("nested", nested);

        Map<String, Object> mapDeep = new HashMap<>();
        mapDeep.putAll(mapRegular);
        mapDeep.put("list", list);

        Map resultRegular = Helper.deserialize(Helper.serialize(mapDeep), Map.class);
        Map resultDeep = Helper.deserialize(Helper.deepSerialize(mapDeep), Map.class);
        Assert.assertTrue(resultRegular.equals(mapRegular));
        Assert.assertTrue(resultDeep.equals(mapDeep));
    }

    /**
     * Test of deserialize method, of class Helper.
     */
    @Test
    public void testDeserializeObject() {
        Object result = Helper.deserialize(" null ", Object.class);
        Assert.assertEquals(null, result);

        result = Helper.deserialize("true", Boolean.class);
        Assert.assertEquals(Boolean.TRUE, result);

        result = Helper.deserialize("false", Boolean.class);
        Assert.assertEquals(Boolean.FALSE, result);

        result = Helper.deserialize("1234.5678", Double.class);
        Assert.assertEquals(1234.5678, result);

        result = Helper.deserialize("\"1234abcd\"", String.class);
        Assert.assertEquals("1234abcd", result);
    }

    @Test
    public void testDeserializeList() {
        List<Map> resultList = Helper.deserialize("[{\"a\" : true}, {\"a\" : 1}, {\"a\" : \"xyz\"}]", List.class);
        Assert.assertEquals(true, resultList.get(0).get("a"));
        Assert.assertEquals(1, resultList.get(1).get("a"));
        Assert.assertEquals("xyz", resultList.get(2).get("a"));
    }

}
