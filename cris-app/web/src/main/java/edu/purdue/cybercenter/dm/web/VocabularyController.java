package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.dto.TermMapper;
import edu.purdue.cybercenter.dm.dto.VocabularyDto;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.service.VocabularyService;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.DomainObjectUtils;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import edu.purdue.cybercenter.dm.xml.vocabulary.Vocabulary;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RequestMapping("/vocabularys")
@Controller
public class VocabularyController {

    @Autowired
    private DomainObjectService domainObjectService;
    @Autowired
    private VocabularyService vocabularyService;
    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value = {"/index", "/index/**"}, method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "vocabularys/index";
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        Session session = DomainObjectHelper.getHbmSession();

        session.enableFilter("mostRecentUpdatedVocabularyFilter");
        String result = WebJsonHelper.list(request, response, edu.purdue.cybercenter.dm.domain.Vocabulary.class);
        session.disableFilter("mostRecentUpdatedVocabularyFilter");

        return result;
    }

    @RequestMapping(value = "/import", method = {RequestMethod.PUT, RequestMethod.POST}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Object importVocabulary(MultipartHttpServletRequest request, HttpServletResponse response) {
        String error = null;
        edu.purdue.cybercenter.dm.domain.Vocabulary vocabulary = null;

        List<MultipartFile> files = WebHelper.multipartFileMapToList(request.getMultiFileMap());
        if (files.size() == 1) {
            MultipartFile mpFile = files.get(0);
            if (!mpFile.isEmpty()) {
                try {
                    String xmlVocabulary = new String(mpFile.getBytes());
                    vocabulary = vocabularyService.saveXml(xmlVocabulary, mpFile.getOriginalFilename());
                    if (vocabulary == null) {
                        error = "All terms in the Vocabulary are already in the system. Vocabulary is not imported.";
                    }
                } catch (IOException ex) {
                    error = "Failed to import the Vocabulary: " + ex.getMessage();
                }
            } else {
                String filename = mpFile.getOriginalFilename();
                if (StringUtils.isEmpty(filename)) {
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

        String message;
        if (StringUtils.isEmpty(error)) {
            message = DomainObjectUtils.toJson(vocabulary, request.getContextPath());
        } else {
            message = String.format("{\"hasError\":true,\"message\":\"%s\"}", error);
        }

        return WebHelper.buildIframeResponse(message);
    }

    @RequestMapping(value = "/export/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportVocabulary(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary = edu.purdue.cybercenter.dm.domain.Vocabulary.findVocabulary(id);

        String filename = dbVocabulary.getKey();
        String content = vocabularyService.export(dbVocabulary);
        String contentType = "application/xml";

        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        response.setContentType(contentType);
        response.setContentLength(content.length());

        try (ServletOutputStream sos = response.getOutputStream()) {
            sos.write(content.getBytes());
            sos.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @RequestMapping(value = "/save", method = {RequestMethod.PUT, RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object save(HttpServletRequest request, HttpServletResponse response) {
        String json = request.getParameter("vocabulary");
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary = vocabularyService.saveJson(json, null);

        if (dbVocabulary == null) {
            throw new RuntimeException("All terms in the Vocabulary are already in the system. Vocabulary is not imported.");
        }

        String result = DomainObjectUtils.toJson(dbVocabulary, request.getContextPath());

        return result;
    }

    @RequestMapping(value = "/load/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object loadById(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary;
        if (id != null) {
            dbVocabulary = vocabularyService.findById(id);
        } else {
            throw new RuntimeException("Nothing to load: no vocabulary of id is specified");
        }

        String result = load(dbVocabulary);

        return result;
    }

    @RequestMapping(value = "/load/{uuid}/{version}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object loadByUuid(@PathVariable("uuid") String uuid, @PathVariable("version") String version, HttpServletRequest request, HttpServletResponse response) {
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary = vocabularyService.findByUuidAndVersion(UUID.fromString(uuid), UUID.fromString(version));
        if (dbVocabulary == null) {
            throw new RuntimeException("no vocabulary of the uuid and version found: " + uuid + ", " + version);
        }

        String result = load(dbVocabulary);

        return result;
    }

    private String load(edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary) {
        Vocabulary vocabulary = vocabularyService.dbVocabularyToVocabulary(dbVocabulary);
        String result;
        if (vocabulary != null) {
            VocabularyDto vocabularyDto = TermMapper.INSTANCE.toVocabularyDto(vocabulary);
            result = Helper.deepSerialize(vocabularyDto);
        } else {
            result = null;
        }
        return result;
    }

    @RequestMapping(value = "/head/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object makeCurrent(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary = vocabularyService.findById(id);
        dbVocabulary = vocabularyService.makeVocabularyLatest(dbVocabulary);
        return Helper.serialize(dbVocabulary);
    }

    @RequestMapping(value = "/status/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CacheEvict(value = "vocabulary", allEntries = true)
    public Object changeStatus(@PathVariable("id") Integer id, @RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        edu.purdue.cybercenter.dm.domain.Vocabulary vocabulary = domainObjectService.findById(id, edu.purdue.cybercenter.dm.domain.Vocabulary.class);

        edu.purdue.cybercenter.dm.domain.Vocabulary updatedVocabulary = vocabularyService.changeStatus(vocabulary, null);
        String result = Helper.serialize(updatedVocabulary);

        return result;
    }

    @RequestMapping(value = "/versions/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object getVersions(@PathVariable("uuid") UUID uuid, HttpServletRequest request, HttpServletResponse response) {
        List<Map<String, Object>> results = getVersionsByUuid(uuid);
        return Helper.deepSerialize(results);
    }

    public List<Map<String, Object>> getVersionsByUuid(UUID uuid) {
        List<edu.purdue.cybercenter.dm.domain.Vocabulary> dbVocabularies;
        if (uuid != null) {
            Map.Entry<String, String> orderBy = new HashMap.SimpleEntry<>("timeUpdated", "desc");
            Map<String, Object> where = WebHelper.FromJsonToFilterClass(String.format("{\"op\":\"equal\",\"data\":[{\"op\":\"uuid\",\"data\":\"uuid\",\"isCol\":true},{\"op\":\"uuid\",\"data\":\"%s\",\"isCol\":false}]}", uuid.toString()));
            dbVocabularies = domainObjectService.findEntries(null, null, orderBy, where, edu.purdue.cybercenter.dm.domain.Vocabulary.class);
        } else {
            dbVocabularies = new ArrayList<>();
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary : dbVocabularies) {
            Vocabulary v = vocabularyService.dbVocabularyToVocabulary(dbVocabulary);
            Map<String, Object> result = new HashMap<>();
            result.put("id", dbVocabulary.getId());
            result.put("uuid", dbVocabulary.getUuid());
            result.put("versionNumber", dbVocabulary.getVersionNumber());
            result.put("key", dbVocabulary.getKey());
            result.put("name", dbVocabulary.getName());
            result.put("domain", dbVocabulary.getDomain());
            result.put("description", dbVocabulary.getDescription());
            result.put("definition", v);
            result.put("statusId", dbVocabulary.getStatusId());
            result.put("timeCreated", dbVocabulary.getTimeCreated());
            result.put("timeUpdated", dbVocabulary.getTimeUpdated());
            results.add(result);
        }

        return results;
    }

    @RequestMapping(value = "/partials/{partial}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String partial(@PathVariable String partial, HttpServletRequest request, HttpServletResponse response) {
        return "vocabularys/partials/" + partial;
    }

}
