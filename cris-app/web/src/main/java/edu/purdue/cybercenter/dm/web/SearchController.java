/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Term;
import edu.purdue.cybercenter.dm.service.SecurityService;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.EnumDatasetState;
import edu.purdue.cybercenter.dm.util.Helper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import static org.elasticsearch.index.query.FilterBuilders.andFilter;
import static org.elasticsearch.index.query.FilterBuilders.existsFilter;
import static org.elasticsearch.index.query.FilterBuilders.missingFilter;
import static org.elasticsearch.index.query.FilterBuilders.notFilter;
import static org.elasticsearch.index.query.FilterBuilders.orFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.FilterBuilders.termsFilter;
import static org.elasticsearch.index.query.FilterBuilders.typeFilter;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.constantScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.fuzzyQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.prefixQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author xu222
 */
@RequestMapping("/search")
@Controller
public class SearchController {

    @Autowired
    private SecurityService securityService;

    private static Client elasticsearchClient = null;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model) {
        return "search/index";
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String search(HttpServletRequest request, HttpServletResponse response) {
        if (elasticsearchClient == null) {
            //TODO: use autowired
            // initialize the search engine the first time
            String url;
            int port;
            String searchEngineUrl = (String) request.getSession().getServletContext().getAttribute("searchEngineUrl");
            int idx = searchEngineUrl.lastIndexOf(":");
            if (idx != -1) {
                url = searchEngineUrl.substring(0, idx);
                String sPort = searchEngineUrl.substring(idx + 1);
                if (sPort.endsWith("/")) {
                    sPort = sPort.substring(0, sPort.length() - 1);
                }
                port = Integer.parseInt(sPort);
            } else {
                url = searchEngineUrl;
            }
            idx = url.indexOf("//");
            if (idx != -1) {
                url = url.substring(idx + 2);
            }
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            port = 9300;
            elasticsearchClient = new TransportClient().addTransportAddress(new InetSocketTransportAddress(url, port));
        }

        String jsonQueryContext = request.getParameter("queryContext");

        Map<String, Object> queryContext = Helper.deserialize(jsonQueryContext, Map.class);
        Integer currentPage = (Integer) queryContext.get("currentPage");
        Integer resultPerPage = (Integer) queryContext.get("resultPerPage");
        Integer tenantId = (Integer) request.getSession().getAttribute("tenantId");

        QueryBuilder queryBuilder = buildSearchQuery(queryContext);
        FilterBuilder filterBuilder = buildSearchFilter(tenantId);

        SearchResponse searchResponse = elasticsearchClient.prepareSearch("cris")
                .setQuery(queryBuilder)
                .setPostFilter(filterBuilder)
                .setFrom(currentPage * resultPerPage)
                .setSize(resultPerPage)
                .setExplain(true)
                .execute()
                .actionGet();
        String result = searchResponse.toString();

        return result;
    }

    private QueryBuilder buildSearchQuery(Map<String, Object> queryContext) {
        List<Map<String, Object>> queryData = (List<Map<String, Object>>) queryContext.get("queryData");

        BoolQueryBuilder queryBuilder = boolQuery();

        for (Map<String, Object> map : queryData) {
            String andOr = (String) map.get("andOr");
            String templateId = (String) map.get("templateId");
            String termAlias = (String) map.get("termAlias");
            String op = (String) map.get("op");
            String field = ((String) map.get("field"));
            String input = map.get("input") != null ? ((String) map.get("input")).toLowerCase() : map.get("hiddenInput") != null ? ((String) map.get("hiddenInput")).toLowerCase() : null;

            if (StringUtils.isEmpty(field) || "template".equals(field)) {
                if (StringUtils.isEmpty(termAlias)) {
                    // default to all terms
                    termAlias = "_all";
                }
            } else {
                // special cases: search all templates by a user or date
                termAlias = field;
            }

            if (StringUtils.isBlank(op)) {
                // default to "search" op
                op = "search";
            }

            QueryBuilder qb;
            // common ops: operations common to all types
            if (op.equals("equals")) {
                qb = constantScoreQuery(termFilter(termAlias, input));
            } else if (op.equals("not equal")) {
                qb = constantScoreQuery(notFilter(termFilter(termAlias, input)));
            } else if (op.equals("is empty")) {
                qb = constantScoreQuery(missingFilter(termAlias));
            } else if (op.equals("is not empty")) {
                qb = constantScoreQuery(existsFilter(termAlias));
                // numeric ops: ncluding number, date/time
            } else if (op.equals("range") && input != null) {
                String range = input.replaceAll(" ", "");
                boolean includeLower = range.charAt(0) == '[';
                boolean includeUpper = range.charAt(range.length() - 1) == ']';
                range = range.substring(1, range.length() - 1);
                String[] ends = range.split(",");
                qb = rangeQuery(termAlias).from(ends[0]).to(ends[1]).includeLower(includeLower).includeUpper(includeUpper);
            } else if (op.equals("less than")) {
                qb = rangeQuery(termAlias).lt(input);
            } else if (op.equals("greater than")) {
                qb = rangeQuery(termAlias).gt(input);
            } else if (op.equals("less than or equal")) {
                qb = rangeQuery(termAlias).lte(input);
            } else if (op.equals("greater than or equal")) {
                qb = rangeQuery(termAlias).gte(input);
                // string ops
            } else if (op.equals("search")) {
                qb = fuzzyQuery(termAlias, input);
            } else if (op.equals("contains")) {
                qb = matchPhraseQuery(termAlias, input);
            } else if (op.equals("not contain")) {
                qb = boolQuery().mustNot(matchPhraseQuery(termAlias, input));
            } else if (op.equals("starts with")) {
                qb = prefixQuery(termAlias, input);
            } else if (op.equals("not start with")) {
                qb = boolQuery().mustNot(prefixQuery(termAlias, input));
            } else if (op.equals("ends with")) {
                qb = wildcardQuery(termAlias, "*" + input);
            } else if (op.equals("not end with")) {
                qb = boolQuery().mustNot(wildcardQuery(termAlias, "*" + input));
            } else {
                // shouldn't reach here, just in case
                qb = fuzzyQuery(termAlias, input);
            }

            FilterBuilder fb = null;
            if (templateId != null && !templateId.isEmpty() && "template".equals(field)) {
                Term template = Term.findTerm(Integer.parseInt(templateId));
                if (template != null) {
                    String documentType = DatasetUtils.makeCollectionName(template.getUuid(), false);
                    fb = typeFilter(documentType);
                }
            }

            QueryBuilder termQueryBuilder;
            if (fb != null) {
                termQueryBuilder = filteredQuery(qb, fb);
            } else {
                termQueryBuilder = qb;
            }
            if (andOr == null || andOr.isEmpty() || andOr.equals("must")) {
                queryBuilder.must(termQueryBuilder);
            } else if (andOr.equals("should")) {
                queryBuilder.should(termQueryBuilder);
            } else if (andOr.equals("must_not")) {
                queryBuilder.mustNot(termQueryBuilder);
            }
        }

        return queryBuilder;
    }

    private FilterBuilder buildSearchFilter(Integer tenantId) {
        List<Integer> projectIds = securityService.getPermittedProjectIds();
        List<Integer> experimentIds = securityService.getPermittedExperimentIds();

        FilterBuilder tenantFilterBuilder = termFilter(MetaField.TenantId, tenantId);
        FilterBuilder stateFilterBuilder = termsFilter(MetaField.State, Arrays.asList(EnumDatasetState.Operational.getIndex(), EnumDatasetState.Archived.getIndex()));

        FilterBuilder projectFilterBuilder = termsFilter(MetaField.ProjectId, projectIds);
        FilterBuilder experimentFilterBuilder = termsFilter(MetaField.ExperimentId, experimentIds);
        FilterBuilder orFilterBuilder = orFilter(projectFilterBuilder, experimentFilterBuilder);

        FilterBuilder filterBuilder = andFilter(tenantFilterBuilder, stateFilterBuilder, orFilterBuilder);

        return filterBuilder;
    }
}
