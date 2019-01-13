package cukes.steps;

import cucumber.api.DataTable;
import cukes.helper.ContentTypeService;
import cukes.type.ContentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseStepDefinition {

    @Autowired
    protected ContentTypeService contentTypeService;

    public Map<String, String> getMap(DataTable dataTable) {
        Map<String, String> finalMap = new HashMap<>();
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> map : maps) {
            finalMap.putAll(map);
        }
        return finalMap;
    }

    private String getFileContent(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
        return new String(encoded, Charset.defaultCharset());
    }

    public String getFileContent(ContentType contentType, String filename) throws URISyntaxException, IOException {
        return getFileContent(getFile(contentType, filename));
    }

    public File getFile(ContentType contentType, String filename) throws URISyntaxException {

        String extension = "." + contentType.extension();

        if(filename != null && !filename.endsWith(extension)) {
            filename = filename + extension;
        }

        URL resource = this.getClass().getResource(filename);
        return new File(resource.toURI());
    }
}
