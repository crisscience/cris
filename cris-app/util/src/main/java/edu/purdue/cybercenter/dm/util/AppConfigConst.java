/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

/**
 *
 * @author xu222
 */
public final class AppConfigConst {

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static final String TMP_PATH = "tmpPath";                    // from configuration
    public static final String APP_TMP_PATH = "appTmpPath";             // %TMP_PATH/workarea
    public static final String SESSION_TMP_PATH = "sessionTmpPath";     // %APP_TMP_PATH/session/{session_id}
    public static final String JOB_TMP_PATH = "jobTmpPath";             // %APP_TMP_PATH/job/{job_id}

    private static final String TMP_PATH_VALUE = System.getProperty("java.io.tmpdir");
    private static final String APP_TMP_PATH_VALUE = TMP_PATH_VALUE + FILE_SEPARATOR + "workarea";
    private static final String SESSION_TMP_PATH_VALUE = APP_TMP_PATH_VALUE + FILE_SEPARATOR + "session";
    private static final String JOB_TMP_PATH_VALUE = APP_TMP_PATH_VALUE + FILE_SEPARATOR + "job";

    public static String getTmpPath() {
        return TMP_PATH_VALUE;
    }

    public static String getAppTmpPath() {
        return APP_TMP_PATH_VALUE;
    }

    public static String getSessionTmpPath() {
        return SESSION_TMP_PATH_VALUE;
    }

    public static String getJobTmpPath() {
        return JOB_TMP_PATH_VALUE;
    }
}
