/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.xml.vocabulary.AttachTo;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author xu222
 */
public final class TermName {

    /*
     * whole term
     * 1b122c30-0eed-11e2-892e-0800200c9a66({_template_version: 1})
     *
     * a field
     * 1b122c30-0eed-11e2-892e-0800200c9a66.alias1({_template_version: 1})
     *
     * a nested field
     * 1b122c30-0eed-11e2-892e-0800200c9a66.alias1.alias2({_template_version: 1})
     *
     * ({_template_version: 1}) is optional. In case of missing, it means the latest version
     */

    private UUID uuid;
    private UUID version;

    private List<String> path;

    private String queryString;

    private Boolean isList;

    public TermName() {
        clear();
    }

    public TermName(String name) {
        parse(name);
    }

    public TermName(Term term) {
        parse(term);
    }

    public void parse(String name) {
        if (name == null || name.isEmpty()) {
            clear();
            return;
        }

        // precess query string
        String queryString;
        if (name.indexOf("(") != -1 && name.endsWith(")")) {
            queryString = name.substring(name.indexOf("(") + 1, name.length() - 1);
            name = name.substring(0, name.indexOf("("));
            if (!queryString.isEmpty() && !queryString.equals("{}")) {
                Map<String, Object> query = Helper.deserialize(queryString, Map.class);
                String templateVersion = (String) query.get(MetaField.TemplateVersion);
                if (templateVersion != null) {
                    version = UUID.fromString(templateVersion);
                } else {
                    version = null;
                }
            } else {
                queryString = null;
                version = null;
            }
        } else {
            queryString = null;
            version = null;
        }
        this.queryString = queryString;

        if (name.endsWith("[]")) {
            this.isList = true;
            name = name.substring(0, name.lastIndexOf("["));
        } else if (name.endsWith("*")) {
            this.isList = null;
            name = name.substring(0, name.lastIndexOf("*"));
        } else {
            this.isList = false;
        }

        String[] fields = name.split("\\.");

        if (fields.length >= 1 && fields[0].matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")) {
            setUuid(UUID.fromString(fields[0]));
        }

        if (fields.length != 0) {
            path = new ArrayList<String>(Arrays.asList(fields));
        } else {
            path = new ArrayList<String>();
        }
    }

    public void parse(Term term) {
        uuid = UUID.fromString(term.getUuid());
        version = UUID.fromString(term.getVersion());

        path = new ArrayList<String>(Arrays.asList(new String[]{uuid.toString()}));

        Map<String, Object> query = new HashMap<String, Object>();
        if (version != null) {
            query.put(MetaField.TemplateVersion, version.toString());
            queryString = Helper.serialize(query);
        } else {
            queryString = null;
        }

        isList = term.isList() == null ? false : term.isList();
    }

    public void push(String alias) {
        this.path.add(alias);
    }

    /*
     * The first segment of the path is the term UUID
     * and the rest are aliases
     */
    public void push(Term node) {
        if (path.isEmpty()) {
            this.path.add(node.getUuid());
        } else {
            String alias = node.getAlias() == null ? node.getName() : node.getAlias();
            this.path.add(alias);
        }
    }

    public void push(AttachTo node) {
        String alias = node.getUseAlias();
        this.path.add(alias);
    }

    public String pop() {
        String segment;
        if (path.isEmpty()) {
            segment = null;
        } else {
            String alias = path.get(path.size() - 1);
            path.remove(path.size() - 1);
            segment = alias;
        }
        return segment;
    }

    public void clear() {
        uuid = null;
        version = null;
        path = null;
        queryString = null;
        isList = null;
    }

    public boolean isEmpty() {
        boolean isEmpty;
        if (uuid == null && version == null && path == null && queryString == null && isList == null) {
            isEmpty = true;
        } else {
            isEmpty = false;
        }
        return isEmpty;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();

        if (this.getPathString() != null) {
            sb.append(this.getPathString());
        }

        if (isList != null && isList) {
            sb.append("[]");
        }

        if (queryString != null && !queryString.isEmpty()) {
            sb.append("(");
            sb.append(queryString);
            sb.append(")");
        } else if (version != null) {
            sb.append("(");
            sb.append("\"_template_version:\"").append(version.toString()).append("\"");
            sb.append(")");
        }

        return sb.toString();
    }

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the version
     */
    public UUID getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(UUID version) {
        this.version = version;
    }

    /**
     * @return the path in string format
     */
    public String getPathString() {
        String pathString;
        if (path == null || path.isEmpty()) {
            pathString = null;
        } else {
            pathString = StringUtils.join(path, ".");
        }
        return pathString;
    }

    /**
     * @return the alias: PathString without the leading UUID
     */
    public String getAlias() {
        String alias;
        if (path == null || path.isEmpty() || (uuid != null && path.size() <= 1)) {
            alias = null;
        } else {
            if (uuid != null) {
                alias = StringUtils.join(path.subList(1, path.size()), ".");
            } else {
                alias = StringUtils.join(path, ".");
            }
        }
        return alias;
    }

    /**
     * @return the queryString
     */
    public String getQueryString() {
        /* don't have enough info to get the quotes right.
        if (query == null || query.isEmpty()) {
            return null;
        } else {
            return Helper.serialize(this.query);
        }
        */

        return this.queryString;
    }

    /**
     * @param queryString the query to set
     */
    public void setQueryString(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            this.queryString = null;
        } else {
            this.queryString = queryString;
        }
    }

    /**
     * @return the isArray
     */
    public Boolean getIsList() {
        return isList;
    }

    /**
     * @param isList the isArray to set
     */
    public void setIsList(Boolean isList) {
        this.isList = isList;
    }
}
