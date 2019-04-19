package cukes.stub;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import cukes.type.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Component
public class WireMockService {

    private WireMockServer wireMockServer;

    public WireMockService() {
        this.wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8080).httpsPort(8443));
        this.wireMockServer.start();
    }

    public void setResponseData(HttpMethod httpMethod, String url, ContentType contentType, HttpStatus httpStatus, String responseString) {
        setResponseData(httpMethod, url, contentType, httpStatus, responseString, null);
    }

    public void setResponseData(HttpMethod httpMethod, String url, ContentType contentType, HttpStatus httpStatus, File responseFile, Map<String, String> requestConditions) throws IOException {
        setResponseData(httpMethod, url, contentType, httpStatus, getFileContent(responseFile), requestConditions);
    }

    public void setResponseData(HttpMethod httpMethod, String url, ContentType contentType, HttpStatus httpStatus, File responseFile) throws IOException {
        setResponseData(httpMethod, url, contentType, httpStatus, responseFile, null);
    }

    public void setResponseData(HttpMethod httpMethod, String url, ContentType contentType, HttpStatus httpStatus, String responseString, Map<String, String> requestConditions) {

        MappingBuilder mappingBuilder = request(httpMethod.name(), urlEqualTo(url));

        if(httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT) {
            mappingBuilder = mappingBuilder.withHeader("Content-Type", equalTo(contentType.mediaType().toString()));
        }

        if(requestConditions != null) {
            for (Map.Entry<String, String> entry : requestConditions.entrySet()) {

                String pathKey = entry.getKey();

                if(StringUtils.isNotBlank(pathKey)) {

                    if(ContentType.JSON == contentType) {
                        pathKey = pathKey.trim();
                        pathKey = (!pathKey.startsWith("$.")) ? "$." + pathKey : pathKey;
                        mappingBuilder.withRequestBody(matchingJsonPath(pathKey, equalTo(entry.getValue())));
                    } else if(ContentType.XML == contentType) {

                        String xpathExpression = pathKey.trim();

                        if(!xpathExpression.contains("/")) {
                            if(xpathExpression.contains("(") && xpathExpression.contains(")")) {
                                xpathExpression = xpathExpression.replace("(", "(//");
                            }
                            else {
                                xpathExpression = String.format("//%s/text()", xpathExpression);
                            }
                            xpathExpression = xpathExpression.replace(".", "/");
                        }
                        mappingBuilder.withRequestBody(matchingXPath(xpathExpression, equalTo(entry.getValue())));
                    }
                }
            }
        }

        wireMockServer.stubFor(mappingBuilder.willReturn(aResponse()
                                                .withStatus(httpStatus.value())
                                                .withHeader("Content-Type", contentType.mediaType().toString())
                                                .withBody(responseString)));
    }

    private String getFileContent(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
        return new String(encoded, Charset.defaultCharset());
    }

    public void clearStubs() {
        wireMockServer.resetAll();
    }
}
