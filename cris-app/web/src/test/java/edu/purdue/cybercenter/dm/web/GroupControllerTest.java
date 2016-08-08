package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Constant;
import edu.purdue.cybercenter.dm.util.Helper;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author rangars
 */
public class GroupControllerTest extends BaseWithAdminUserControllerTest {

    private static final String newGroup = "{name: \"demoGroup\", description: \"This is a demo\", enabled: true}";
    private static final String operationalGroup = "{\"admin\":true,\"classificationId\":{\"$ref\":\"/classifications/1\",\"code\":\"1\",\"creatorId\":null,\"description\":\"PUCCR Member\",\"name\":\"Member\",\"tenantId\":1,\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null},\"creatorId\":null,\"description\":\"\",\"email\":null,\"enabled\":true,\"id\":1001,\"imageId\":null,\"name\":\"Mass Spectrometry Technician Group \",\"ownerId\":{\"$ref\":\"/users/1\",\"accountNonExpired\":true,\"accountNonLocked\":true,\"admin\":true,\"creatorId\":null,\"credentialsNonExpired\":true,\"email\":\"george.washington@whitehouse.gov\",\"enabled\":true,\"externalId\":null,\"externalSource\":null,\"firstName\":\"george\",\"imageId\":null,\"lastName\":\"washington\",\"middleName\":\"\",\"password\":\"2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb\",\"salt\":\"93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da\",\"tenantId\":1,\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null,\"username\":\"george.washington\"},\"tenantId\":1,\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null}";
    private static final String deprecatedGroup = "{\"admin\":false,\"classificationId\":{\"$ref\":\"/classifications/1\",\"code\":\"1\",\"creatorId\":null,\"description\":\"PUCCR Member\",\"name\":\"Member\",\"tenantId\":1,\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null},\"creatorId\":null,\"description\":\"\",\"email\":null,\"enabled\":false,\"id\":1005,\"imageId\":null,\"name\":\"Test Group\",\"ownerId\":{\"$ref\":\"/users/1\",\"accountNonExpired\":true,\"accountNonLocked\":true,\"admin\":true,\"creatorId\":null,\"credentialsNonExpired\":true,\"email\":\"george.washington@whitehouse.gov\",\"enabled\":true,\"externalId\":null,\"externalSource\":null,\"firstName\":\"george\",\"imageId\":null,\"lastName\":\"washington\",\"middleName\":\"\",\"password\":\"2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb\",\"salt\":\"93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da\",\"tenantId\":1,\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null,\"username\":\"george.washington\"},\"tenantId\":1,\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null}";
    private static final String operationalGroupId = "1001";
    private static final String deprecatedGroupId = "1005";

    @BeforeClass
    public static void setupClass() throws Exception {
    }

    private void validateGroupInfo(Map<String, Object> expectedMap, Map<String, Object> groupMap){
        if(groupMap == null || groupMap.isEmpty())
            throw new RuntimeException("Improper Inputs");

        assertEquals(14, groupMap.size());
        assertEquals("Id", expectedMap.get("id"), groupMap.get("id"));
        assertEquals("Name", expectedMap.get("name"), groupMap.get("name"));
        assertEquals("OwnerId", ((Map<String, Object>) expectedMap.get("ownerId")).get("$ref"), ((Map<String, Object>) groupMap.get("ownerId")).get("$ref"));
        assertEquals("description", expectedMap.get("description"), groupMap.get("description"));
        assertEquals("ClassificationId", ((Map<String, Object>) expectedMap.get("classificationId")).get("code"), ((Map<String, Object>) groupMap.get("classificationId")).get("code"));
        assertEquals("tenantId", expectedMap.get("tenantId"), groupMap.get("tenantId"));
        assertEquals("Enabled", expectedMap.get("enabled"), groupMap.get("enabled"));
    }

