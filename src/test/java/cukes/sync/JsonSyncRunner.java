package cukes.sync;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;

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
    private File resourceDir;

    // JSON SYNC RUNNER SETTINGS FILE NAME
    private static final String JSON_SYNC_CONFIG_FILE = "config/json_sync_config.json";
    private static String[] FIELDS_TO_IGNORE = new String[]{"_class"};

    public JsonSyncRunner() throws URISyntaxException {
        this.objectMapper = getObjectMapper();
        Path resourcePath = Paths.get(getClass().getResource("/cukes").toURI());
        this.resourceDir = Paths.get(resourcePath.toString().replace("\\target\\test-classes\\", "\\src\\test\\resources\\")).toFile();
    }

    private void scanAndSyncJson() throws IOException {
        JsonClassConfig[] jsonClassConfigs = loadJson();
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
            Object object = objectMapper.readValue(jsonString, aClass);

            if(StringUtils.isNotBlank(jsonString)) {
                SimpleBeanPropertyFilter beanPropertyFilter = SimpleBeanPropertyFilter.serializeAllExcept(FIELDS_TO_IGNORE);
                objectMapper.addMixIn(Object.class, DynamicFilterMixIn.class);
                objectMapper.setFilterProvider(new DynamicFilterProvider(beanPropertyFilter));

                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
                FileUtils.writeStringToFile(jsonFile, json, Charset.defaultCharset(), false);
                System.out.println(String.format("Updated Json file %s with class %s.", jsonFile.getAbsolutePath(), aClass.getCanonicalName()));
            }
        }catch (IOException ex) {
            throw new RuntimeException(String.format("Sync of Json file %s with Class %s failed.", jsonFile, aClass.getCanonicalName()), ex);
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

    private JsonClassConfig[] loadJson() throws IOException {
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

    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, URISyntaxException {
        JsonSyncRunner steps = new JsonSyncRunner();
        steps.scanAndSyncJson();
    }
}
