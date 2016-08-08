/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 *
 * @author xu222
 */
public class ProductBuildInfo {

    private String major;
    private String minor;
    private String patch;
    private String type;
    private String buildNumber;
    private String buildId;
    private String buildTag;
    private Date buildTimestamp;
    private String jobName;
    private String executorNumber;
    private String javaHome;
    private String workspace;
    private String revision;

    public ProductBuildInfo() {
        Resource resource = new ClassPathResource("/build.properties");
        try {
            Properties props = PropertiesLoaderUtils.loadProperties(resource);
            this.major = props.getProperty("version.major");
            this.minor = props.getProperty("version.minor");
            this.patch = props.getProperty("version.patch");
            this.type = props.getProperty("version.type");
            this.buildNumber = props.getProperty("jenkins.build.number");
            this.buildId = props.getProperty("jenkins.build.id");
            this.buildTag = props.getProperty("jenkins.build.tag");
            this.jobName = props.getProperty("jenkins.job.name");
            this.executorNumber = props.getProperty("jenkins.executor.number");
            this.javaHome = props.getProperty("jenkins.java.home");
            this.workspace = props.getProperty("jenkins.workspace");
            this.revision = props.getProperty("jenkins.svn.revision");

            if (this.buildId == null || this.buildId.isEmpty()) {
                this.buildTimestamp = new Date();
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                this.buildTimestamp = sdf.parse(this.buildId);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load property file: /build.properties: " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            if ("${BUILD_NUMBER}".equals(this.buildNumber)) {
                // not by build server: use the current date/time
                this.buildTimestamp = new Date();
            } else {
                // by build server
                throw new RuntimeException("Invalid build ID: " + this.buildId, ex);
            }
        }
    }

    /**
     * @return the major
     */
    public String getMajor() {
        return major;
    }

    /**
     * @return the minor
     */
    public String getMinor() {
        return minor;
    }

    /**
     * @return the patch
     */
    public String getPatch() {
        return patch;
    }

    /**
     * @return the buildType
     */
    public String getBuildType() {
        return type;
    }

    /**
     * @return the buildNumber
     */
    public String getBuildNumber() {
        return buildNumber;
    }

    /**
     * @return the buildId
     */
    public String getBuildId() {
        return buildId;
    }

    /**
     * @return the buildTag
     */
    public String getBuildTag() {
        return buildTag;
    }

    /**
     * @return the buildTimestamp
     */
    public Date getBuildTimestamp() {
        return buildTimestamp;
    }

    /**
     * @return the jobName
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * @return the executorNumber
     */
    public String getExecutorNumber() {
        return executorNumber;
    }

    /**
     * @return the javaHome
     */
    public String getJavaHome() {
        return javaHome;
    }

    /**
     * @return the workspace
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * @return the revision
     */
    public String getRevision() {
        return revision;
    }
}
