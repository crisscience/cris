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
import com.mongodb.WriteConcern;
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
        return save(termUuid, termVersion, value, null);
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

    private QueryBuilder buildFilter(Integer tenantId, Integer groupId, Integer userId, List<Integer> projectIds, List<Integer> experimentIds, List<Integer>  ownerProjectIds, List<Integer> ownerExperimentIds, boolean isAdmin) {
        QueryBuilder qbFilter = new QueryBuilder();

        qbFilter.put(MetaField.TenantId).is(tenantId);

        if (!isAdmin) {
            QueryBuilder qbUserOrGroup = new QueryBuilder();
            QueryBuilder qbUser = new QueryBuilder();
            qbUser.put(MetaField.IsGroupOwner).is(false);
            qbUser.put(MetaField.OwnerId).is(userId);
            QueryBuilder qbGroup = new QueryBuilder();
            qbGroup.put(MetaField.IsGroupOwner).is(true);
            qbGroup.put(MetaField.OwnerId).is(groupId);
            qbUserOrGroup.or(qbUser.get(), qbGroup.get());

//            QueryBuilder qbOwnerProjectsOrExperiments = new QueryBuilder();
            QueryBuilder qbOwnerProjects = new QueryBuilder();
            qbOwnerProjects.put(MetaField.ProjectId).in(ownerProjectIds);
//            QueryBuilder qbOwnerExperiments = new QueryBuilder();
//            qbOwnerExperiments.put(MetaField.ExperimentId).in(ownerExperimentIds);
//            qbOwnerProjectsOrExperiments.or(qbOwnerProjects.get(), qbOwnerExperiments.get());

            QueryBuilder qbOwners = new QueryBuilder();
            qbOwners.and(qbUserOrGroup.get(), qbOwnerProjects.get());

//            QueryBuilder qbProjectsOrExperiments = new QueryBuilder();
            QueryBuilder qbProjects = new QueryBuilder();
            qbProjects.put(MetaField.ProjectId).in(projectIds);
//            QueryBuilder qbExperiments = new QueryBuilder();
//            qbExperiments.put(MetaField.ExperimentId).in(experimentIds);
//            qbProjectsOrExperiments.or(qbProjects.get(), qbExperiments.get());

            qbFilter.or(qbOwners.get(), qbProjects.get());
        }

        return qbFilter;
    }

    private QueryBuilder buildQuery(Map<String, Object> query, List<EnumDatasetState> states, UUID termVersion) {
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

        if (termVersion != null) {
            qb.put(MetaField.TemplateVersion).is(termVersion);
        }

        return qb;
    }

    private void addDistinct(List<DBObject> pipeline, Map<String, Boolean> distinct) {
        // distinct
        DBObject keys = new BasicDBObject(distinct);
        if (!keys.containsField(MetaField.Id)) {
            // if there's no _id, then there may be duplicates
            Map<String, Object> groupBy = new HashMap<>();
            Map<String, Object> idField = new HashMap<>();
            for (String key : keys.keySet()) {
                idField.put(key, "$" + key);
                Map<String, String> field = new HashMap<>();
                field.put("$last", "$" + key);
                groupBy.put(key, field);
            }
            groupBy.put(MetaField.Id, idField);
            pipeline.add((new BasicDBObject()).append("$group", groupBy));

            // if there's no _id, add it and set to false since mongoDB treat missing as true
            keys.put(MetaField.Id, false);
        } else {
            // if there's an _id, remove it since mongoDB treat missing as true
            keys.removeField(MetaField.Id);
        }

        pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_PROJECT, keys));
    }

    private List<DBObject> addProject(List<DBObject> pipeline, Map<String, Boolean> project) {
        // projection
        DBObject keys = new BasicDBObject(project);
        if (!keys.containsField(MetaField.Id)) {
            // if there's no _id, add it and set to false since mongoDB treat missing as true
            keys.put(MetaField.Id, false);
        } else {
            // if there's an _id, remove it since mongoDB treat missing as true
            keys.removeField(MetaField.Id);
        }
        pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_PROJECT, keys));

        return pipeline;
    }

    private List<DBObject> addCount(List<DBObject> pipeline) {
        DBObject count = new BasicDBObject();
        count.put("_id", null);

        DBObject sum = new BasicDBObject();
        sum.put("$sum", 1);
        count.put("count", sum);
        pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_GROUP, count));
        return pipeline;
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
        Integer groupId = edu.purdue.cybercenter.dm.threadlocal.GroupId.get();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        if (tenantId == null || userId == null) {
            return result;
        }

        if (aggregators == null) {
            aggregators = new HashMap<>();
        }

        Boolean isAdmin = (Boolean) aggregators.get(DocumentService.IS_ADMIN);
        List<Integer> projectIds = (List) aggregators.get(DocumentService.PROJECT_IDS);
        List<Integer> experimentIds = (List) aggregators.get(DocumentService.EXPERIMENT_IDS);
        List<Integer> ownerProjectIds = (List) aggregators.get(DocumentService.OWNER_PROJECT_IDS);
        List<Integer> ownerExperimentIds = (List) aggregators.get(DocumentService.OWNER_EXPERIMENT_IDS);
        if (isAdmin == null) {
            isAdmin = false;
        }
        if (projectIds == null) {
            projectIds = new ArrayList<>();
        }
        if (experimentIds == null) {
            experimentIds = new ArrayList<>();
        }
        QueryBuilder qbFilter = buildFilter(tenantId, groupId, userId, projectIds, experimentIds, ownerProjectIds, ownerExperimentIds, isAdmin);

        Map<String, Object> match = (Map<String, Object>) aggregators.get(DocumentService.AGGREGATOR_MATCH);
        Map<String, Boolean> distinct = (Map<String, Boolean>) aggregators.get(DocumentService.AGGREGATOR_DISTINCT);
        Map<String, Object> group = (Map<String, Object>) aggregators.get(DocumentService.AGGREGATOR_GROUP);
        Map<String, Integer> sort = (Map<String, Integer>) aggregators.get(DocumentService.AGGREGATOR_SORT);
        Integer skip = (Integer) aggregators.get(DocumentService.AGGREGATOR_SKIP);
        Integer limit = (Integer) aggregators.get(DocumentService.AGGREGATOR_LIMIT);
        Map<String, Boolean> project = (Map<String, Boolean>) aggregators.get(DocumentService.AGGREGATOR_PROJECT);

        List<DBObject> pipeline = new ArrayList<>();

        QueryBuilder qbQuery = buildQuery(match, states, termVersion);
        qbQuery.and(qbFilter.get());

        // match
        pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_MATCH, qbQuery.get()));

        // distinct
        if (distinct != null) {
            addDistinct(pipeline, distinct);
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
            addProject(pipeline, project);
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
    public long count(UUID termUuid, UUID termVersion, Map<String, Object> aggregators, List<EnumDatasetState> states) {
        if (termUuid == null) {
            return -1;
        }

        String collectionName = DatasetUtils.makeCollectionName(termUuid, false);
        if (!mongoTemplate.collectionExists(collectionName)) {
            return -1;
        }

        Integer tenantId = edu.purdue.cybercenter.dm.threadlocal.TenantId.get();
        Integer groupId = edu.purdue.cybercenter.dm.threadlocal.GroupId.get();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        if (tenantId == null || userId == null) {
            return -1;
        }

        Boolean isAdmin = (Boolean) aggregators.get(DocumentService.IS_ADMIN);
        List<Integer> projectIds = (List) aggregators.get(DocumentService.PROJECT_IDS);
        List<Integer> experimentIds = (List) aggregators.get(DocumentService.EXPERIMENT_IDS);
        List<Integer> ownerProjectIds = (List) aggregators.get(DocumentService.OWNER_PROJECT_IDS);
        List<Integer> ownerExperimentIds = (List) aggregators.get(DocumentService.OWNER_EXPERIMENT_IDS);
        if (isAdmin == null) {
            isAdmin = false;
        }
        if (projectIds == null) {
            projectIds = new ArrayList<>();
        }
        if (experimentIds == null) {
            experimentIds = new ArrayList<>();
        }
        QueryBuilder qbFilter = buildFilter(tenantId, groupId, userId, projectIds, experimentIds, ownerProjectIds, ownerExperimentIds, isAdmin);

        Map<String, Object> match = (Map<String, Object>) aggregators.get(DocumentService.AGGREGATOR_MATCH);
        Map<String, Boolean> distinct = (Map<String, Boolean>) aggregators.get(DocumentService.AGGREGATOR_DISTINCT);
        Map<String, Object> group = (Map<String, Object>) aggregators.get(DocumentService.AGGREGATOR_GROUP);
        Map<String, Boolean> project = (Map<String, Boolean>) aggregators.get(DocumentService.AGGREGATOR_PROJECT);

        QueryBuilder qbQuery = buildQuery(match, states, termVersion);
        qbQuery.and(qbFilter.get());

        List<DBObject> pipeline = new ArrayList<>();

        // match
        pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_MATCH, qbQuery.get()));

        // distinct
        if (distinct != null) {
            addDistinct(pipeline, distinct);
        }

        // group
        if (group != null) {
            pipeline.add((new BasicDBObject()).append(DocumentService.AGGREGATOR_GROUP, group));
        }

        // projection
        if (project != null) {
            addProject(pipeline, project);
        }

        // count documents
        addCount(pipeline);

        DBCollection collection = mongoTemplate.getCollection(collectionName);

        Builder optionsBuiler = AggregationOptions.builder();
        optionsBuiler.allowDiskUse(Boolean.TRUE).outputMode(AggregationOptions.OutputMode.CURSOR);
        Cursor cursor = collection.aggregate(pipeline, optionsBuiler.build());
        int size;
        if (cursor.hasNext()) {
            size = (Integer) cursor.next().toMap().get("count");
        } else {
            size = 0;
        }

        return size;
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
        Integer groupId = edu.purdue.cybercenter.dm.threadlocal.GroupId.get();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        if (tenantId == null || userId == null) {
            return null;
        }

        DBObject document = new BasicDBObject(value);

        // remove object version of project, experiment, job, task and user, and other fileds that should not be saved
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
        document.put(MetaField.UpdaterGroupId, groupId);

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
            document.put(MetaField.CreatorGroupId, groupId);

            Boolean isGroupOwner = (Boolean) document.get(MetaField.IsGroupOwner);
            if (isGroupOwner == null) {
                document.put(MetaField.IsGroupOwner, null);
            }
            document.put(MetaField.OwnerId, isGroupOwner == null ? null : (isGroupOwner ? groupId : userId));

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
            document.removeField(MetaField.CreatorGroupId);
            document.removeField(MetaField.State);

            document.removeField(MetaField.ProjectId);
            document.removeField(MetaField.ExperimentId);
            document.removeField(MetaField.JobId);
            document.removeField(MetaField.TaskId);
            document.removeField(MetaField.ContextId);
        }

        DBObject upsertDocument = new BasicDBObject();
        upsertDocument.put("$set", document);
        collection.update(queryBuilder.get(), upsertDocument, true, false, new WriteConcern(1));

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
