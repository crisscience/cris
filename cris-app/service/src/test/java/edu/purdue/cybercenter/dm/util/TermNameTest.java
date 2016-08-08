/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import java.util.UUID;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author xu222
 */
@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
public class TermNameTest {

    private static final String TemplateVersion = "_template_version";
    private static final String Field = "field";
    private static final String NestedField = "nestedField";
    private static final String Field3 = "field3";
    private static final String Field4 = "field4";
    private static final String uuid = "1b122c30-0eed-11e2-892e-0800200c9a66";
    private static final String version = "1fd77b80-c7c0-11e2-8b8b-0800200c9a66";
    private static final String queryString = "{\"" + TemplateVersion + "\":\"" + version + "\"}";

    @Test
    public void testTerm() {
        String path = uuid;
        String term = path;
        String termWithParenthesis = term + "()";
        String termWithVersion = uuid + "[](" + queryString + ")";

        TermName termName = new TermName(term);
        assertEquals("Term UUID is wrong", UUID.fromString(uuid), termName.getUuid());
        assertEquals("Term version should be null", null, termName.getVersion());
        assertEquals("Term path is wrong", path, termName.getPathString());
        assertEquals("Term is not a list", false, termName.getIsList());
        assertEquals("Term query string should be null", null, termName.getQueryString());
        assertEquals("Term alias is wrong", null, termName.getAlias());

        termName.parse(termWithParenthesis);
        assertEquals("Term UUID is wrong", UUID.fromString(uuid), termName.getUuid());
        assertEquals("Term version should be null", null, termName.getVersion());
        assertEquals("Term path is wrong", path, termName.getPathString());
        assertEquals("Term is not a list", false, termName.getIsList());
        assertEquals("Term query string should be null", null, termName.getQueryString());
        assertEquals("Term alias is wrong", null, termName.getAlias());

        termName.parse(termWithVersion);
        assertEquals("Term UUID is wrong", UUID.fromString(uuid), termName.getUuid());
        assertEquals("Term version is wrong", UUID.fromString(version), termName.getVersion());
        assertEquals("Term path is wrong", path, termName.getPathString());
        assertEquals("Term should be a list", true, termName.getIsList());
        assertEquals("Term query string is wrong", queryString, termName.getQueryString());
        assertEquals("Term alias is wrong", null, termName.getAlias());
    }

    @Test
    public void testField() {
        String path = uuid + "." + Field;
        String fieldWithParenthesis = path + "()";
        String fieldWithVersion = path + "[]({\"" + TemplateVersion + "\":\"" + version + "\"})";

        TermName termName = new TermName(path);
        assertEquals("Field UUID is wrong", UUID.fromString(uuid), termName.getUuid());
        assertEquals("Field version should be null", null, termName.getVersion());
        assertEquals("Field path is wrong", path, termName.getPathString());
        assertEquals("Field is not a list", false, termName.getIsList());
        assertEquals("Field query string should be null", null, termName.getQueryString());
        assertEquals("Field alias is wrong", Field, termName.getAlias());

        termName.parse(fieldWithParenthesis);
        assertEquals("Field UUID is wrong", UUID.fromString(uuid), termName.getUuid());
        assertEquals("Field version should be null", null, termName.getVersion());
        assertEquals("Field path is wrong", path, termName.getPathString());
        assertEquals("Field is not a list", false, termName.getIsList());
        assertEquals("Field query string should be null", null, termName.getQueryString());
        assertEquals("Field alias is wrong", Field, termName.getAlias());

        termName.parse(fieldWithVersion);
        assertEquals("Field UUID is wrong", UUID.fromString(uuid), termName.getUuid());
        assertEquals("Field version is wrong", UUID.fromString(version), termName.getVersion());
        assertEquals("Field path is wrong", path, termName.getPathString());
        assertEquals("Field should be a list", true, termName.getIsList());
        assertEquals("Field query string is wrong", queryString, termName.getQueryString());
        assertEquals("Field alias is wrong", Field, termName.getAlias());
    }

