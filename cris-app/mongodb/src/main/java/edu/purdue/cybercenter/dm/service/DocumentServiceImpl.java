package edu.purdue.cybercenter.dm.service;

import com.mongodb.AggregationOptions;
import com.mongodb.AggregationOptions.Builder;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.QueryOperators;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.EnumDatasetState;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final String FIELD_ORIGINAL_ID = "original_id";
    private static final String FIELD_REV_TYPE = "revtype";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Map<String, Object> findById(UUID termUuid, ObjectId id) {
        List<EnumDatasetState> states = new ArrayList<>();
        states.add(EnumDatasetState.Operational);
        return findById(termUuid, id, states);
    }

    @Override
    public List<Map<String, Object>> find(UUID termUuid, UUID termVersion, Map<String, Object> aggregators) {
        List<EnumDatasetState> states = new ArrayList<>();
        states.add(EnumDatasetState.Operational);
        return find(termUuid, termVersion, aggregators, states);
    }

    @Override
    public List<Map<String, Object>> find(UUID termUuid, UUID termVersion, Map<String, Object> aggregators, File outputFile) {
        List<EnumDatasetState> states = new ArrayList<>();
        states.add(EnumDatasetState.Operational);
        return find(termUuid, termVersion, aggregators, states, outputFile);
    }

    @Override
    public long count(UUID termUuid, UUID termVersion, Map<String, Object> query) {
        List<EnumDatasetState> states = new ArrayList<>();
        states.add(EnumDatasetState.Operational);
        return count(termUuid, termVersion, query, states);
    }

    @Override
    public Map<String, Object> save(UUID termUuid, UUID termVersion, Map<String, Object> value) {
        return save(termUuid, termVersion, value, EnumDatasetState.Operational);
    }

    @Override
    public void delete(UUID termUuid, UUID termVersion, Map<String, Object> query) {
        List<EnumDatasetState> states = new ArrayList<>();
        states.add(EnumDatasetState.Operational);
        delete(termUuid, termVersion, query, states);
    }

    @Override
    public Map<String, Object> findById(UUID termUuid, ObjectId id, List<EnumDatasetState> states) {
        String collectionName = DatasetUtils.makeCollectionName(termUuid, false);
        DBObject dbObject = mongoTemplate.findById(id, DBObject.class, collectionName);
        Map<String, Object> map;
        if (dbObject == null) {
            map = null;
        } else {
            map = dbObject.toMap();
        }
        return map;
    }

    private QueryBuilder buildQuery(Map<String, Object> query, List<EnumDatasetState> states, Integer tenantId, UUID termVersion) {
        QueryBuilder qb = new QueryBuilder();
        Integer currentJobId = null;
        if (query != null && !query.isEmpty()) {
            for (String key : query.keySet()) {
                switch (key) {
                    case MetaField.FieldsToReturn:
                    case MetaField.DISTINCT:
                        break;
                    case MetaField.Current + MetaField.JobId:
                        currentJobId = (Integer) query.get(key);
                        break;
                    default:
                        qb.put(key).is(query.get(key));
                        break;
                }
            }
        }

        List<Integer> stateQuery = DatasetUtils.convertEnumStatesToIntegerStates(states);
        if (currentJobId != null) {
            QueryBuilder qbState = new QueryBuilder();
            qbState.put(MetaField.State).in(stateQuery);
            QueryBuilder qbCurrentJob = new QueryBuilder();
            qbCurrentJob.put(MetaField.JobId).is(currentJobId);

            Object object = qb.get().get(QueryOperators.OR);
            if (object == null) {
                qb.or(qbState.get(), qbCurrentJob.get());
            } else {
                QueryBuilder qbOr = new QueryBuilder();
                qbOr.or(qbState.get(), qbCurrentJob.get());
                qb.and(qbOr.get(), qbOr.get());
            }
        } else {
            qb.put(MetaField.State).in(stateQuery);
        }

        qb.put(MetaField.TenantId).is(tenantId);

        if (termVersion != null) {
            qb.put(MetaField.TemplateVersion).is(termVersion);
        }

        return qb;
    }

    @Override
    public List<Map<String, Object>> find(UUID termUuid, UUID termVersion, Map<String, Object> aggregators, List<EnumDatasetState> states) {
        return this.find(termUuid, termVersion, aggregators, states, null);
    }

    @Override
    public List<Map<String, Object>> find(UUID termUuid, UUID termVersion, Map<String, Object> aggregators, List<EnumDatasetState> states, File outputFile) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (termUuid == null) {
            return result;
        }

        String collectionName = DatasetUtils.makeCollectionName(termUuid, false);
        if (!mongoTemplate.collectionExists(collectionName)) {
            return result;
        }

        Integer tenantId = edu.purdue.cybercenter.dm.threadlocal.TenantId.get();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        if (tenantId == null || userId == null) {
            return result;
        }

        if (aggregators == null) {
            aggregators = new HashMap<>();
        }

        Map<String, Object> match = (Map<String, Object>) aggregators.get(DocumentService.AGGREGATOR_MATCH);
        Map<String, Boolean> distinct = (Map<String, Boolean>) aggregators.get(DocumentService.AGGREGATOR_DISTINCT);
        Map<String, Object> group = (Map<String, Object>) aggregators.get(DocumentService.AGGREGATOR_GROUP);
        Map<String, Integer> sort = (Map<String, Integer>) aggregators.get(DocumentService.AGGREGATOR_SORT);
        Integer skip = (Integer) aggregators.get(DocumentService.AGGREGATOR_SKIP);
        Integer limit = (Integer) aggregators.get(DocumentService.AGGREGATOR_LIMIT);
        Map<String, Boolean> project = (Map<String, Boolean>) aggregators.get(DocumentService.AGGREGATOR_PROJECT);

        List<DBObject> pipeline = new ArrayList<>();

        // match
        QueryBuilder qb = buildQuery(match, states, tenantId, termVersion);
        pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_MATCH, qb.get()));

        // distinct
        if (distinct != null) {
            if (!distinct.containsKey(MetaField.Id)) {
                // if there's no _id, then there may be duplicates
                Map<String, Object> groupBy = new HashMap<>();
                Map<String, Object> idField = new HashMap<>();
                for (Map.Entry<String, Boolean> entry : distinct.entrySet()) {
                    idField.put(entry.getKey(), "$" + entry.getKey());
                    Map<String, String> field = new HashMap<>();
                    field.put("$last", "$" + entry.getKey());
                    groupBy.put(entry.getKey(), field);
                }
                groupBy.put(MetaField.Id, idField);
                pipeline.add((new BasicDBObject()).append("$group", groupBy));

                // if there's no _id, add it and set to false since mongoDB treat missing as true
                distinct.put(MetaField.Id, false);
            } else {
                // if there's an _id, remove it since mongoDB treat missing as true
                distinct.remove(MetaField.Id);
            }

            DBObject keys = new BasicDBObject(distinct);
            pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_PROJECT, keys));
        }

        // group
        if (group != null) {
            pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_GROUP, group));
        }

        // sort
        if (sort != null && !sort.isEmpty()) {
            pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_SORT, sort));
        }

        // skip and limit
        if (skip != null) {
            pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_SKIP, skip));
        }
        if (limit != null) {
            pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_LIMIT, limit));
        }

        // projection
        if (project != null) {
            if (!project.containsKey(MetaField.Id)) {
                // if there's no _id, add it and set to false since mongoDB treat missing as true
                project.put(MetaField.Id, false);
            } else {
                // if there's an _id, remove it since mongoDB treat missing as true
                project.remove(MetaField.Id);
            }
            DBObject keys = new BasicDBObject(project);
            pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_PROJECT, keys));
        }

        Builder optionsBuiler = AggregationOptions.builder();
        optionsBuiler.allowDiskUse(Boolean.TRUE).outputMode(AggregationOptions.OutputMode.CURSOR);

        DBCollection collection = mongoTemplate.getCollection(collectionName);
        Cursor cursor = collection.aggregate(pipeline, optionsBuiler.build());
        if (outputFile == null) {
            while (cursor.hasNext()) {
                Map doc = cursor.next().toMap();
                result.add(doc);
            }
        } else {
            // to file
            try (FileWriter fileWriter = new FileWriter(outputFile)) {
                fileWriter.write("[\n");
                boolean isFirst = true;
                while (cursor.hasNext()) {
                    Map doc = cursor.next().toMap();
                    if (isFirst) {
                        isFirst = false;
                        fileWriter.write(DatasetUtils.serialize(doc) + "\n");
                    } else {
                        fileWriter.write(", " + DatasetUtils.serialize(doc) + "\n");
                    }
                }
                fileWriter.write("]");
            } catch (IOException ex) {
                throw new RuntimeException("failed to write dataset into file: " + outputFile.getName(), ex);
            }
        }

        return result;
    }

    @Override
    public long count(UUID termUuid, UUID termVersion, Map<String, Object> query, List<EnumDatasetState> states) {
        if (termUuid == null) {
            return -1;
        }

        Integer tenantId = edu.purdue.cybercenter.dm.threadlocal.TenantId.get();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        if (tenantId == null || userId == null) {
            return -1;
        }

        String collectionName = DatasetUtils.makeCollectionName(termUuid, false);
        if (!mongoTemplate.collectionExists(collectionName)) {
            return -1;
        }

        QueryBuilder qb = buildQuery(query, states, tenantId, termVersion);

        DBCollection collection = mongoTemplate.getCollection(collectionName);
        return collection.count(qb.get());
    }

    @Override
    public Map<String, Object> save(UUID termUuid, UUID termVersion, Map<String, Object> value, EnumDatasetState state) {
        if (value == null) {
            return null;
        }

        UUID versionFromDocument = (UUID) value.get(MetaField.TemplateVersion);
        if (termUuid == null || (termVersion == null && versionFromDocument == null)) {
            throw new RuntimeException(String.format("Save failed: Both UUID: %s and version: %s must be present", termUuid, termVersion));
        }

        Integer tenantId = edu.purdue.cybercenter.dm.threadlocal.TenantId.get();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        if (tenantId == null || userId == null) {
            return null;
        }

        DBObject document = new BasicDBObject(value);

        // remove object version of project, experiment, job, task and user
        document.removeField(MetaField.User);
        document.removeField(MetaField.Project);
        document.removeField(MetaField.Experiment);
        document.removeField(MetaField.Job);
        document.removeField(MetaField.Task);
        document.removeField(MetaField.LocalVariables);

        // add cris controlled fields
        if (termVersion != null) {
            document.put(MetaField.TemplateVersion, termVersion);
        }

        Date date = new Date();
        document.put(MetaField.TimeUpdated, date);
        document.put(MetaField.UpdaterId, userId);

        boolean isNewDocument;
        ObjectId objectId = (ObjectId) document.get(MetaField.Id);
        if (objectId == null) {
            isNewDocument = true;
            objectId = new ObjectId();
        } else {
            isNewDocument = false;
        }

        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.put(MetaField.Id).is(objectId);
        String collectionName = DatasetUtils.makeCollectionName(termUuid, false);
        DBCollection collection = mongoTemplate.getCollection(collectionName);
        if (!isNewDocument) {
            long count = collection.count(queryBuilder.get());
            isNewDocument = (count == 0);
        }

        if (isNewDocument) {
            document.put(MetaField.Id, objectId);
            document.put(MetaField.TenantId, tenantId);
            document.put(MetaField.TimeCreated, date);
            document.put(MetaField.CreatorId, userId);
            if (state != null) {
                // state from method parameter has highest precedence
                document.put(MetaField.State, state.getIndex());
            } else {
                // then the state comes with the document
                Integer docState = (Integer) document.get(MetaField.State);
                if (docState == null) {
                    // then the default
                    document.put(MetaField.State, EnumDatasetState.Operational.getIndex());
                }
            }
        } else {
            /*
             * the following fields cannot be updated
             * so we use the original no matter what is in the new document
             */
            document.removeField(MetaField.TenantId);
            document.removeField(MetaField.TimeCreated);
            document.removeField(MetaField.CreatorId);
            document.removeField(MetaField.State);

            document.removeField(MetaField.ProjectId);
            document.removeField(MetaField.ExperimentId);
            document.removeField(MetaField.JobId);
            document.removeField(MetaField.TaskId);
        }

        DBObject upsertDocument = new BasicDBObject();
        upsertDocument.put("$set", document);
        collection.update(queryBuilder.get(), upsertDocument, true, false);

        // retrieve the saved document
        DBObject savedDocument = collection.findOne(queryBuilder.get());

        // save to audit collection
        ObjectId _id = (ObjectId) savedDocument.get(MetaField.Id);
        int revtype = isNewDocument ? 0 : 1;

        // make necessary changes for the audit version of the document
        savedDocument.put(FIELD_ORIGINAL_ID, _id);
        savedDocument.put(FIELD_REV_TYPE, revtype);
        savedDocument.removeField(MetaField.Id);

        String collectionNameAudit = DatasetUtils.makeCollectionName(termUuid, true);
        DBCollection collectionAudit = mongoTemplate.getCollection(collectionNameAudit);
        collectionAudit.save(savedDocument);

        // put document back in its original state
        savedDocument.put(MetaField.Id, _id);
        savedDocument.removeField(FIELD_REV_TYPE);
        savedDocument.removeField(FIELD_ORIGINAL_ID);

        return savedDocument.toMap();
    }

    @Override
    public void delete(UUID termUuid, UUID termVersion, Map<String, Object> query, List<EnumDatasetState> states) {
        QueryBuilder qb = new QueryBuilder();

        if (query != null && !query.isEmpty()) {
            query.entrySet().stream().forEach((entry) -> {
                qb.put(entry.getKey()).is(entry.getValue());
            });
        }

        Integer tenantId = edu.purdue.cybercenter.dm.threadlocal.TenantId.get();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        List<Integer> stateQuery = DatasetUtils.convertEnumStatesToIntegerStates(states);
        qb.put(MetaField.State).in(stateQuery);
        qb.put(MetaField.TenantId).is(tenantId);

        if (termVersion != null) {
            qb.put(MetaField.TemplateVersion).is(termVersion);
        }

        String collectionName = DatasetUtils.makeCollectionName(termUuid, false);
        DBCollection collection = mongoTemplate.getCollection(collectionName);
        DBCursor cursor = collection.find(qb.get());
        while (cursor.hasNext()) {
            DBObject doc = cursor.next();
            collection.remove(doc);
        }
    }

    @Override
    public Set<String> getCollectionNames() {
        return mongoTemplate.getCollectionNames();
    }

    @Override
    public void update(String collectionName, Map<String, Object> query, Map<String, Object> value) {
        QueryBuilder qb = new QueryBuilder();
        if (query != null && !query.isEmpty()) {
            query.entrySet().stream().forEach((entry) -> {
                qb.put(entry.getKey()).is(entry.getValue());
            });
        }

        BasicDBObject document = new BasicDBObject();
        document.append("$set", new BasicDBObject(value));

        DBCollection collection = mongoTemplate.getCollection(collectionName);
        collection.update(qb.get(), document, false, true);
    }

}
