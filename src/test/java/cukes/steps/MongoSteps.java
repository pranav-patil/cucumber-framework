package cukes.steps;


import com.library.mongodb.domain.Sequence;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cukes.helper.FieldTypeService;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class MongoSteps extends BaseStepDefinition {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private FieldTypeService fieldTypeService;
    @Resource(name = "mongoPackages")
    private List<String> mongoPackages;

    private Map<String, Class<?>> collectionClassMap = new HashMap<>();
    private static final Pattern ARRAY_PATTERN = Pattern.compile("([_A-Za-z0-9]+)\\[([0-9]*)\\]");

    @PostConstruct
    public void init() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
        scanner.addIncludeFilter(new AnnotationTypeFilter(org.springframework.data.mongodb.core.mapping.Document.class));

        for (String domainPackage : mongoPackages) {
            Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(domainPackage);

            if(beanDefinitions != null) {
                for (BeanDefinition beanDef : beanDefinitions) {

                    MultiValueMap<String, Object> document = ((AnnotatedBeanDefinition) beanDef).getMetadata()
                            .getAllAnnotationAttributes(org.springframework.data.mongodb.core.mapping.Document.class.getName());

                    Class<?> clazz = Class.forName(beanDef.getBeanClassName());
                    String collectionName = clazz.getSimpleName();
                    collectionName = Character.toLowerCase(collectionName.charAt(0)) + collectionName.substring(1);

                    if (document != null && document.containsKey("collection")) {
                        String collection = (String) document.getFirst("collection");
                        if (!StringUtils.isBlank(collection)) {
                            collectionName = collection;
                        }
                    }

                    collectionClassMap.put(collectionName, clazz);
                }
            }
        }
    }

    @Given("^collection \"(.*?)\" has no records$")
    public void collectionHasData(String collection) {
        mongoTemplate.remove(new Query(), collection);
    }

    @Given("^collection \"(.*?)\" has data$")
    public void collectionHasData(String collection, DataTable dataTable) {

        if (!mongoTemplate.collectionExists(collection)) {
            mongoTemplate.createCollection(collection);
        }

        Class<?> collectionClass = getCollectionClass(collection);
        List<Map<String, String>> dataTableMaps = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> map : dataTableMaps) {

            final Document document = new Document("_class", collectionClass.getName());
            addBasicDBObject(map, document, collectionClass);

            mongoTemplate.execute(collectionClass, new CollectionCallback<Object>() {
                @Override
                public Object doInCollection(MongoCollection<Document> mongoCollection) throws MongoException, DataAccessException {
                    mongoCollection.insertOne(document);
                    return null;
                }
            });
        }
    }

    @Given("^MongoDB sequence \"(.*?)\" has counter (\\d+)$")
    public void setSequenceCounter(String sequenceName, Long startCounter) {
        Query query = new Query(Criteria.where("name").is(sequenceName));
        Update update = new Update().set("counter", startCounter);
        UpdateResult updateResult = mongoTemplate.upsert(query, update, Sequence.class);

        if(!updateResult.wasAcknowledged() || (updateResult.getModifiedCount() == 0 && updateResult.getUpsertedId() == null)) {
            throw new RuntimeException(String.format("Sequence Insert/Update failure: %s", updateResult));
        }
    }

    @Given("^insert a record into MongoDB collection \"(.*?)\" with the document")
    public void insertCollectionRecordsWithDocument$(String collection, String insertDocument) {
        MongoCollection<Document> dbCollection = mongoTemplate.getCollection(collection);
        Document insertObject = (Document) JSON.parse(insertDocument);
        dbCollection.insertOne(insertObject);
    }

    @Given("^delete records from MongoDB collection \"(.*?)\" which match the conditions$")
    public void deleteCollectionRecordsWithConditions$(String collection, DataTable dataTable) {
        Class<?> collectionClass = getCollectionClass(collection);
        Query findQuery = getFindQuery(getMap(dataTable), collectionClass);
        mongoTemplate.remove(findQuery, collectionClass);
    }

    @And("^Drop collection \"(.*?)\"")
    public void dropCollection(String collection) {
        mongoTemplate.dropCollection(collection);
    }

    @Then("^Verify that MongoDB collection \"(.*?)\" has (\\d+) records which match the conditions$")
    public void verifyCollectionRecordsWithConditions$(String collection, int numberOfRecords, DataTable dataTable) {
        Class<?> collectionClass = getCollectionClass(collection);
        Query findQuery = getFindQuery(getMap(dataTable), collectionClass);
        List<?> results = mongoTemplate.find(findQuery, collectionClass);

        if(results != null) {
            assertEquals(numberOfRecords, results.size());
        } else {
            assertEquals("MongoDB results are empty", numberOfRecords, 0);
        }
    }

    @Then("^Verify that MongoDB collection \"(.*?)\" has (\\d+) records which match the query")
    public void verifyCollectionRecordsWithQuery$(String collection, int numberOfRecords, String findQuery) {
        Document dbDocument = (Document) JSON.parse(findQuery);
        MongoCollection<Document> dbCollection = mongoTemplate.getCollection(collection);
        long count = dbCollection.count(dbDocument);
        assertEquals(numberOfRecords, count);
    }

    private Class<?> getCollectionClass(String collection) {
        Class<?> collectionClass = collectionClassMap.get(collection);

        if(collectionClass == null) {
            throw new IllegalArgumentException(String.format("MongoDB collection class %s does not exist in domain packages %s", collection, mongoPackages));
        }
        return collectionClass;
    }

    private void addBasicDBObject(Map<String, String> map, Document document, Class<?> collectionClass) {

        for (Map.Entry<String, String> entry : map.entrySet()) {

            String key = entry.getKey();
            Object value = fieldTypeService.getObject(collectionClass, key, entry.getValue());

            if(key.contains(".")) {
                String[] tokens = key.split("\\.");
                setNestedFields(document, tokens, 0, value);
            } else {
                document.put(key, value);
            }
        }
    }

    private void setNestedFields(Map<String, Object> dbEntryMap, String[] tokens, int currentToken, Object value) {

        if (currentToken >= tokens.length) {
            return;
        }

        String token = tokens[currentToken];
        BasicDBObject nestedDocument;

        Matcher matcher = ARRAY_PATTERN.matcher(token);

        if (matcher.matches()) {

            String arrayName = matcher.group(1);
            BasicDBList array;

            if(dbEntryMap.containsKey(arrayName)) {
                array = (BasicDBList) dbEntryMap.get(arrayName);
            } else {
                array = new BasicDBList();
                dbEntryMap.put(arrayName, array);
            }

            int arrayIndex = Integer.parseInt(matcher.group(2));

            if(currentToken == tokens.length - 1) {
                addToArray(array, arrayIndex, value);
                return;
            }

            if(arrayIndex >= array.size()) {
                nestedDocument = new BasicDBObject();
                array.add(arrayIndex, nestedDocument);
            } else {
                nestedDocument = (BasicDBObject) array.get(arrayIndex);
            }
        }
        else if(!dbEntryMap.containsKey(token)) {
            nestedDocument = new BasicDBObject();
            dbEntryMap.put(token, nestedDocument);
        } else {
            nestedDocument = (BasicDBObject) dbEntryMap.get(token);
        }

        currentToken++;

        if (currentToken == tokens.length - 1 && !tokens[currentToken].contains("[")) {
            nestedDocument.put(tokens[currentToken], value);
            return;
        }

        setNestedFields(nestedDocument, tokens, currentToken, value);
    }

    private Query getFindQuery(Map<String, String> matchConditions, Class<?> collectionClass) {

        Query query = new Query();

        if (matchConditions == null) {
            return query;
        }

        for (Map.Entry<String, String> entry : matchConditions.entrySet()) {

            if (!StringUtils.isBlank(entry.getKey())) {
                Object value = fieldTypeService.getObject(collectionClass, entry.getKey(), entry.getValue());
                query.addCriteria(Criteria.where(entry.getKey().trim()).is(value));
            }
        }

        return query;
    }

    private void addToArray(List<Object> array, int index, Object value) {

        int elementsToAdd = (index - array.size()) + 1;

        for (int i = 0; i < elementsToAdd; i++) {
            array.add(null);
        }

        array.set(index, value);
    }
}