    /*
     * 1. create a group
     *    * expected status code: status().isCreated()
     *    * expected response content: newly created group with id assigned
     */
    @Test
    public void testCreateGroup() throws Exception{
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/groups").content(newGroup).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isCreated());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /*
     * 2. retrieve an exiting group
     *    * expected status code: status().isOk()
     *    * expected response content: content
    */

    @Test
    public void testRetrieveGroup() throws Exception{
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/groups/"+operationalGroupId).accept(MediaType.APPLICATION_JSON).session(httpSession);//operationalGroupId
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        validateGroupInfo(Helper.deserialize(operationalGroup, Map.class), Helper.deserialize(resultActions.andReturn().getResponse().getContentAsString(), Map.class));

        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }
    /*
     * 3. update an existing group
     *    * expected status code: status().isOk()
     *    * expected response content: updated content
    */
    @Test
    public void testUpdateGroup() throws Exception{
        Map<String, Object> groupMap = Helper.deserialize(operationalGroup, Map.class);
        groupMap.put("description", "This is a demo. Another Update.");
        groupMap.put("name", "demoGroup with Update");

        Map<String, Object> expectedMap = Helper.deserialize(operationalGroup, Map.class);
        expectedMap.put("description", "This is a demo. Another Update.");
        expectedMap.put("name", "demoGroup with Update");

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/groups/"+operationalGroupId).content(Helper.deepSerialize(groupMap)).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        validateGroupInfo(expectedMap, Helper.deserialize(resultActions.andReturn().getResponse().getContentAsString(), Map.class));

        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }
    /*
     * 4. deprecate an existing operational group
     *    * expected status code: status().isOk()
     *    * expected response content: updated content
    */
    @Test
    public void testDeprecateGroup() throws Exception{
        Map<String, Object> expectedMap = Helper.deserialize(operationalGroup, Map.class);
        expectedMap.put("enabled", false);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/groups/"+operationalGroupId).content(Helper.deepSerialize(expectedMap)).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        validateGroupInfo(expectedMap, Helper.deserialize(resultActions.andReturn().getResponse().getContentAsString(), Map.class));

        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }
    /*
     * 5. restore an existing deprecated group
     *    * expected status code: status().isOk()
     *    * expected response content: updated content
     */
    @Test
    public void testRestoreGroup() throws Exception{
        Map<String, Object> expectedMap = Helper.deserialize(deprecatedGroup, Map.class);
        expectedMap.put("enabled", true);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/groups/"+operationalGroupId).content(Helper.deepSerialize(expectedMap)).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        validateGroupInfo(expectedMap, Helper.deserialize(resultActions.andReturn().getResponse().getContentAsString(), Map.class));

        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }
    /*
     * 6. retrieve a non-exist group
     *    * expected status code: status().isNotFound()
     *    * expected response content: no content
    */
    @Test
    public void testRetrieveNonExistGroup() throws Exception{
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/groups/1006").accept(MediaType.APPLICATION_JSON).session(httpSession);//operationalGroupId
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());

        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }
    /*
     * 7. update a non-exist group
     *    * expected status code: status().isNotFound()
     *    * expected response content: no content
    */
    @Ignore
    @Test
    public void testUpdateNonExistGroup() throws Exception{
        String groupUpdate = "{id: 1006, description: \"This is a demo. Another Update.\", name: \"demoGroup with Update\"}";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/groups/1006").content(groupUpdate).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());

        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }
    /*
     * 8. deprecate a non-exist group
     *    * expected status code: status().isNotFound()
     *    * expected response content: no content
    */
    @Ignore
    @Test
    public void testDeprecateNonExistGroup() throws Exception{
        String groupUpdate = "{id: 1006,\"enabled\":false}";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/groups/1006").content(groupUpdate).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());

        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }
    /*
     * 9. restore a non-exist group
     *    * expected status code: status().isNotFound()
     *    * expected response content: no content
     */
    @Ignore
    @Test
    public void testRestoreNonExistGroup() throws Exception{
        String groupUpdate = "{id: 1006, \"enabled\":true}";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/groups/1006").content(groupUpdate).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());

        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }

