package edu.purdue.cybercenter.dm.web;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.junit.BeforeClass;

public class BaseTest {

    // Make setUpDataSource() be executed only once.
    private static boolean started = false;

    @BeforeClass
    public static void setUpDataSource() throws Exception {
        if (started) {
            return;
        } else {
            started = true;
        }

        try {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES,
                    "org.apache.naming");

            InitialContext ic = new InitialContext();
            ic.createSubcontext("java:");
            ic.createSubcontext("java:comp");
            ic.createSubcontext("java:comp/env");
            ic.createSubcontext("java:comp/env/jdbc");

            Properties properties = new Properties();
            properties.setProperty("url", "jdbc:postgresql://localhost:5432/test");
            properties.setProperty("maxActive", "10");
            properties.setProperty("maxIdle", "8");
            properties.setProperty("minIdle", "10");
            properties.setProperty("maxWait", "10");
            properties.setProperty("testOnBorrow", "true");
            properties.setProperty("username", "test");
            properties.setProperty("password", "c1234c");
            properties.setProperty("validationQuery", "SELECT 1");
            properties.setProperty("removeAbandoned", "true");
            properties.setProperty("removeAbandonedTimeout", "1");
            properties.setProperty("logAbandoned", "true");

            DataSource ds = BasicDataSourceFactory.createDataSource(properties);
            ic.bind("java:comp/env/jdbc/cris", ds);
        } catch (NamingException ex) {
            System.exit(-1);
        }
    }
}
