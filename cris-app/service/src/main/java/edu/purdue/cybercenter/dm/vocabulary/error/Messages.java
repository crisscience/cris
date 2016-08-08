package edu.purdue.cybercenter.dm.vocabulary.error;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

public class Messages {

    private static final Properties messages;

    static {
        messages = new Properties();
        try {
            InputStream resource = Messages.class.getResourceAsStream("Messages.properties");
            messages.load(resource);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to read message property file.", ex);
        }
    }

    public static String getMessage(String errorCode, Object[] args) {
        String errorMessage;
        if (messages.get(errorCode) != null) {
            errorMessage = MessageFormat.format((String) messages.get(errorCode), args);
        } else {
            errorMessage = "Message not Found for the code: [" + errorCode + "]";
        }
        return errorMessage;
    }

}