    @Test
    public void testNestedField() {
        String path = uuid + "." + Field + "." + NestedField;
        String nestedFieldWithParenthesis = path + "()";
        String nestedFieldWithVersion = path + "[]({\"" + TemplateVersion + "\":\"" + version + "\"})";

        TermName termName = new TermName(path);
        assertEquals("Nested field UUID is wrong", UUID.fromString(uuid), termName.getUuid());
        assertEquals("Nested field version should be null", null, termName.getVersion());
        assertEquals("Nested field path is wrong", path, termName.getPathString());
        assertEquals("Nested field is not a list", false, termName.getIsList());
        assertEquals("Nested field query string should be null", null, termName.getQueryString());
        assertEquals("Nested field alias is wrong", Field + "." + NestedField, termName.getAlias());

        termName.parse(nestedFieldWithParenthesis);
        assertEquals("Nested field UUID is wrong", UUID.fromString(uuid), termName.getUuid());
        assertEquals("Nested field version should be null", null, termName.getVersion());
        assertEquals("Nested field path is wrong", path, termName.getPathString());
        assertEquals("Nested field is not a list", false, termName.getIsList());
        assertEquals("Nested field query string should be null", null, termName.getQueryString());
        assertEquals("Nested field alias is wrong", Field + "." + NestedField, termName.getAlias());

        termName.parse(nestedFieldWithVersion);
        assertEquals("Nested field UUID is wrong", UUID.fromString(uuid), termName.getUuid());
        assertEquals("Nested field version is wrong", UUID.fromString(version), termName.getVersion());
        assertEquals("Nested field path is wrong", path, termName.getPathString());
        assertEquals("Nested field should be a list", true, termName.getIsList());
        assertEquals("Nested field query string is wrong", queryString, termName.getQueryString());
        assertEquals("Nested field alias is wrong", Field + "." + NestedField, termName.getAlias());
    }

    @Test
    public void testPushPop() {
        String path = uuid + "." + Field + "." + NestedField;
        String nestedFieldWithVersion = path + "({" + TemplateVersion + " : \"" + version + "\"})";

        TermName termName = new TermName(nestedFieldWithVersion);

        termName.push(Field3);
        assertEquals("Nested field path is wrong", path + "." + Field3, termName.getPathString());
        termName.push(Field4);
        assertEquals("Nested field path is wrong", path + "." + Field3 + "." + Field4, termName.getPathString());
        termName.pop();
        assertEquals("Nested field path is wrong", path + "." + Field3, termName.getPathString());
        termName.pop();
        assertEquals("Nested field path is wrong", path, termName.getPathString());
    }

    @Test
    public void testSpecialCases() {
        TermName termName = new TermName(uuid + "[]({" + "})");
        assertEquals("uuid", uuid, termName.getUuid().toString());
        assertEquals("version", null, termName.getVersion());
        assertEquals("pathString", uuid, termName.getPathString());
        assertEquals("alias", null, termName.getAlias());
        assertEquals("queryString", null, termName.getQueryString());
        assertEquals("isLIst", true, termName.getIsList());
        assertEquals("isEmpty", false, termName.isEmpty());
        assertEquals("name", uuid + "[]", termName.getName());

        termName = new TermName(uuid + "[](" + ")");
        assertEquals("uuid", uuid, termName.getUuid().toString());
        assertEquals("version", null, termName.getVersion());
        assertEquals("pathString", uuid, termName.getPathString());
        assertEquals("alias", null, termName.getAlias());
        assertEquals("queryString", null, termName.getQueryString());
        assertEquals("isLIst", true, termName.getIsList());
        assertEquals("isEmpty", false, termName.isEmpty());
        assertEquals("name", uuid + "[]", termName.getName());

        termName = new TermName(uuid + "[]");
        assertEquals("uuid", uuid, termName.getUuid().toString());
        assertEquals("version", null, termName.getVersion());
        assertEquals("pathString", uuid, termName.getPathString());
        assertEquals("alias", null, termName.getAlias());
        assertEquals("queryString", null, termName.getQueryString());
        assertEquals("isLIst", true, termName.getIsList());
        assertEquals("isEmpty", false, termName.isEmpty());
        assertEquals("name", uuid + "[]", termName.getName());

        termName = new TermName(uuid);
        assertEquals("uuid", uuid, termName.getUuid().toString());
        assertEquals("version", null, termName.getVersion());
        assertEquals("pathString", uuid, termName.getPathString());
        assertEquals("alias", null, termName.getAlias());
        assertEquals("queryString", null, termName.getQueryString());
        assertEquals("isLIst", false, termName.getIsList());
        assertEquals("isEmpty", false, termName.isEmpty());
        assertEquals("name", uuid, termName.getName());

        termName = new TermName("");
        assertEquals("uuid", null, termName.getUuid());
        assertEquals("version", null, termName.getVersion());
        assertEquals("pathString", null, termName.getPathString());
        assertEquals("alias", null, termName.getAlias());
        assertEquals("queryString", null, termName.getQueryString());
        assertEquals("isLIst", null, termName.getIsList());
        assertEquals("isEmpty", true, termName.isEmpty());
        assertEquals("name", "", termName.getName());

        termName = new TermName("abc.xyz");
        assertEquals("uuid", null, termName.getUuid());
        assertEquals("version", null, termName.getVersion());
        assertEquals("pathString", "abc.xyz", termName.getPathString());
        assertEquals("alias", "abc.xyz", termName.getAlias());
        assertEquals("queryString", null, termName.getQueryString());
        assertEquals("isLIst", false, termName.getIsList());
        assertEquals("isEmpty", false, termName.isEmpty());
        assertEquals("name", "abc.xyz", termName.getName());
    }
}
