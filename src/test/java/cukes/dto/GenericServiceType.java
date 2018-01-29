package cukes.dto;

import java.util.regex.Pattern;

public class GenericServiceType {

    private String scheme;
    private String host;
    private int port;
    private Pattern urlPattern;
    private String responseFilePath;

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Pattern getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String regexPattern) {
        this.urlPattern =  Pattern.compile(regexPattern);
    }

    public String getResponseFilePath() {
        return responseFilePath;
    }

    public void setResponseFilePath(String responseFilePath) {
        this.responseFilePath = responseFilePath;
    }
}
