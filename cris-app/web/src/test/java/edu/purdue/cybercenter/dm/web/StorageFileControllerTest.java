package edu.purdue.cybercenter.dm.web;

import java.io.FileInputStream;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author xu222
 */
public class StorageFileControllerTest extends BaseWithAdminUserControllerTest {

    //@Test
    public void uploadFiles() throws Exception {
        /*
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/ifupload").accept(MediaType.ALL).session(httpSession);
        */
        MockMultipartHttpServletRequestBuilder mockHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/ifupload").accept(MediaType.ALL).session(httpSession);
        MockMultipartFile mf11 = new MockMultipartFile("group_l_files", "hs_err_pid2656.log", "text/log", new FileInputStream("hs_err_pid2656.log"));
        MockMultipartFile mf12 = new MockMultipartFile("group_1_files", "nb-configuration.xml", "text/xml", new FileInputStream("nb-configuration.xml"));
        MockMultipartFile mf13 = new MockMultipartFile("group_1_files", "pom.xml", "text/xml", new FileInputStream("pom.xml"));
        mockHttpServletRequestBuilder.file(mf11);
        mockHttpServletRequestBuilder.file(mf12);
        mockHttpServletRequestBuilder.file(mf13);
        MockMultipartFile mf21 = new MockMultipartFile("group_2_files", "ehcache.xml", "text/xml", new FileInputStream("src/test/resources/META-INF/spring/ehcache.xml"));
        MockMultipartFile mf22 = new MockMultipartFile("group_2_files", "applicationContext-security.xml", "text/html", new FileInputStream("src/test/resources/META-INF/spring/applicationContext-security.xml"));
        MockMultipartFile mf23 = new MockMultipartFile("group_2_files", "applicationContext.xml", "text/html", new FileInputStream("src/test/resources/META-INF/spring/applicationContext.xml"));
        mockHttpServletRequestBuilder.file(mf21);
        mockHttpServletRequestBuilder.file(mf22);
        mockHttpServletRequestBuilder.file(mf23);

        // perform action
        ResultActions resultActions = this.mockMvc.perform(mockHttpServletRequestBuilder);
        ModelAndView mav = resultActions.andReturn().getModelAndView();
        Map<String, Object> model = mav.getModel();
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        assertNotNull(contents);
    }

    @Test
    public void downloadFiles() throws Exception {
    }

    @Test
    public void deleteFile() {
    }
}