    /*
     * 10. add an existing user to an existing group
     *    * expected status code: status().isOk()
     *    * expected response content: no content
     */
    @Ignore
    @Test
    public void testAddUserToGroup() throws Exception{
        String userIdToBeAdded = "6";
        String groupId = "1000";
        String addUser = "{\"groupId\": {$ref: \"/groups/"+ groupId+"\"}, \"userId\": {$ref: \"/users/" + userIdToBeAdded + "\"}}";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/groups/add?groupId=" + groupId + "&userId="+userIdToBeAdded).content(addUser).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }
    /*
     * 11. remove an existing user from an existing group
     *    * expected status code: status().isOk()
     *    * expected response content: no content
     */
    @Ignore
    @Test
    public void testRemoveUserToGroup() throws Exception{
        String userIdToBeRemoved = "2";
        String groupId = "1000";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = delete("/groups/remove?groupId=" + groupId + "&userId="+userIdToBeRemoved).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /*
     * 12. add a non-existing user to an existing group
     *    * expected status code: status().isNotFound()
     *    * expected response content: informative error message
     */
    @Ignore
    @Test
    public void testAddNonExistUserToGroup() throws Exception{
        String userIdToBeAdded = "1000";// non exist user
        String groupId = "1000";
        String addUser = "{\"groupId\": {$ref: \"/groups/"+ groupId+"\"}, \"userId\": {$ref: \"/users/" + userIdToBeAdded + "\"}}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/groups/add?groupId=" + groupId + "&userId="+userIdToBeAdded).content(addUser).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());
    }
    /*
     * 13. remove a non-existing user from an existing group
     *    * expected status code: status().isNotFound()
     *    * expected response content: informative error message
     */
    @Ignore
    @Test
    public void testRemoveNonExistUserToGroup() throws Exception{
        String userIdToBeRemoved = "1000"; // non exist user
        String groupId = "1000";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = delete("/groups/remove?groupId=" + groupId + "&userId="+userIdToBeRemoved).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());
    }
    /*
     * 14. add an existing user to a non-existing group
     *    * expected status code: status().isNotFound()
     *    * expected response content: informative error message
     */
    @Ignore
    @Test
    public void testAddUserToNonExistGroup() throws Exception{
        String userIdToBeAdded = "6";
        String groupId = "5000";// Non exist Group
        String addUser = "{\"groupId\": {$ref: \"/groups/"+ groupId+"\"}, \"userId\": {$ref: \"/users/" + userIdToBeAdded + "\"}}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/groups/add?groupId=" + groupId + "&userId="+userIdToBeAdded).content(addUser).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());
    }
    /*
     * 15. remove an existing user from a non-existing group
     *    * expected status code: status().isNotFound()
     *    * expected response content: informative error message
     */
    @Ignore
    @Test
    public void testRemoveUserToNonExistGroup() throws Exception{
        String userIdToBeRemoved = "6";
        String groupId = "5000";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = delete("/groups/remove?groupId=" + groupId + "&userId="+userIdToBeRemoved).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());
    }
    /*
     * 16. add a non-existing user to a non-existing group
     *    * expected status code: status().isNotFound()
     *    * expected response content: informative error message
     */
    @Ignore
    @Test
    public void testAddNonExistUserToNonExistGroup() throws Exception{
        String userIdToBeAdded = "1000";// Non exist Group
        String groupId = "5000";// Non exist Group
        String addUser = "{\"groupId\": {$ref: \"/groups/"+ groupId+"\"}, \"userId\": {$ref: \"/users/" + userIdToBeAdded + "\"}}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/groups/add?groupId=" + groupId + "&userId="+userIdToBeAdded).content(addUser).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());
    }
    /*
     * 17. remove a non-existing user from a non-existing group
     *    * expected status code: status().isNotFound()
     *    * expected response content: informative error message
     */
    @Ignore
    @Test
    public void testRemoveNonExistUserToNonExistGroup() throws Exception{
        String userIdToBeRemoved = "1000";// Non exist user
        String groupId = "5000";// Non exist group

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = delete("/groups/remove?groupId=" + groupId + "&userId="+userIdToBeRemoved).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());
    }
    /*
     * 18. set an existing user as the owner of an existing group of which the user is a member
     *    * expected status code: status().isOk()
     *    * expected response content: no content
     */
    @Ignore
    @Test
    public void testSetUserAsGroupAdmin() throws Exception{
        String userIdToBeMadeAdmin = "2";
        String groupId = "1000";
        String addUserAsAdmin = "{\"groupId\": {$ref: \"/groups/"+ groupId+"\"}, \"userId\": {$ref: \"/users/" + userIdToBeMadeAdmin + "\"}}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/groups/admin?groupId=" + groupId + "&userId="+userIdToBeMadeAdmin).content(addUserAsAdmin).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    /*
     * 19. set an existing user as the owner of an existing group of which the user is a NOT member
     *    * expected status code: status().isNotFound()
     *    * expected response content: informative error message
     */
    @Ignore
    @Test
    public void testSetUserAsNonMemberGroupAdmin() throws Exception{
        String userIdToBeMadeAdmin = "6"; // Non member
        String groupId = "1000";
        String addUserAsAdmin = "{\"groupId\": {$ref: \"/groups/"+ groupId+"\"}, \"userId\": {$ref: \"/users/" + userIdToBeMadeAdmin + "\"}}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/groups/admin?groupId=" + groupId + "&userId="+userIdToBeMadeAdmin).content(addUserAsAdmin).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());
    }
    /*
     * 20. set a non-existing user as the owner of an existing group
     *    * expected status code: status().isNotFound()
     *    * expected response content: informative error message
     */
    @Ignore
    @Test
    public void testSetNonExistUserAsGroupOwner() throws Exception{
        String userIdToBeMadeAdmin = "1000";// Non exist user
        String groupId = "1000";
        String addUserAsAdmin = "{\"groupId\": {$ref: \"/groups/"+ groupId+"\"}, \"userId\": {$ref: \"/users/" + userIdToBeMadeAdmin + "\"}}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/groups/admin?groupId=" + groupId + "&userId="+userIdToBeMadeAdmin).content(addUserAsAdmin).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());
    }
    /*
     * 21. set an existing user as the owner of a non-existing group
     *    * expected status code: status().isNotFound()
     *    * expected response content: informative error message
     */
    @Ignore
    @Test
    public void testSetUserAsNonExistGroupOwner() throws Exception{
        String userIdToBeMadeAdmin = "1";
        String groupId = "5000"; // Non exist group
        String addUserAsAdmin = "{\"groupId\": {$ref: \"/groups/"+ groupId+"\"}, \"userId\": {$ref: \"/users/" + userIdToBeMadeAdmin + "\"}}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/groups/admin?groupId=" + groupId + "&userId="+userIdToBeMadeAdmin).content(addUserAsAdmin).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());
    }
    /*
     * 22. set a non-existing user as the owner of a non-existing group
     *    * expected status code: status().isNotFound()
     *    * expected response content: informative error message
     */
    @Ignore
    @Test
    public void testSetNonExistUserAsNonExistGroupOwner() throws Exception{
        String userIdToBeMadeAdmin = "1000"; //Non exist user
        String groupId = "5000"; // Non exist group
        String addUserAsAdmin = "{\"groupId\": {$ref: \"/groups/"+ groupId+"\"}, \"userId\": {$ref: \"/users/" + userIdToBeMadeAdmin + "\"}}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/groups/admin?groupId=" + groupId + "&userId="+userIdToBeMadeAdmin).content(addUserAsAdmin).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isNotFound());
    }

    @Ignore
    @Test
    public void adminShouldBeAbleToAddAndRemoveUserFromGroup() throws Exception {
        createTestWorkspace();
        createTestUser();
        Integer userId = enableTestUser();
        Integer groupId = createTestGroup();
        MockHttpServletRequestBuilder  mockHttpServletRequestBuilder = get("/testws").session(httpSession);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());
        assertNotNull(httpSession.getAttribute("tenantId"));

        login(Constant.AdminUsername, "password");

        mockHttpServletRequestBuilder = get("/users/?groupId=" + groupId + "&sort=+username").session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        String content = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> map = Helper.deserialize(content, List.class);

        assertEquals(map.size(), 0);

        mockHttpServletRequestBuilder = post("/groupusers").accept(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":{\"$ref\":\"/groups/" + groupId + "\"},\"userId\":{\"$ref\":\"/users/" + userId + "\"}}")
            .session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockHttpServletRequestBuilder = get("/users/?groupId=" + groupId + "&sort=+username").session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        content = resultActions.andReturn().getResponse().getContentAsString();
        map = Helper.deserialize(content, List.class);
        assertEquals(map.size(), 1);
        assertEquals(map.get(0).get("id"), userId);

        mockHttpServletRequestBuilder = get("/groupusers/?groupId=" + groupId + "&userId=" + userId).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        content = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> hashmap = Helper.deserialize(content, Map.class);
        mockHttpServletRequestBuilder = delete("/groupusers/" + hashmap.get("id")).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        mockHttpServletRequestBuilder = get("/users/?groupId=" + groupId + "&sort=+username").session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        content = resultActions.andReturn().getResponse().getContentAsString();
        map = Helper.deserialize(content, List.class);

        assertEquals(map.size(), 0);
    }

    private Integer createTestGroup() throws Exception {
        MockHttpServletRequestBuilder  mockHttpServletRequestBuilder = get("/testws").accept(MediaType.APPLICATION_JSON).session(httpSession);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());
        assertNotNull(httpSession.getAttribute("tenantId"));

        login(Constant.AdminUsername, "password");
        mockHttpServletRequestBuilder = post("/groups").accept(MediaType.APPLICATION_JSON)
                .content("{name : \"Test Group\", description : \"This is a test group.\"}")
                .session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockHttpServletRequestBuilder = get("/groups?sort=+name").session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        String content = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> map = Helper.deserialize(content, List.class);
        Integer groupId = null;
        for (Map<String, Object> stringObjectMap : map) {
            if (stringObjectMap.get("name").equals("Test Group")) {
                groupId = (Integer) stringObjectMap.get("id");
            }
        }

        logout();

        return groupId;
    }

    private Integer enableTestUser() throws Exception {
        login(Constant.AdminUsername, "password");
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/users?sort=+lastName").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        String content = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> map = Helper.deserialize(content, List.class);
        Integer userId = null;
        for (Map<String, Object> stringObjectMap : map) {
            if (stringObjectMap.get("username").equals("test@person.com")) {
                userId = (Integer) stringObjectMap.get("id");
            }
        }
        assertNotNull(userId);
        mockHttpServletRequestBuilder = get("/users/" + userId).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        content = resultActions.andReturn().getResponse().getContentAsString();
        content = content.replace("\"enabled\":false", "\"enabled\":true");
        mockHttpServletRequestBuilder = put("/users/" + userId).content(content).accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        content = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("GCT: ******************************************" + content + "******************************************");
        assertEquals(-1, content.indexOf("error"));

        logout();
        return userId;
    }


    private void createTestUser() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/auth/signup")
                .param("firstName", "Test")
                .param("middleName", "Space")
                .param("lastName", "Person")
                .param("email", "test@person.com")
                .param("username", "test@person.com")
                .param("password", "password")
                .param("password2", "password")
                .session(httpSession);


        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());

        Thread.sleep(100);
    }

    private void createTestWorkspace() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/tenants/create")
                .param("urlIdentifier", "testws")
                .param("name", "Test Workspace")
                .param("password1", "password")
                .param("password2", "password")
                .session(httpSession);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());

        mockHttpServletRequestBuilder = get("/tenants/summary?urlIdentifier=testws&name=Test+Workspace").session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockHttpServletRequestBuilder = get("/testws").session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());
        assertNotNull(httpSession.getAttribute("tenantId"));
    }

}
