package edu.purdue.cybercenter.dm.web;

import org.junit.Before;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author xu222
 */
@EnableWebMvc
@ComponentScan(basePackages = "edu.purdue.cybercenter.dm.web")
public class BaseWithNormalUserControllerTest extends BaseControllerTest {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        setupWorkspace();

        loginNormalUser();
    }
}
