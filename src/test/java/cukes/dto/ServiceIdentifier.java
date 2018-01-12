package cukes.dto;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpMethod;

public class ServiceIdentifier {

    private HttpMethod httpMethod;
    private String serviceUrl;

    public ServiceIdentifier(HttpMethod httpMethod, String serviceUrl) {
        this.httpMethod = httpMethod;
        this.serviceUrl = serviceUrl;
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

    @Override
    public boolean equals(Object object) {

        if (object == this) {
            return true;
        }

        if (!(object instanceof ServiceIdentifier)) {
            return false;
        }

        ServiceIdentifier identifier = (ServiceIdentifier) object;
        return ObjectUtils.equals(identifier.getHttpMethod(), this.httpMethod) &&
                ObjectUtils.equals(identifier.getServiceUrl(), this.serviceUrl);
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
        return result;
    }
}

