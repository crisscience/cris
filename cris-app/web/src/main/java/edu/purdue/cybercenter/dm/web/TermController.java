/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.dto.TermDto;
import edu.purdue.cybercenter.dm.dto.TermMapper;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.service.TermService;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author xu222
 */
@RequestMapping("/terms")
@Controller
public class TermController {

    @Autowired
    private DomainObjectService domainObjectService;
    @Autowired
    private TermService termService;

    @RequestMapping(value = "/fetchDetails/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object fetchTermDetails(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        String result = fetchTermDetails(id);
        return result;
    }

    @RequestMapping(value = "/fetchDetails/{uuid}/{version}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object fetchTermDetails(@PathVariable("uuid") String uuid, @PathVariable("version") String version, HttpServletRequest request, HttpServletResponse response) {
        String result = fetchTermDetails(uuid, version);
        return result;
    }

    @RequestMapping(value = "/fetchDetails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object fetchTermDetails(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        String uuid = request.getParameter("uuid");
        String version = request.getParameter("version");
        String result;
        if (StringUtils.isNoneBlank(id)) {
            result = fetchTermDetails(Integer.parseInt(id));
        } else {
            result = fetchTermDetails(uuid, version);
        }
        return result;
    }

    private String fetchTermDetails(Integer id) {
        edu.purdue.cybercenter.dm.domain.Term dbTerm = termService.findById(id);
        return fetchTermDetails(dbTerm);
    }

    private String fetchTermDetails(String uuid, String version) {
        edu.purdue.cybercenter.dm.domain.Term dbTerm = termService.findByUuidAndVersionNumber(UUID.fromString(uuid), version != null ? UUID.fromString(version) : null);
        return fetchTermDetails(dbTerm);
    }

    private String fetchTermDetails(edu.purdue.cybercenter.dm.domain.Term dbTerm) {
        Term term = termService.dbTermToTerm(dbTerm);
        termService.deReference(term);
        return fetchTermDetails(term);
    }

    private String fetchTermDetails(Term term) {
        String result;
        if (term != null) {
            TermDto templateDto = TermMapper.INSTANCE.toTermDto(term);
            result = Helper.deepSerialize(templateDto);
        } else {
            result = null;
        }
        return result;
    }

    @RequestMapping(value = "/fetchAllTerms", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String fetchAllTerms(HttpServletRequest request, HttpServletResponse response) {
        return fetchAllTerms(request, response, false);
    }

    @RequestMapping(value = "/fetchAllTemplates", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String fetchAllTemplates(HttpServletRequest request, HttpServletResponse response) {
        return fetchAllTerms(request, response, true);
    }

    private String fetchAllTerms(HttpServletRequest request, HttpServletResponse response, boolean isTemplate) {
        Session session = DomainObjectHelper.getHbmSession();

        Filter filter = session.enableFilter("mostRecentUpdatedTermFilter");
        filter.setParameter("isTemplate", isTemplate);
        Filter assetStatusFilter = session.enableFilter("assetStatusFilter");
        assetStatusFilter.setParameterList("statusIds", Arrays.asList(1));
        String responseBody = listTerms(request, response, isTemplate ? "template" : "term");
        session.disableFilter("assetStatusFilter");
        session.disableFilter("mostRecentUpdatedTermFilter");

        return responseBody;
    }

    private String listTerms(HttpServletRequest request, HttpServletResponse response, String type) {
        Integer[] ia = WebHelper.getDojoGridPaginationInfo(request);
        Integer firstResult = ia[0];
        Integer lastResult = ia[1];
        Map.Entry<String, String> orderBy = WebHelper.getDojoJsonRestStoreOrderBy(request.getParameterNames());
        Map<String, Object> where = WebHelper.FromJsonToFilterClass(request.getParameter("filter"));
        List<edu.purdue.cybercenter.dm.domain.Term> dbTerms = domainObjectService.findEntries(firstResult, lastResult - firstResult + 1, orderBy, where, edu.purdue.cybercenter.dm.domain.Term.class);
        Integer totalCount = domainObjectService.countEntries(where, edu.purdue.cybercenter.dm.domain.Term.class).intValue();
        WebHelper.setDojoGridPaginationInfo(firstResult, lastResult, totalCount, response);

        List<Term> terms = termService.dbTermsToTerms(dbTerms);
        List<TermDto> termDtos = new ArrayList<>();
        for (Term term : terms) {
            term.setType(type);
            TermDto termDto = TermMapper.INSTANCE.toTermDto(term);
            termDtos.add(termDto);
        }
        return Helper.deepSerialize(termDtos);
    }

}
