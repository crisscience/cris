package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.service.DocumentService;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.EnumDatasetState;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@ComponentScan(basePackages = "edu.purdue.cybercenter.dm.web")
public class BaseWithAdminUserControllerTest extends BaseControllerTest {

    @Autowired
    private DocumentService documentService;

    private Date startDate;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        setupWorkspace();

        loginAdminUser();

        startDate = new Date();
    }

    @After
    @Override
    public void tearDown() {
        // cleanup
        List<EnumDatasetState> states = new ArrayList<>();
        states.add(EnumDatasetState.Archived);
        states.add(EnumDatasetState.Deprecated);
        states.add(EnumDatasetState.Operational);
        states.add(EnumDatasetState.Sandboxed);
        states.add(EnumDatasetState.Temporary);

        Map<String, Date> date = new HashMap<>();
        date.put("$gte", startDate);
        Map<String, Object> query = new HashMap<>();
        query.put("_time_created", date);

        Set<String> collectionNames = documentService.getCollectionNames();
        collectionNames.stream().map((collectionName) -> DatasetUtils.collectionNameToUuid(collectionName)).filter((uuid) -> (uuid != null)).forEach((uuid) -> {
            documentService.delete(uuid, null, query, states);
        });
    }
}
