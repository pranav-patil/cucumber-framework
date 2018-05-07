package cukes.steps;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cukes.helper.FieldTypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class SQLSteps extends BaseStepDefinition {

    @Autowired
    private DataSource sqlDataSource;
    @Autowired
    private FieldTypeService fieldTypeService;
    @Resource(name = "sqldbPackages")
    private List<String> sqldbPackages;
    private JdbcTemplate sqlJdbcTemplate;

    private Map<String, Class<?>> tableClassMap = new HashMap<>();

    @PostConstruct
    public void init() throws Exception {
        this.sqlJdbcTemplate = new JdbcTemplate(sqlDataSource);
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
        scanner.addIncludeFilter(new AnnotationTypeFilter(javax.persistence.Table.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(javax.persistence.Entity.class));

        for (String domainPackage : sqldbPackages) {
            Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(domainPackage);

            for (BeanDefinition beanDef : beanDefinitions) {

                AnnotationMetadata metadata = ((AnnotatedBeanDefinition) beanDef).getMetadata();
                Map<String, Object> tableAnnotationAttributes = metadata.getAnnotationAttributes(Table.class.getName());
                Map<String, Object> entityAnnotationAttributes = metadata.getAnnotationAttributes(Entity.class.getName());

                Class<?> clazz = Class.forName(beanDef.getBeanClassName());
                String tableName = clazz.getSimpleName();
                tableName = Character.toLowerCase(tableName.charAt(0)) + tableName.substring(1);

                if (tableAnnotationAttributes != null && tableAnnotationAttributes.containsKey("name")) {
                    String table = (String) tableAnnotationAttributes.get("name");
                    if (!StringUtils.isBlank(table)) {
                        tableName = table;
                    }
                } else if (entityAnnotationAttributes != null && entityAnnotationAttributes.containsKey("name")) {
                    String table = (String) entityAnnotationAttributes.get("name");
                    if (!StringUtils.isBlank(table)) {
                        tableName = table;
                    }
                }

                tableClassMap.put(tableName, clazz);
            }
        }
    }

    @Given("^SQL table \"(.*?)\" has no records$")
    public void tableHasNoRecords(String table) {
        sqlJdbcTemplate.update(String.format("DELETE FROM %s", table));
    }

    @Given("^SQL table \"(.*?)\" has data$")
    public void tableHasData(String table, DataTable dataTable) {

        Class<?> tableClass = getTableClass(table);
        List<Map<String, String>> dataTableMaps = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> dataMap : dataTableMaps) {
            insert(table, fieldTypeService.convertToObjectMap(tableClass, dataMap));
        }
    }

    @Given("^SQL sequence \"(.*?)\" has counter (\\d+)$")
    public void setSequenceCounter(String sequenceName, Long startCounter) {
    }

    @Given("^delete records from SQL table \"(.*?)\" which match the conditions$")
    public void deleteTableRecordsWithConditions$(String table, DataTable dataTable) {

        Class<?> tableClass = getTableClass(table);
        Map<String, Object> paramObjectMap = fieldTypeService.convertToObjectMap(tableClass, getMap(dataTable));
        Collection<Object> values = paramObjectMap.values();

        String parameters = paramObjectMap.keySet().stream()
                                                   .map(e -> e + " = ?")
                                                   .collect(Collectors.joining(" AND "));
        String sqlQuery = String.format("DELETE FROM %s WHERE %s", table, parameters);
        int deletedRows = sqlJdbcTemplate.update(sqlQuery, values.toArray(new Object[values.size()]));
        System.out.println(String.format("Deleted %d rows from %s", deletedRows, table));
    }

    @And("^Drop table \"(.*?)\"")
    public void dropTable(String table) throws Exception {
        sqlJdbcTemplate.update(String.format("DROP TABLE %s", table));
    }

    @Then("^Verify that SQL table \"(.*?)\" has (\\d+) records which match the conditions$")
    public void verifyTableHasRecordsWithConditions$(String table, int numberOfRecords, DataTable dataTable) {
        Class<?> tableClass = getTableClass(table);
        Map<String, String> dataMap = getMap(dataTable);
        Map<String, Object> paramObjectMap = fieldTypeService.convertToObjectMap(tableClass, dataMap);
        Collection<Object> values = paramObjectMap.values();

        String parameters = paramObjectMap.keySet().stream()
                                                   .map(e -> e + " = ?")
                                                   .collect(Collectors.joining(" AND "));
        String sqlQuery = String.format("SELECT count(*) FROM %s WHERE %s", table, parameters);

        int count = sqlJdbcTemplate.queryForObject(sqlQuery, values.toArray(new Object[values.size()]), Integer.class);
        assertEquals(numberOfRecords, count);
    }

    @Then("^Verify that (\\d+) records are fetched by SQL query")
    public void verifyRecordsFetchedBySQLQuery$(int numberOfRecords, String selectQuery) {
        List<Map<String, Object>> mapList = sqlJdbcTemplate.queryForList(selectQuery);
        assertEquals(numberOfRecords, mapList.size());
    }

    public void insert(String table, final Map<String, Object> map) {
        String columns = String.join(",", map.keySet());
        String arguments = StringUtils.repeat("?", ", ", map.size());
        String query = String.format("INSERT INTO %s (%s) VALUES (%s)", table, columns, arguments);
        Collection<Object> values = map.values();
        sqlJdbcTemplate.update(query, values.toArray(new Object[values.size()]));
    }

    private Class<?> getTableClass(String table) {
        Class<?> tableClass = tableClassMap.get(table);

        if(tableClass == null) {
            throw new IllegalArgumentException(String.format("SQL table/entity class %s does not exist in domain packages %s", table, sqldbPackages));
        }
        return tableClass;
    }
}
