/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

/**
 *
 * @author xu222
 */
@ControllerAdvice
public class GlobalControllerExceptionHandler {

    private static final String ViewGeneralException = "generalException";

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class.getName());

    /*
     * There are three return types
     * 1. error page: a jsp page view
     * 2. regular ajax respone: in json format
     * 3. iframe response: it is a wrapper around a regulat ajax response
     */
    @ExceptionHandler(Throwable.class)
    public Object handleError(HttpServletRequest request, HttpServletResponse response, Throwable exception) throws Throwable {
        logger.error("Request: {} raised {}", new Object[]{request.getRequestURI(), exception});
        logger.error(exception.getMessage(), exception);

        // Rethrow annotated exceptions or they will be processed here instead.
        if (AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class) != null) {
            throw exception;
        }

        //TODO: figure out the status code: should be 400s and 500s
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String url = request.getRequestURL().toString();

        String view;
        if (exception instanceof CrisControllerException) {
            // wrong url or someone is tempering the request
            httpStatus = ((CrisControllerException) exception).getHttpStatus();
            view = ViewGeneralException;
        } else if (exception instanceof NoSuchRequestHandlingMethodException) {
            // wrong url or someone is tempering the request

            view = ViewGeneralException;
        } else if (exception instanceof AccessDeniedException) {
            // lack of permission or someone is tempering the request

            view = ViewGeneralException;
        } else if (exception instanceof TypeMismatchException) {
            // most often happens with controller parameters

            view = ViewGeneralException;
        } else if (exception instanceof MissingServletRequestParameterException) {
            // missing expected parameters

            view = ViewGeneralException;
        } else if (exception instanceof DataAccessException) {
            // data access problem

            view = ViewGeneralException;
        } else if (exception instanceof RuntimeException) {
            // General unchecked exception

            view = ViewGeneralException;
        } else if (exception instanceof Exception) {
            // General checked exception

            view = ViewGeneralException;
        } else if (exception instanceof Error) {
            // fatal error

            view = ViewGeneralException;
        } else {
            // everything else: should never happen
            view = ViewGeneralException;
        }

        String header = request.getHeader("Accept");
        String isIframe = request.getParameter("isIframe");
        boolean bIsIframe = isIframe != null && isIframe.equalsIgnoreCase("true");
        Date timestamp = new Date();

        String message;
        if (exception.getMessage() == null) {
            message = exception.getClass().getSimpleName();
        } else {
            message = exception.getMessage();
        }

        Object result;
        if (header != null && header.contains("html") && !bIsIframe) {
            ModelAndView mav = new ModelAndView();
            mav.addObject("hasError", true);
            mav.addObject("message", message);
            mav.addObject("exception", exception);
            mav.addObject("url", url);
            mav.addObject("timestamp", timestamp);

            response.setStatus(httpStatus.value());
            mav.setViewName(view);
            result = mav;
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("hasError", true);
            map.put("message", message);
            map.put("exception", exception);
            map.put("url", url);
            map.put("timestamp", timestamp);
            String body = Helper.serialize(map);

            String responseBody;
            if (bIsIframe) {
                responseBody = WebHelper.buildIframeResponse(body);
            } else {
                responseBody = body;
            }
            ResponseEntity<String> re = new ResponseEntity<>(responseBody, httpStatus);
            result = re;
        }

        return result;
    }
}
