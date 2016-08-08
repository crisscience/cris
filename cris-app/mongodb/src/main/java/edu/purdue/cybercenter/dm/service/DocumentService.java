package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.util.EnumDatasetState;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public interface DocumentService {

    public static final String AGGREGATOR_MATCH = "$match";
    public static final String AGGREGATOR_SORT = "$sort";
    public static final String AGGREGATOR_SKIP = "$skip";
    public static final String AGGREGATOR_LIMIT = "$limit";
    public static final String AGGREGATOR_PROJECT = "$project";
    public static final String AGGREGATOR_DISTINCT = "$distinct";
    public static final String AGGREGATOR_GROUP = "$group";

    // this set of methods does not have dataset state parameter and default to "Operational"
    // simply call the next set of metheds with state equals "Operational"
    public abstract Map<String, Object> findById(UUID termUuid, ObjectId id);
    public abstract List<Map<String, Object>> find(UUID termUuid, UUID termVersion, Map<String, Object> aggregators);
    public abstract List<Map<String, Object>> find(UUID termUuid, UUID termVersion, Map<String, Object> aggregators, File outputFile);

    public abstract long count(UUID termUuid, UUID termVersion, Map<String, Object> query);

    public abstract Map<String, Object> save(UUID termUuid, UUID termVersion, Map<String, Object> value);

    public abstract void delete(UUID termUuid, UUID termVersion, Map<String, Object> query);

    // this set of methods has dataset state parameter
    public abstract Map<String, Object> findById(UUID termUuid, ObjectId id, List<EnumDatasetState> states);
    public abstract List<Map<String, Object>> find(UUID termUuid, UUID termVersion, Map<String, Object> aggregators, List<EnumDatasetState> states);
    public abstract List<Map<String, Object>> find(UUID termUuid, UUID termVersion, Map<String, Object> aggregators, List<EnumDatasetState> states, File outputFile);

    public abstract long count(UUID termUuid, UUID termVersion, Map<String, Object> query, List<EnumDatasetState> states);

    public abstract Map<String, Object> save(UUID termUuid, UUID termVersion, Map<String, Object> value, EnumDatasetState state);

    public abstract void delete(UUID termUuid, UUID termVersion, Map<String, Object> query, List<EnumDatasetState> states);

    public abstract Set<String> getCollectionNames();
    public abstract void update(String collectionName, Map<String, Object> query, Map<String, Object> value);
}
