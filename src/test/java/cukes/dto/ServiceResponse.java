package cukes.dto;

import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.Map;

public class ServiceResponse {

    private HttpStatus httpStatus;
    private String responseString;
    private File responseFile;
    private Map<String, String> requestConditions;

    public ServiceResponse(HttpStatus httpStatus, String responseString) {
        this.httpStatus = httpStatus;
        this.responseString = responseString;
    }

    public ServiceResponse(HttpStatus httpStatus, File responseFile) {
        this.httpStatus = httpStatus;
        this.responseFile = responseFile;
    }

    public ServiceResponse(HttpStatus httpStatus, String responseString, Map<String, String> requestConditions) {
        this.httpStatus = httpStatus;
        this.responseString = responseString;
        this.requestConditions = requestConditions;
    }

    public ServiceResponse(HttpStatus httpStatus, File responseFile, Map<String, String> requestConditions) {
        this.httpStatus = httpStatus;
        this.responseFile = responseFile;
        this.requestConditions = requestConditions;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getResponseString() {
        return responseString;
    }

    public void setResponseString(String responseString) {
        this.responseString = responseString;
    }

    public File getResponseFile() {
        return responseFile;
    }

    public void setResponseFile(File responseFile) {
        this.responseFile = responseFile;
    }

    public Map<String, String> getRequestConditions() {
        return requestConditions;
    }

    public void setRequestConditions(Map<String, String> requestConditions) {
        this.requestConditions = requestConditions;
    }
}
