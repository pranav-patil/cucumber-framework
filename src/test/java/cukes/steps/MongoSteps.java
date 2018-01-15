package cukes.steps;


import com.library.mongodb.domain.Sequence;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
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
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cukes.stub.DateStubService.DEFAULT_DATE_FORMAT;
import static org.junit.Assert.assertEquals;

public class MongoSteps extends BaseStepDefinition {

    @Autowired
    private MongoTemplate mongoTemplate;
    private Map<String, Class<?>> collectionClassMap = new HashMap<>();

    private static final SimpleDateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    private static final String[] MONGO_DOMAIN_PACKAGES = {"com.library.mongodb.domain" };
    private static final Pattern ARRAY_PATTERN = Pattern.compile("([_A-Za-z0-9]+)\\[([0-9]*)\\]");

    @PostConstruct
    public void initIt() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
        scanner.addIncludeFilter(new AnnotationTypeFilter(org.springframework.data.mongodb.core.mapping.Document.class));

        Set<BeanDefinition> domainBeans = null;

        for (String domainPackage : MONGO_DOMAIN_PACKAGES) {
            Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(domainPackage);

            if(domainBeans == null) {
                domainBeans = beanDefinitions;
            } else {
                domainBeans.addAll(beanDefinitions);
            }
        }

        for (BeanDefinition beanDef : domainBeans) {

            MultiValueMap<String, Object> document = ((AnnotatedBeanDefinition) beanDef).getMetadata()
                    .getAllAnnotationAttributes(org.springframework.data.mongodb.core.mapping.Document.class.getName());

            if(document != null) {
                Class<?> clazz = Class.forName(beanDef.getBeanClassName());
                String collectionName = clazz.getSimpleName();
                collectionName = Character.toLowerCase(collectionName.charAt(0)) + collectionName.substring(1);

                if (document.containsKey("collection")) {
                    String collection = (String) document.getFirst("collection");
                    if (!StringUtils.isBlank(collection)) {
                        collectionName = collection;
                    }
                }

                collectionClassMap.put(collectionName, clazz);
            }
        }
    }

    @Given("^collection \"(.*?)\" has no records$")
    public void collectionHasData(String collection) throws Throwable {
        mongoTemplate.remove(new Query(), collection);
    }

    @Given("^collection \"(.*?)\" has data$")
    public void collectionHasData(String collection, DataTable dataTable) throws Throwable {

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
    public void setSequenceCounter(String sequenceName, Long startCounter) throws Throwable {
        Query query = new Query(Criteria.where("name").is(sequenceName));
        Update update = new Update().set("counter", startCounter);
        UpdateResult updateResult = mongoTemplate.upsert(query, update, Sequence.class);

        if(!updateResult.wasAcknowledged() || (updateResult.getModifiedCount() == 0 && updateResult.getUpsertedId() == null)) {
            throw new RuntimeException(String.format("Sequence Insert/Update failure: %s", updateResult));
        }
    }

    @Given("^insert a record into MongoDB collection \"(.*?)\" with the document")
    public void insertCollectionRecordsWithDocument$(String collection, String insertDocument) throws Exception {
        MongoCollection<Document> dbCollection = mongoTemplate.getCollection(collection);
        Document insertObject = (Document) JSON.parse(insertDocument);
        dbCollection.insertOne(insertObject);
    }

    @Given("^delete records from MongoDB collection \"(.*?)\" which match the conditions$")
    public void deleteCollectionRecordsWithConditions$(String collection, DataTable dataTable) throws Exception {
        Class<?> collectionClass = getCollectionClass(collection);
        Query findQuery = getFindQuery(getMap(dataTable), collectionClass);
        mongoTemplate.remove(findQuery, collectionClass);
    }

    @And("^Drop collection \"(.*?)\"")
    public void dropCollection(String collection) throws Exception {
        mongoTemplate.dropCollection(collection);
    }

    @Then("^Verify that MongoDB collection \"(.*?)\" has (\\d+) records which match the conditions$")
    public void verifyCollectionRecordsWithConditions$(String collection, int numberOfRecords, DataTable dataTable) throws Exception {
        Class<?> collectionClass = getCollectionClass(collection);
        Map<String, String> map = getMap(dataTable);
        Query findQuery = getFindQuery(getMap(dataTable), collectionClass);
        List<?> results = mongoTemplate.find(findQuery, collectionClass);

        if(results != null) {
            assertEquals(numberOfRecords, results.size());
        } else {
            assertEquals("MongoDB results are empty", numberOfRecords, 0);
        }
    }

    @Then("^Verify that MongoDB collection \"(.*?)\" has (\\d+) records which match the query")
    public void verifyCollectionRecordsWithQuery$(String collection, int numberOfRecords, String findQuery) throws Exception {
        Document dbDocument = (Document) JSON.parse(findQuery);
        MongoCollection<Document> dbCollection = mongoTemplate.getCollection(collection);
        long count = dbCollection.count(dbDocument);
        assertEquals(numberOfRecords, count);
    }

    private Class<?> getCollectionClass(String collection) {
        Class<?> collectionClass = collectionClassMap.get(collection);

        if(collectionClass == null) {
            throw new IllegalArgumentException(String.format("MongoDB collection class %s does not exist in domain packages %s", collection, MONGO_DOMAIN_PACKAGES));
        }
        return collectionClass;
    }

    private void addBasicDBObject(Map<String, String> map, Document document, Class<?> collectionClass) {

        for (Map.Entry<String, String> entry : map.entrySet()) {

            String key = entry.getKey();
            Object value = getObject(collectionClass, key, entry.getValue());

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
                Object value = getObject(collectionClass, entry.getKey(), entry.getValue());
                query.addCriteria(Criteria.where(entry.getKey().trim()).is(value));
            }
        }

        return query;
    }

    private Object getObject(Class<?> collectionClass, String fieldName, String fieldValue) {
        Field field = getDeclaredField(collectionClass, fieldName);
        if (field != null) {
            return toObject(field.getType(), fieldValue);
        } else {
            return detectObjectType(fieldValue);
        }
    }

    private Field getDeclaredField(Class<?> type, String fieldName) {
        try {
            return type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)) {
                return getDeclaredField(type.getSuperclass(), fieldName);
            }
        }
        return null;
    }

    public Object toObject(Class clazz, String value) {

        if (value == null || "null".equals(value)) {
            return null;
        }

        if (Date.class == clazz) {
            try {
                return DEFAULT_DATE_FORMATTER.parse(value);
            } catch (ParseException pex) {
            }
        }

        if (String.class == clazz) return value;
        if (Boolean.class == clazz || boolean.class == clazz) return Boolean.parseBoolean(value);
        if (Byte.class == clazz || byte.class == clazz) return Byte.parseByte(value);
        if (Short.class == clazz || short.class == clazz) return Short.parseShort(value);
        if (Integer.class == clazz || int.class == clazz) return Integer.parseInt(value);
        if (Long.class == clazz || long.class == clazz) return Long.parseLong(value);
        if (Float.class == clazz || float.class == clazz) return Float.parseFloat(value);
        if (Double.class == clazz || double.class == clazz) return Double.parseDouble(value);
        return value;
    }

    public Object detectObjectType(String string) {

        if (string.matches("(true|false)")) {
            return Boolean.valueOf(string);
        }

        if (string.equals("null")) {
            return null;
        }

        try {
            return DEFAULT_DATE_FORMATTER.parse(string);
        } catch (ParseException pex) {
        }

        return string;
    }

    private void addToArray(List<Object> array, int index, Object value) {

        int elementsToAdd = (index - array.size()) + 1;

        for (int i = 0; i < elementsToAdd; i++) {
            array.add(null);
        }

        array.set(index, value);
    }
}