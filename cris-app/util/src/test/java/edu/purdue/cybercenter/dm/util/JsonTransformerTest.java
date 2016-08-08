/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author xu222
 */
public class JsonTransformerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    public JsonTransformerTest() {
    }

    @Test
    public void testNoTransformation() {
        String jsonIn = "";
        String jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", jsonIn, jsonOut);

        jsonIn = "abcd1234";
        jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", jsonIn, jsonOut);

        jsonIn = "{}";
        jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", jsonIn, jsonOut);

        jsonIn = "[]";
        jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", jsonIn, jsonOut);

        jsonIn = " { } ";
        jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", jsonIn, jsonOut);

        jsonIn = " [ ] ";
        jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", jsonIn, jsonOut);

        jsonIn = "{a : true, b : 1, c: \"xyz\"}";
        jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", jsonIn, jsonOut);

        jsonIn = "[{a : 1}, {a : \"x\"}]";
        jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", jsonIn, jsonOut);
    }

    @Test
    public void testTransformation() {
        // no directive: return the $data object
        String jsonIn = "{$directive:[], $data:[{\"a\":true,\"b\":1},{\"a\":false,\"b\":1,\"c\":\"x\"}], $context:{}}";
        String jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", "[{\"a\":true,\"b\":1},{\"a\":false,\"b\":1,\"c\":\"x\"}]", jsonOut);

        // single directive: return the $data object
        jsonIn = "{$directive:{$merge: {}}, $data:[{\"a\":true,\"b\":1},{\"a\":false,\"c\":\"x\"}], $context:{}}";
        jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", "{\"a\":false,\"b\":1,\"c\":\"x\"}", jsonOut);

        // multiple directive: return the $data object
        jsonIn = "{$directive:[{$merge: {}}], $data:[{\"a\":true,\"b\":1},{\"a\":false,\"b\":1,\"c\":\"x\"}], $context:{}}";
        jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", "{\"a\":false,\"b\":1,\"c\":\"x\"}", jsonOut);
    }

    @Test
    public void testMergeIntoNull() {
        // empty directive: return the $data object
        String jsonIn = "{$directive:{$merge:{$path: \"xxx\"}}, $data:{\"xxx\":[null,{\"a\":false,\"b\":1,\"c\":\"x\"}]}, $context:{}}";
        String jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", "{\"xxx\":{\"a\":false,\"b\":1,\"c\":\"x\"}}", jsonOut);
    }

    @Test
    public void testMergeIntoEmpty() {
        // empty directive: return the $data object
        String jsonIn = "{$directive:{$merge:{$path: \"xxx\"}}, $data:{\"xxx\":[{},{\"a\":false,\"b\":1,\"c\":\"x\"}]}, $context:{}}";
        String jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", "{\"xxx\":{\"a\":false,\"b\":1,\"c\":\"x\"}}", jsonOut);
    }

    @Test
    public void testMergeNullIntoObject() {
        String jsonIn = "{$directive:{$merge:{$path: \"xxx\"}}, $data:{\"xxx\":[{\"a\":false,\"b\":1,\"c\":\"x\"}, null]}, $context:{}}";
        String jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", "{\"xxx\":{\"a\":false,\"b\":1,\"c\":\"x\"}}", jsonOut);
    }

    @Test
    public void testMergeNestedNullIntoObject() {
        String jsonIn = "{$directive:{$merge:{$path: \"xxx\"}}, $data:{\"xxx\":[{\"a\":false,\"b\":1,\"c\":\"x\",\"d\":{\"a\":false,\"b\":1,\"c\":\"x\"}}, {\"a\":false,\"b\":1,\"c\":\"x\",\"d\":null}]}, $context:{}}";
        String jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", "{\"xxx\":{\"a\":false,\"b\":1,\"c\":\"x\",\"d\":{\"a\":false,\"b\":1,\"c\":\"x\"}}}", jsonOut);
    }

    @Test
    public void testMergeNullIntoPrimitive() {
        String jsonIn = "{$directive:{$merge:{$path: \"xxx\"}}, $data:{\"xxx\":[{\"a\":false,\"b\":1,\"c\":\"x\"}, {\"a\":null,\"b\":null,\"c\":null}]}, $context:{}}";
        String jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", "{\"xxx\":{\"a\":null,\"b\":null,\"c\":null}}", jsonOut);
    }

    @Test
    public void testNestedData() {
        // empty directive: return the $data object
        String jsonIn = "{$directive:{$merge:{$path: \"xxx\"}}, $data:{\"xxx\":[{\"a\":true,\"b\":1},{\"a\":false,\"b\":1,\"c\":\"x\"}]}, $context:{}}";
        String jsonOut = JsonTransformer.transformJson(jsonIn);
        Assert.assertEquals("should be equal", "{\"xxx\":{\"a\":false,\"b\":1,\"c\":\"x\"}}", jsonOut);
    }

    @Test
    public void testEmptyDirective() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("empty directive");

        // empty directive: return the $data object
        String jsonIn = "{$directive:{}, $data:[{\"a\":true,\"b\":1},{\"a\":false,\"b\":1,\"c\":\"x\"}], $context:{}}";
        JsonTransformer.transformJson(jsonIn);

    }
}
