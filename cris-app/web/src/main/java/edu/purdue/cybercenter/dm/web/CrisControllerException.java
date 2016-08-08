/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web;

import org.springframework.http.HttpStatus;

/**
 *
 * @author xu222
 */
public class CrisControllerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private HttpStatus httpStatus;

    public CrisControllerException(HttpStatus httpStatus) {
        super();
        this.httpStatus = httpStatus;
    }

    public CrisControllerException(String msg, HttpStatus httpStatus) {
        super(msg);
        this.httpStatus = httpStatus;
    }

    public CrisControllerException(String msg, Throwable cause, HttpStatus httpStatus) {
        super(msg, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.httpStatus = statusCode;
    }

}
