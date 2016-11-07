/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web.util;

import edu.purdue.cybercenter.dm.domain.CrisEntity;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import static edu.purdue.cybercenter.dm.util.DomainObjectHelper.DOJO_FILTER_DATA;
import static edu.purdue.cybercenter.dm.util.DomainObjectHelper.DOJO_FILTER_IS_COL;
import static edu.purdue.cybercenter.dm.util.DomainObjectHelper.DOJO_FILTER_OP;
import edu.purdue.cybercenter.dm.util.DomainObjectUtils;
import edu.purdue.cybercenter.dm.util.EnumDojoOperator;
import edu.purdue.cybercenter.dm.util.Helper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 *
 * @author jiaxu
 */
@Component
public class WebJsonHelper {

    @Autowired
    private DomainObjectService domainObjectService;

    /*
     * id is null: for show only: error message: no id is psecified
     * invalid id: for show only: error message: not found
     * validation failure: for create* and update*: error message: from validator
     * other failures: for all: error message: operation failed + message from the exception
     */
    public <T> ResponseEntity<String> show(Integer id, HttpServletRequest request, HttpServletResponse response, Class<T> clazz) {
        ResponseEntity<String> responseEntity;
        if (id == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            try {
                T item = domainObjectService.findById(id, clazz);
                if (item == null) {
                    responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
                } else {
                    String responseBody = DomainObjectUtils.toJson(item, request.getContextPath());
                    responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
                }
            } catch (Exception ex) {
                responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        return responseEntity;
    }

    public <T> String list(HttpServletRequest request, HttpServletResponse response, Class<T> clazz) {
        Integer[] ia = WebHelper.getDojoGridPaginationInfo(request);
        Integer firstResult = ia[0];
        Integer lastResult = ia[1];

        Entry<String, String> orderBy = WebHelper.getDojoJsonRestStoreOrderBy(request.getParameterNames());
        Map<String, Object> origWhere = WebHelper.FromJsonToFilterClass(request.getParameter("filter"));

        Map<String, Object> where;
        if (StringUtils.isNotEmpty(request.getParameter("enabled"))) {
            Boolean enabled = Boolean.parseBoolean(request.getParameter("enabled"));
            Map<String, Object> left = new HashMap<>();
            left.put(DOJO_FILTER_OP, EnumDojoOperator.typeBoolean.getName());
            left.put(DOJO_FILTER_IS_COL, true);
            left.put(DOJO_FILTER_DATA, "enabled");
            Map<String, Object> right = new HashMap<>();
            right.put(DOJO_FILTER_OP, EnumDojoOperator.typeBoolean.getName());
            right.put(DOJO_FILTER_IS_COL, false);
            right.put(DOJO_FILTER_DATA, enabled);
            List<Map<String, Object>> filterData = new ArrayList<>();
            filterData.add(left);
            filterData.add(right);

            Map<String, Object> conditionEnabled = new HashMap<>();
            conditionEnabled.put(DOJO_FILTER_OP, EnumDojoOperator.equal.name());
            conditionEnabled.put(DOJO_FILTER_DATA, filterData);

            if (origWhere != null) {
                // add to where clause
                List<Map<String, Object>> whereData = new ArrayList<>();
                whereData.add(conditionEnabled);
                whereData.add(origWhere);

                where = new HashMap<>();
                where.put(DOJO_FILTER_OP, EnumDojoOperator.and.name());
                where.put(DOJO_FILTER_DATA, whereData);
            } else {
                where = conditionEnabled;
            }
        } else {
            where = origWhere;
        }

        List<T> items = domainObjectService.findEntries(firstResult, lastResult - firstResult + 1, orderBy, where, clazz);
        Integer totalCount = domainObjectService.countEntries(where, clazz).intValue();

        WebHelper.setDojoGridPaginationInfo(firstResult, lastResult, totalCount, response);

        return DomainObjectUtils.toJsonArray(items, request.getContextPath());
    }

    public <T extends CrisEntity> ResponseEntity<String> create(String json, HttpServletRequest request, HttpServletResponse response, Class<T> clazz) {
        ResponseEntity<String> responseEntity;
        try {
            T item = DomainObjectUtils.fromJson(json, request.getContextPath(), clazz);
            Set<ConstraintViolation<T>> constraintViolations = domainObjectService.validate(item);
            if (!constraintViolations.isEmpty()) {
                String errorMsg = violationsToJson(constraintViolations);
                responseEntity = new ResponseEntity<>("{\"message\": \"" + errorMsg + "\"}", HttpStatus.BAD_REQUEST);
            } else {
                domainObjectService.persist(item, clazz);
                WebHelper.setDojoLocationHeader(domainObjectService.get$ref(item, clazz), request, response);
                String responseBody = DomainObjectUtils.toJson(item, request.getContextPath());
                responseEntity = new ResponseEntity<>(responseBody, HttpStatus.CREATED);
            }
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("hasError", Boolean.TRUE);
            error.put("message", ex.getMessage());
            error.put("status", "Unable to create the " + clazz.getSimpleName().toLowerCase());
            responseEntity = new ResponseEntity<>(Helper.serialize(error), HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    public <T extends CrisEntity> ResponseEntity<String> createArray(String json, HttpServletRequest request, HttpServletResponse response, Class<T> clazz) {
        Collection<T> items = DomainObjectUtils.fromJsonArray(json, request.getContextPath(), clazz);
        for (T item : items) {
            domainObjectService.persist(item, clazz);
        }
        String responseBody = DomainObjectUtils.toJsonArray(items, request.getContextPath());
        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    }

    public <T extends CrisEntity> ResponseEntity<String> update(String json, HttpServletRequest request, HttpServletResponse response, Class<T> clazz) {
        ResponseEntity<String> responseEntity;
        try {
            T item = DomainObjectUtils.fromJson(json, request.getContextPath(), clazz);
            Set<ConstraintViolation<T>> constraintViolations = domainObjectService.validate(item);
            if (!constraintViolations.isEmpty()) {
                String errorMsg = violationsToJson(constraintViolations);
                responseEntity =  new ResponseEntity<>("{\"message\": \"" + errorMsg + "\"}", HttpStatus.BAD_REQUEST);
            } else {
                T merged;
                merged = domainObjectService.merge(item, clazz);
                if (merged == null) {
                    responseEntity = new ResponseEntity<>("{\"message\": \"" + "Warn: Unable to save the changes" + "\"}", HttpStatus.BAD_REQUEST);
                } else {
                    String responseBody = DomainObjectUtils.toJson(merged, request.getContextPath());
                    responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
                }
            }
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("hasError", Boolean.TRUE);
            error.put("message", ex.getMessage());
            error.put("status", "Unable to update the " + clazz.getSimpleName().toLowerCase());
            responseEntity = new ResponseEntity<>(Helper.serialize(error), HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    public <T extends CrisEntity> ResponseEntity<String> updateArray(String json, HttpServletRequest request, HttpServletResponse response, Class<T> clazz) {
        Collection<T> items = DomainObjectUtils.fromJsonArray(json, request.getContextPath(), clazz);
        for (T item : items) {
            if (domainObjectService.merge(item, clazz) == null) {
                throw new RuntimeException("Unable to update: " + Helper.serialize(item));
            }
        }
        String responseBody = DomainObjectUtils.toJsonArray(items, request.getContextPath());
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    public <T extends CrisEntity> ResponseEntity<String> delete(Integer id, HttpServletRequest request, HttpServletResponse response, Class<T> clazz) {
        ResponseEntity<String> responseEntity;
        try {
            if (id == null) {
                responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                T item;
                item = domainObjectService.findById(id, clazz);
                if (item == null) {
                    responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
                } else {
                    domainObjectService.remove(item, clazz);
                    responseEntity = new ResponseEntity<>(HttpStatus.OK);
                }
            }
        } catch (Exception ex) {
            responseEntity = new ResponseEntity<>("{\"message\": \"" + ex.getMessage() + ": Unable to delete the " + clazz.getSimpleName().toLowerCase() + "\"}", HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    private <T> String violationsToJson(Set<ConstraintViolation<T>> violations) {
        String json = "";
        if (!violations.isEmpty()) {
            for (ConstraintViolation cv : violations) {
                Path propertyPath = cv.getPropertyPath();
                String msg = cv.getMessage();
                Object invalidValue = cv.getInvalidValue();
                json = propertyPath.toString() + "(" + invalidValue + "): " + msg;
            }
        }
        return json;
    }

}
