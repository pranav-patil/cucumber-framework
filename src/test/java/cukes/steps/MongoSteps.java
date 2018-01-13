package cukes.steps;


import com.library.mongodb.Sequence;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static cukes.stub.DateStubService.DEFAULT_DATE_FORMAT;
import static org.junit.Assert.assertEquals;

public class MongoSteps extends BaseStepDefinition {

    public static final SimpleDateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    private static final String[] MONGO_DOMAIN_PACKAGES = {"com.library.mongodb" };
    @Autowired
    private MongoTemplate mongoTemplate;
    private Map<String, Class<?>> collectionClassMap = new HashMap<>();

    @PostConstruct
    public void initIt() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Document.class));

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

            MultiValueMap<String, Object> document = ((AnnotatedBeanDefinition) beanDef).getMetadata().getAllAnnotationAttributes(Document.class.getName());

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

            final org.bson.Document document = new org.bson.Document("_class", collectionClass.getName());
            addBasicDBObject(map, document, collectionClass);

            mongoTemplate.execute(collectionClass, new CollectionCallback<Object>() {
                @Override
                public Object doInCollection(MongoCollection<org.bson.Document> mongoCollection) throws MongoException, DataAccessException {
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

        if(updateResult.getModifiedCount() == 0) {
            throw new RuntimeException(String.format("Sequence Insert/Update failure: %s", updateResult));
        }
    }

    @Given("^delete records from MongoDB collection \"(.*?)\" which match the conditions$")
    public void deleteCollectionRecordsWithConditions$(String collection, DataTable dataTable) throws Exception {
        Class<?> collectionClass = getCollectionClass(collection);
        Query findQuery = getFindQuery(getMap(dataTable), collectionClass);
        mongoTemplate.remove(findQuery, collectionClass);
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

    private Class<?> getCollectionClass(String collection) {
        Class<?> collectionClass = collectionClassMap.get(collection);

        if(collectionClass == null) {
            throw new IllegalArgumentException(String.format("MongoDB collection class %s does not exist in domain packages %s", collection, MONGO_DOMAIN_PACKAGES));
        }
        return collectionClass;
    }

    private void addBasicDBObject(Map<String, String> map, org.bson.Document document, Class<?> collectionClass) {

        for (Map.Entry<String, String> entry : map.entrySet()) {

            String key = entry.getKey();
            Object value = getObject(collectionClass, entry.getValue(), key);

            if(key.contains(".")) {
                String[] tokens = key.split("\\.");
                setNestedFields(document, tokens, 0, value);
            } else {
                document.put(key, value);
            }
        }
    }

    private void setNestedFields(Map<String, Object> dbEntryMap, String[] tokens, int currentToken, Object value) {

        if(currentToken >= tokens.length) {
            return;
        }

        String token = tokens[currentToken];
        BasicDBObject nestedDocument;

        if(!dbEntryMap.containsKey(token)) {
            nestedDocument = new BasicDBObject();
            dbEntryMap.put(token, nestedDocument);
        } else {
            nestedDocument = (BasicDBObject) dbEntryMap.get(token);
        }

        currentToken++;

        if(currentToken == tokens.length-1) {
            nestedDocument.put(tokens[currentToken], value);
            return;
        }

        setNestedFields(nestedDocument, tokens, currentToken, value);
    }

    private Query getFindQuery(Map<String, String> matchConditions, Class<?> collectionClass) {

        Query query = new Query();

        if(matchConditions == null) {
            return query;
        }

        for (Map.Entry<String, String> entry : matchConditions.entrySet()) {

            if(!StringUtils.isBlank(entry.getKey())) {
                Object value = getObject(collectionClass, entry.getKey(), entry.getValue());
                query.addCriteria(Criteria.where(entry.getKey().trim()).is(value));
            }
        }

        return query;
    }

    private Object getObject(Class<?> collectionClass, String fieldName, String fieldValue) {
        Object value;
        try {
            Class<?> classType = collectionClass.getDeclaredField(fieldName).getType();
            value = toObject(classType, fieldValue);
        } catch (NoSuchFieldException e) {
            value = fieldValue;
        }
        return value;
    }

    public Object toObject(Class clazz, String value) {

        if("null".equals(value)){
            return null;
        }

        if( Date.class == clazz ) {
            try {
                return DEFAULT_DATE_FORMATTER.parse(value);
            } catch (ParseException pex) { }
        }

        if( Boolean.class == clazz || boolean.class == clazz ) return Boolean.parseBoolean( value );
        if( Byte.class == clazz || byte.class == clazz ) return Byte.parseByte( value );
        if( Short.class == clazz || short.class == clazz ) return Short.parseShort( value );
        if( Integer.class == clazz || int.class == clazz ) return Integer.parseInt( value );
        if( Long.class == clazz || long.class == clazz ) return Long.parseLong( value );
        if( Float.class == clazz || float.class == clazz) return Float.parseFloat( value );
        if( Double.class == clazz || double.class == clazz) return Double.parseDouble( value );
        return value;
    }
}