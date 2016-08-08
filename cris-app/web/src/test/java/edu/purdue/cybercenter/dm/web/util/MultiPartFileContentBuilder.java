package edu.purdue.cybercenter.dm.web.util;

import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Srivathsava
 * Date: 10/4/13
 * Time: 10:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class MultiPartFileContentBuilder {

    String multiPartBoundary;
    String httpRequestContent;

    public MultiPartFileContentBuilder(String multiPartBoundary) {
        this.multiPartBoundary = multiPartBoundary;
        httpRequestContent = "";
    }

    public MultiPartFileContentBuilder addFile(File file, String fileName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        StringWriter writer = new StringWriter();
        IOUtils.copy(fileInputStream, writer);

        httpRequestContent = httpRequestContent.concat(multiPartBoundary)
                          .concat("\n")
                          .concat("Content-Disposition: form-data; ")
                          .concat("name=\"detailFileName\"\n\n")
                          .concat(fileName)
                          .concat("\n")
                          .concat(multiPartBoundary)
                          .concat("\n")
                          .concat("Content-Disposition: form-data; ")
                          .concat("name=\"file\"; ")
                          .concat("filename=\"" + fileName + "\"\n")
                          .concat("Content-Type: text/xml\n\n")
                          .concat(writer.toString())
                          .concat("\n");
        return this;
    }

    public MultiPartFileContentBuilder addField(String fieldName, String value) throws IOException {
        httpRequestContent = httpRequestContent.concat(multiPartBoundary)
                .concat("\n")
                .concat("Content-Disposition: form-data; ")
                .concat("name=\"" + fieldName + "\"\n\n")
                .concat(value)
                .concat("\n");
        return this;
    }

    public String build(){
        httpRequestContent = httpRequestContent.concat(multiPartBoundary)
                          .concat("--\n");
        return httpRequestContent;
    }
}
