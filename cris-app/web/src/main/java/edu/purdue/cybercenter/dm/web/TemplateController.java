package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.dto.TermDto;
import edu.purdue.cybercenter.dm.dto.TermMapper;
import edu.purdue.cybercenter.dm.service.DatasetService;
import edu.purdue.cybercenter.dm.service.DocumentService;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.service.TemplateService;
import edu.purdue.cybercenter.dm.service.TermService;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.DomainObjectUtils;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import edu.purdue.cybercenter.dm.xml.vocabulary.AttachTo;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RequestMapping("/templates")
@Controller
public class TemplateController {

    @Autowired
    private DomainObjectService domainObjectService;
    @Autowired
    private TermService termService;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "templates/index";
    }

   @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, edu.purdue.cybercenter.dm.domain.Term.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        Session session = DomainObjectHelper.getHbmSession();

        session.enableFilter("mostRecentUpdatedTermFilter").setParameter("isTemplate", true);
        String result = WebJsonHelper.list(request, response, edu.purdue.cybercenter.dm.domain.Term.class);
        session.disableFilter("mostRecentUpdatedTermFilter");

        return result;
    }

    @RequestMapping(value = "/metadata/{uuid}/{version}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object getMetadataByUuidAndVersion(@PathVariable("uuid") String uuid, @PathVariable("version") String version, HttpServletRequest request, HttpServletResponse response) {
        edu.purdue.cybercenter.dm.domain.Term dbTerm = termService.findByUuidAndVersionNumber(UUID.fromString(uuid), UUID.fromString(version));
        String result = DomainObjectUtils.toJson(dbTerm, request.getContextPath());
        return result;
    }

    @RequestMapping(value = "/metadata/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object getMetadataByUuid(@PathVariable("uuid") String uuid, HttpServletRequest request, HttpServletResponse response) {
        edu.purdue.cybercenter.dm.domain.Term dbTerm = termService.findByUuidAndVersionNumber(UUID.fromString(uuid), null);
        String result = DomainObjectUtils.toJson(dbTerm, request.getContextPath());
        return result;
    }

    @RequestMapping(value = "/import", method = {RequestMethod.PUT, RequestMethod.POST}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Object importTemplate(MultipartHttpServletRequest request, HttpServletResponse response) {
        String error = null;
        edu.purdue.cybercenter.dm.domain.Term dbTemplate = null;
        List<MultipartFile> files = WebHelper.multipartFileMapToList(request.getMultiFileMap());
        if (files.size() == 1) {
            MultipartFile mpFile = files.get(0);
            if (!mpFile.isEmpty()) {
                try {
                    String xmlTemplate = new String(mpFile.getBytes());
                    dbTemplate = templateService.saveXml(xmlTemplate, mpFile.getOriginalFilename());

                    // update search engine
                    //TODO: This should just generate an event instead of doing the update itself
                    try {
                        String sEngineUrl = (String) request.getSession().getServletContext().getAttribute("searchEngineUrl");
                        notifySearchEngine(dbTemplate.getUuid().toString(), sEngineUrl);
                    } catch (Exception ex) {
                        // ignore failure
                    }
                } catch (IOException ex) {
                    // Return error message saying upload will fail because same version cannot be uploaded twice.
                    error = "Fail to import template: " + mpFile.getOriginalFilename() + ": " + ex.getMessage();
                }
            } else {
                String filename = mpFile.getOriginalFilename();
                if (filename == null || filename.isEmpty()) {
                    error = "No file name found in the request";
                } else {
                    error = "The file is empty: " + filename;
                }
            }
        } else if (files.isEmpty()) {
            error = "No file found in the request";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("too many files:");
            boolean isFirst = true;
            for (MultipartFile file : files) {

                if (isFirst) {
                    sb.append(" ");
                    isFirst = false;
                } else {
                    sb.append(", ");
                }
                sb.append(file.getOriginalFilename());
            }

            error = sb.toString();
        }

        String result;
        if (error != null && !error.isEmpty()) {
            Map<String, Object> exception = new HashMap<>();
            exception.put("hasError", Boolean.TRUE);
            exception.put("message", error);
            exception.put("template", DomainObjectUtils.toJson(dbTemplate, request.getContextPath()));
            result = Helper.serialize(exception);
        } else {
            result = DomainObjectUtils.toJson(dbTemplate, request.getContextPath());
        }

        return WebHelper.buildIframeResponse(result);
    }

    @RequestMapping(value = "/export/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportTemplate(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        edu.purdue.cybercenter.dm.domain.Term template = edu.purdue.cybercenter.dm.domain.Term.findTerm(id);

        String name = template.getKey();
        String content = template.getContent();
        String contentType = "application/xml";

        response.setHeader("Content-Disposition", "attachment; filename=" + name + (name.endsWith(".xml") ? "" : ".xml"));
        response.setContentType(contentType);
        response.setContentLength(content.length());

        try (ServletOutputStream sos = response.getOutputStream()) {
            sos.write(content.getBytes());
            sos.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Filed to export template: " + name, ex);
        }
    }

    @RequestMapping(value = "/save", method = {RequestMethod.PUT, RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object save(HttpServletRequest request, HttpServletResponse response) {
        boolean hasError = false;
        String message = null;
        edu.purdue.cybercenter.dm.domain.Term dbTemplate = null;
        try {
            String jsonTemplate = request.getParameter("template");
            dbTemplate = templateService.saveJson(jsonTemplate, null);
        } catch (Exception ex) {
            hasError = true;
            message = "Fail to save template: " + ex.getMessage();
        }

        // update search engine
        //TODO: This should just generate an event instead of doing the update itself
        try {
            String sEngineUrl = (String) request.getSession().getServletContext().getAttribute("searchEngineUrl");
            notifySearchEngine(dbTemplate.getUuid().toString(), sEngineUrl);
        } catch (Exception ex) {
            // ignore failure
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hasError", hasError);
        result.put("message", message);
        result.put("template", dbTemplate == null ? null : DomainObjectUtils.toJson(dbTemplate, request.getContextPath()));

        return Helper.serialize(result);
    }

    @RequestMapping(value = "/load/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object loadById(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        edu.purdue.cybercenter.dm.domain.Term dbTerm;
        if (id != null) {
            dbTerm = termService.findById(id);
        } else {
            throw new RuntimeException("Nothing to load: no vocabulary id or uuid is specified");
        }

        String result = load(dbTerm);
        return result;

    }

    @RequestMapping(value = "/load/{uuid}/{version}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object loadByUuidAndVersion(@PathVariable("uuid") String uuid, @PathVariable("version") String version, HttpServletRequest request, HttpServletResponse response) {
        edu.purdue.cybercenter.dm.domain.Term dbTerm = termService.findByUuidAndVersionNumber(UUID.fromString(uuid), UUID.fromString(version));
        String result = load(dbTerm);
        return result;
    }

    @RequestMapping(value = "/load", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object load(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        String uuid = request.getParameter("uuid");
        String version = request.getParameter("version");

        edu.purdue.cybercenter.dm.domain.Term dbTerm;
        if (StringUtils.isNoneBlank(id)) {
            dbTerm = termService.findById(Integer.parseInt(id));
        } else {
            dbTerm = termService.findByUuidAndVersionNumber(UUID.fromString(uuid), version != null ? UUID.fromString(version) : null);
        }

        String result = load(dbTerm);
        return result;
    }

    private String load(edu.purdue.cybercenter.dm.domain.Term dbTerm) {
        Term term = termService.dbTermToTerm(dbTerm);
        termService.deReference(term);
        TermDto termDto = TermMapper.INSTANCE.toTermDto(term);
        String result = Helper.deepSerialize(termDto);

        return result;
    }

    @RequestMapping(value = "/head/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object makeCurrent(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        edu.purdue.cybercenter.dm.domain.Term dbTerm = termService.findById(id);
        dbTerm = termService.makeTermLatest(dbTerm);
        return Helper.serialize(dbTerm);
    }

    @RequestMapping(value = "/status/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CacheEvict(value = "vocabulary", allEntries = true)
    public Object changeStatus(@PathVariable("id") Integer id, @RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        // deprecate/restore a template
        // and return the most recent template by timeCreated
        edu.purdue.cybercenter.dm.domain.Term template = domainObjectService.findById(id, edu.purdue.cybercenter.dm.domain.Term.class);

        edu.purdue.cybercenter.dm.domain.Term updatedTemplate = termService.changeStatus(template, null);
        String result = Helper.serialize(updatedTemplate);

        return result;
    }

    @RequestMapping(value = "/versions/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object getVersionsByUuid(@PathVariable("uuid") UUID uuid, HttpServletRequest request, HttpServletResponse response) {
        List<Map<String, Object>> results = getVersions(uuid);
        return Helper.serialize(results);
    }

    private List<Map<String, Object>> getVersions(UUID uuid) {
        List<edu.purdue.cybercenter.dm.domain.Term> terms;
        if (uuid != null) {
            Map.Entry<String, String> orderBy = new HashMap.SimpleEntry<>("timeUpdated", "desc");
            Map<String, Object> where = WebHelper.FromJsonToFilterClass(String.format("{\"op\":\"equal\",\"data\":[{\"op\":\"uuid\",\"data\":\"uuid\",\"isCol\":true},{\"op\":\"uuid\",\"data\":\"%s\",\"isCol\":false}]}", uuid.toString()));

            terms = domainObjectService.findEntries(null, null, orderBy, where, edu.purdue.cybercenter.dm.domain.Term.class);
        } else {
            terms = new ArrayList<>();
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (edu.purdue.cybercenter.dm.domain.Term term : terms) {
            Map<String, Object> result = new HashMap<>();
            result.put("id", term.getId());
            result.put("uuid", term.getUuid());
            result.put("versionNumber", term.getVersionNumber());
            result.put("key", term.getKey());
            result.put("name", term.getName());
            result.put("description", term.getDescription());
            result.put("statusId", term.getStatusId());
            result.put("timeCreated", term.getTimeCreated());
            result.put("timeUpdated", term.getTimeUpdated());
            results.add(result);
        }

        return results;
    }

    @RequestMapping(value = "/xml/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String getXml(@PathVariable("uuid") UUID uuid, @RequestParam(value = "version", required = false) UUID version, HttpServletRequest request, HttpServletResponse response) {
        String sDereference = request.getParameter("dereference");
        boolean deference;
        try {
            deference = Boolean.parseBoolean(sDereference);
        } catch (Exception ex) {
            deference = false;
        }
        Term template = termService.getTerm(uuid, version, deference);
        template.setName(StringEscapeUtils.escapeXml(template.getName()));
        template.setDescription(StringEscapeUtils.escapeXml(template.getDescription()));

        return termService.convertTermToXml(template);
    }

    @RequestMapping(value = "/json/names", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String listNames(HttpServletRequest request, HttpServletResponse response) {
        Session session = DomainObjectHelper.getHbmSession();

        session.enableFilter("mostRecentUpdatedTemplateFilter");
        String responseBody = Helper.deepSerialize(getTemplateList(request));
        session.disableFilter("mostRecentUpdatedTemplateFilter");

        return responseBody;
    }

    private List<Map<String, String>> getTemplateList(HttpServletRequest request) {
        Map<String, Object> queryMap = new HashMap<>();
        String sProjectId = request.getParameter("projectId");
        if (sProjectId != null && !sProjectId.isEmpty()) {
            try {
                Integer projectId = Integer.parseInt(sProjectId);
                if (projectId > 0) {
                    queryMap.put(MetaField.ProjectId, projectId);
                }
            } catch (NumberFormatException ex) {
                throw new RuntimeException(String.format("Invalid project ID: %s", sProjectId), ex);
            }
        }
        String sExperimentId = request.getParameter("experimentId");
        if (sExperimentId != null && !sExperimentId.isEmpty()) {
            try {
                Integer experimentId = Integer.parseInt(sExperimentId);
                if (experimentId > 0) {
                    queryMap.put(MetaField.ExperimentId, experimentId);
                }
            } catch (NumberFormatException ex) {
                throw new RuntimeException(String.format("Invalid experiment ID: %s", sExperimentId), ex);
            }
        }
        String sJobId = request.getParameter("jobId");
        if (sJobId != null && !sJobId.isEmpty()) {
            try {
                Integer jobId = Integer.parseInt(sJobId);
                if (jobId > 0) {
                    queryMap.put(MetaField.JobId, jobId);
                }
            } catch (NumberFormatException ex) {
                throw new RuntimeException(String.format("Invalid job ID: %s", sJobId), ex);
            }
        }

        List<Map<String, String>> ssItems = new ArrayList<>();
        //Where where = WebHelper.FromJsonToFilterClass(String.format("{\"op\":\"equal\",\"data\":[{\"op\":\"boolean\",\"data\":\"isTemplate\",\"isCol\":true},{\"op\":\"boolean\",\"data\":\"%s\",\"isCol\":false}]}", "true"));
        //List<edu.purdue.cybercenter.dm.domain.Term> items = domainObjectService.findEntries(0, Integer.MAX_VALUE, null, where, edu.purdue.cybercenter.dm.domain.Term.class);
        List<edu.purdue.cybercenter.dm.domain.Term> items = templateService.findLatest();
        for (edu.purdue.cybercenter.dm.domain.Term item : items) {
            String showAll = request.getParameter("showAll");
            if ((showAll != null && showAll.equals("true")) || datasetService.count(item.getUuid(), queryMap) > 0) {
                Map<String, String> nvp = new HashMap<>();
                nvp.put("id", item.getUuid().toString());
                nvp.put("name", item.getName());
                nvp.put("version", item.getVersionNumber().toString());
                ssItems.add(nvp);
            }
        }

        Collections.sort(ssItems, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> a, Map<String, String> b) {
                return a.get("name").compareTo(b.get("name"));
            }
        });

        return ssItems;
    }

    //TODO: use listDatasetJson() from RestController
    @Deprecated()
    @RequestMapping(value = "/json/dataset", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String listDataset(HttpServletRequest request, HttpServletResponse response) {
        String sTemplateUuid = request.getParameter("templateUuid");
        String sTemplateVersion = request.getParameter("version");
        if (sTemplateUuid == null || sTemplateUuid.isEmpty()) {
            List<Map<String, String>> tempList = getTemplateList(request);
            if (tempList != null && tempList.size() > 0) {
                sTemplateUuid = tempList.get(0).get("id");
            } else {
                WebHelper.setDojoGridPaginationInfo(0, 0, 0, response);
                return "[{}]";
            }
        }

        Map<String, Object> query = new HashMap<>();
        String sProjectId = request.getParameter("projectId");
        if (sProjectId != null && !sProjectId.isEmpty()) {
            try {
                Integer projectId = Integer.parseInt(sProjectId);
                if (projectId > 0) {
                    query.put(MetaField.ProjectId, projectId);
                }
            } catch (NumberFormatException ex) {
                throw new RuntimeException(String.format("Invalid project ID: %s", sProjectId), ex);
            }
        }
        String sExperimentId = request.getParameter("experimentId");
        if (sExperimentId != null && !sExperimentId.isEmpty()) {
            try {
                Integer experimentId = Integer.parseInt(sExperimentId);
                if (experimentId > 0) {
                    query.put(MetaField.ExperimentId, experimentId);
                }
            } catch (NumberFormatException ex) {
                throw new RuntimeException(String.format("Invalid experiment ID: %s", sExperimentId), ex);
            }
        }
        String sJobId = request.getParameter("jobId");
        if (sJobId != null && !sJobId.isEmpty()) {
            try {
                Integer jobId = Integer.parseInt(sJobId);
                if (jobId > 0) {
                    query.put(MetaField.JobId, jobId);
                }
            } catch (NumberFormatException ex) {
                throw new RuntimeException(String.format("Invalid job ID: %s", sJobId), ex);
            }
        }

        UUID templateUuid = UUID.fromString(sTemplateUuid);
        UUID templateVersion = (sTemplateVersion == null || sTemplateVersion.isEmpty()) ? null : UUID.fromString(sTemplateVersion);

        Integer[] a = WebHelper.getDojoGridPaginationInfo(request);
        int firstResult = a[0];
        int maxResults = a[1] - firstResult + 1;
        int count = (int) datasetService.count(templateUuid, query);
        if (maxResults > 0) {
            Map<String, Integer> sort = new HashMap<>();
            Map.Entry<String, String> ele = WebHelper.getDojoJsonRestStoreOrderBy(request.getParameterNames());
            if (ele == null) {
                Term template = termService.getTerm(templateUuid, templateVersion, true);
                List<Map<String, String>> headers = ((Map<String, ArrayList<Map<String, String>>>) getLayout(request, template).get(0)).get("cells");
                for (Map<String, String> header : headers) {
                    String field = header.get("field");
                    sort.put(field, 1);
                    if (!(field.equals(MetaField.ProjectId) || field.equals(MetaField.ExperimentId) || field.equals(MetaField.JobId))) {
                        break;
                    }
                }
            } else {
                sort.put(ele.getKey(), "asc".equals(ele.getValue()) ? 1 : -1);
            }
            Map<String, Object> aggregators = new HashMap<>();
            aggregators.put(DocumentService.AGGREGATOR_MATCH, query);
            aggregators.put(DocumentService.AGGREGATOR_SORT, sort);
            aggregators.put(DocumentService.AGGREGATOR_SKIP, firstResult);
            aggregators.put(DocumentService.AGGREGATOR_LIMIT, maxResults);
            List values = datasetService.find(templateUuid, aggregators);
            WebHelper.setDojoGridPaginationInfo(firstResult, firstResult + (values.size() - 1), count, response);
            return DatasetUtils.serialize(values);
        } else {
            WebHelper.setDojoGridPaginationInfo(firstResult, firstResult, 0, response);
            return "[{}]";
        }
    }

    @RequestMapping(value = "/json/layout", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String layout(HttpServletRequest request, HttpServletResponse response) {
        String sTemplateUuid = request.getParameter("templateUuid");
        String sTemplateVersion = request.getParameter("version");
        if (sTemplateUuid == null || sTemplateUuid.trim().equals("")) {
            List<Map<String, String>> tempList = getTemplateList(request);
            if (tempList != null && tempList.size() > 0) {
                sTemplateUuid = tempList.get(0).get("id");
            } else {
                return "cells: [{}]";
            }
        }
        UUID templateUuid = UUID.fromString(sTemplateUuid);
        UUID templateVersion = (sTemplateVersion == null || sTemplateVersion.isEmpty()) ? null : UUID.fromString(sTemplateVersion);
        Term template = termService.getTerm(templateUuid, templateVersion, true);
        return Helper.deepSerialize(getLayout(request, template));
    }

    private List<Object> getLayout(HttpServletRequest request, Term template) {
        List<Object> finalContent = new ArrayList<>();
        Map<String, List<Map<String, String>>> content = new HashMap<>();
        if (template != null) {
            List<Map<String, String>> cells = new ArrayList<>();
            for (String str : new String[]{"project", "experiment", "job"}) {
                String id = request.getParameter(str + "Id");
                if (id != null && id.equals("0")) {
                    Map<String, String> aMap = new HashMap<>();
                    aMap.put("name", str);
                    aMap.put("field", "_" + str + "_id");
                    aMap.put("width", "auto");
                    cells.add(aMap);
                }
            }
            List<AttachTo> attachToList = template.getAttachTo();
            for (AttachTo attachTo : attachToList) {
                String fieldName = attachTo.getUseAlias();
                if (fieldName == null) {
                    fieldName = attachTo.getNameField();
                }
                Map<String, String> aMap = new HashMap<>();
                aMap.put("name", fieldName);
                aMap.put("field", fieldName);
                aMap.put("width", "auto");
                cells.add(aMap);
            }
            List<Term> termList = template.getTerm();
            for (Term term : termList) {
                String fieldName = term.getAlias();
                if (fieldName == null) {
                    fieldName = term.getName();
                }
                Map<String, String> aMap = new HashMap<>();
                aMap.put("name", fieldName);
                aMap.put("field", fieldName);
                aMap.put("width", "auto");
                cells.add(aMap);
            }
            content.put("cells", cells);
        }
        finalContent.add(content);
        return finalContent;
    }

    private void notifySearchEngine(String templateUuid, String sEngineUrl) {
        // create index for elasticsearch
        String collectionName = DatasetUtils.makeCollectionName(templateUuid, false);

        Map<String, Object> mongodb = new HashMap<>();
        mongodb.put("db", "cris");
        mongodb.put("collection", collectionName);

        Map<String, Object> index = new HashMap<>();
        index.put("name", "cris");
        index.put("type", collectionName);

        Map<String, Object> mapRequest = new HashMap<>();
        mapRequest.put("type", "mongodb");
        mapRequest.put("mongodb", mongodb);
        mapRequest.put("index", index);

        RestTemplate restTemplate = new RestTemplate();
        String searchEngineUrl = sEngineUrl;
        restTemplate.put(searchEngineUrl + "_river/" + collectionName + "/_meta", Helper.serialize(mapRequest));
    }

}
