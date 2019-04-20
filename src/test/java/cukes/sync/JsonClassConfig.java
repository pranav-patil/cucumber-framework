package cukes.sync;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonClassConfig {

    private String className;
    private List<String> jsonFiles = new ArrayList<>();
    private List<String> jsonDirectories = new ArrayList<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getJsonFiles() {
        return jsonFiles;
    }

    public void setJsonFiles(List<String> jsonFiles) {
        this.jsonFiles = jsonFiles;
    }

    public List<String> getJsonDirectories() {
        return jsonDirectories;
    }

    public void setJsonDirectories(List<String> jsonDirectories) {
        this.jsonDirectories = jsonDirectories;
    }
}
