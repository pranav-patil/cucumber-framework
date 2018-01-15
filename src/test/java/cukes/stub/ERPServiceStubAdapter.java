package cukes.stub;


import com.library.dao.ERPServiceAdapter;
import cukes.type.ContentType;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("stub")
public class ERPServiceStubAdapter extends ERPServiceAdapter {

    @Autowired
    private ServiceStubHttpClient baseHttpClient;

    private static Logger logger = LoggerFactory.getLogger(ERPServiceStubAdapter.class);

    @Override
    public CloseableHttpResponse get(String serviceUrl) throws IOException {
        return baseHttpClient.getStubResponse(HttpMethod.GET, serviceUrl, ContentType.JSON,null);
    }

    @Override
    public CloseableHttpResponse post(StringEntity entity, String serviceUrl) throws IOException {
        return baseHttpClient.getStubResponse(HttpMethod.POST, serviceUrl, ContentType.JSON, entity);
    }

    @Override
    public CloseableHttpResponse put(StringEntity entity, String serviceUrl) throws IOException {
        return baseHttpClient.getStubResponse(HttpMethod.PUT, serviceUrl, ContentType.JSON, entity);
    }
}
