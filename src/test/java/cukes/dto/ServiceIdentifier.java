package cukes.dto;

import cukes.type.ContentType;
import org.springframework.http.HttpMethod;

import java.util.Objects;

public class ServiceIdentifier {

    private HttpMethod httpMethod;
    private String serviceUrl;
    private ContentType contentType;

    public ServiceIdentifier(HttpMethod httpMethod, String serviceUrl, ContentType contentType) {
        this.httpMethod = httpMethod;
        this.serviceUrl = serviceUrl;
        this.contentType = contentType;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean equals(Object object) {

        if (object == this) {
            return true;
        }

        if (!(object instanceof ServiceIdentifier)) {
            return false;
        }

        ServiceIdentifier identifier = (ServiceIdentifier) object;
        return Objects.equals(identifier.getHttpMethod(), this.httpMethod) &&
                Objects.equals(identifier.getServiceUrl(), this.serviceUrl) &&
                Objects.equals(identifier.getContentType(), this.contentType);
    }

    @Override
    public int hashCode() {
        int result = 17;

        if (httpMethod != null) {
            result = 31 * result + httpMethod.hashCode();
        }

        if (serviceUrl != null) {
            result = 31 * result + serviceUrl.hashCode();
        }

        if (contentType != null) {
            result = 31 * result + contentType.hashCode();
        }

        return result;
    }
}

