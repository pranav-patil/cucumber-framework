package cukes.sync;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class JsonSyncRunner {

    private ObjectMapper objectMapper;
    private boolean fileSyncEnabled;

    // JSON SYNC RUNNER SETTINGS FILE NAME
    private static final String JSON_SYNC_CONFIG_FILE = "config/json_sync_config.json";
    private static String[] FIELDS_TO_IGNORE = new String[]{"_class"};
    private static final Gson GSON = new Gson();
    Logger logger = LoggerFactory.getLogger(JsonSyncRunner.class);

    public JsonSyncRunner() {
        this.objectMapper = getObjectMapper();
        this.fileSyncEnabled = Boolean.valueOf(System.getProperty("fileSyncEnabled", "false"));
    }

    private void scanAndSyncJson() throws IOException, URISyntaxException {
        Path resourcePath = Paths.get(getClass().getResource("/cukes").toURI());
        File resourceDir = Paths.get(resourcePath.toString().replace("\\target\\test-classes\\", "\\src\\test\\resources\\")).toFile();
        JsonClassConfig[] jsonClassConfigs = loadJson(resourceDir);
        Set<File> filesProcessed = new LinkedHashSet<>();

        if(jsonClassConfigs != null){
            for (JsonClassConfig jsonClassConfig : jsonClassConfigs) {

                Class aClass = getClass(jsonClassConfig.getClassName());
                if(aClass != null) {
                    jsonClassConfig.getJsonFiles()
                            .forEach(jsonFilePath -> syncJsonFile(filesProcessed, new File(resourceDir, jsonFilePath), aClass));
                    jsonClassConfig.getJsonDirectories()
                            .forEach(jsonDirPath -> syncJsonFilesInDirectory(filesProcessed, new File(resourceDir, jsonDirPath), aClass));
                }
            }
        }
    }

    private void syncJsonFile(Set<File> filesProcessed, File jsonFile, Class aClass) {
        if(!jsonFile.exists() && !"json".equalsIgnoreCase(FilenameUtils.getExtension(jsonFile.getName()))) {
            jsonFile = Paths.get(Paths.get(jsonFile.toURI()).toString() + ".json").toFile();
        }

        if(!jsonFile.exists() && !filesProcessed.isEmpty()) {
            jsonFile = new File(filesProcessed.iterator().next().getParent(), jsonFile.getName());
        }

        if(jsonFile.exists()) {
            syncJson(aClass, jsonFile);
            filesProcessed.add(jsonFile);
        } else {
            System.err.println("FILE NOT FOUND: Skipping file " + jsonFile);
        }
    }

    private void syncJsonFilesInDirectory(Set<File> filesProcessed, File directory, Class aClass) {
        Collection<File> jsonFiles = FileUtils.listFiles(directory, new WildcardFileFilter("*.json"), FalseFileFilter.INSTANCE);
        jsonFiles.stream()
                .filter(jsonFile -> !filesProcessed.contains(jsonFile))
                .forEach(jsonFile -> syncJson(aClass, jsonFile));
    }

    private PrettyPrinter getCustomPrettyPrinter() {
        DefaultPrettyPrinter prettyPrinter = new CustomPrettyPrinter();
        DefaultIndenter defaultIndenter = new DefaultIndenter("  ", DefaultIndenter.SYS_LF);
        prettyPrinter.indentObjectsWith(defaultIndenter);
        prettyPrinter.indentArraysWith(defaultIndenter);
        prettyPrinter.withoutSpacesInObjectEntries();
        return prettyPrinter;
    }

    private void syncJson(Class<?> aClass, File jsonFile)  {
        try {
            String jsonString = FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
            syncJson(jsonFile, jsonString, aClass);
        }catch (IOException ex) {
            throw new RuntimeException(String.format("Sync of Json file %s with Class %s failed.", jsonFile, aClass.getCanonicalName()), ex);
        }
    }

    public void syncJson(File jsonFile)  {

        String classname = "UNKNOWN";
        try {
            jsonFile = Paths.get(jsonFile.getAbsolutePath().replace("\\target\\test-classes\\", "\\src\\test\\resources\\")).toFile();
            String jsonString = FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
            syncJson(jsonFile, jsonString, getJsonDomainClass(jsonString));

        }catch (IOException | ClassNotFoundException ex) {
            logger.error(String.format("Sync of Json file %s with Class %s failed.", jsonFile, classname), ex);
        }
    }

    private Class<?> getJsonDomainClass(String jsonString) throws ClassNotFoundException {

        JsonObject jsonObject = GSON.fromJson(jsonString, JsonObject.class);
        JsonElement classElement = jsonObject.get("_class");

        if(classElement != null) {
            return Class.forName(classElement.getAsString());
        }

        return null;
    }

    private void syncJson(File jsonFile, String jsonString, Class<?> aClass) throws IOException {
        Object object = objectMapper.readValue(jsonString, aClass);

        if (StringUtils.isNotBlank(jsonString)) {
            SimpleBeanPropertyFilter beanPropertyFilter = SimpleBeanPropertyFilter.serializeAllExcept(FIELDS_TO_IGNORE);
            objectMapper.addMixIn(Object.class, DynamicFilterMixIn.class);
            objectMapper.setFilterProvider(new DynamicFilterProvider(beanPropertyFilter));
            String json;

            if(fileSyncEnabled) {
                objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
                ObjectNode jsonNode = objectMapper.convertValue(object, ObjectNode.class);
                jsonNode = jsonNode.put("_class", aClass.getCanonicalName());
                json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            } else {
                json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            }

            FileUtils.writeStringToFile(jsonFile, json, Charset.defaultCharset(), false);
            System.out.println(String.format("Updated Json file %s with class %s.", jsonFile.getAbsolutePath(), aClass.getCanonicalName()));
        }
    }

    private ObjectMapper getObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.setDefaultPrettyPrinter(getCustomPrettyPrinter());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Currently Disabled Sorting Fields by Alphabetical Order
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);

        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new CustomDeserializerModifier());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    private JsonClassConfig[] loadJson(File resourceDir) throws IOException {
        File configFile = new File(resourceDir.getParent(), JSON_SYNC_CONFIG_FILE);
        String configString = FileUtils.readFileToString(configFile, Charset.defaultCharset());
        configString = configString.replace('\\', '/');
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(configString, JsonClassConfig[].class);
    }

    private Class<?> getClass(String className) {
        try {
            if(StringUtils.isNotBlank(className)) {
                return Class.forName(className);
            }
        } catch (ClassNotFoundException e) {
            System.err.println(String.format("Classname %s not found. Skipping sync for all corresponding json files.", className));
        }
        return null;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        JsonSyncRunner steps = new JsonSyncRunner();
        steps.scanAndSyncJson();
    }
}
