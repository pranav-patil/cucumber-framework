package cukes.stub;


import com.library.dao.ERPServiceAdapter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static cukes.type.ContentType.getContentType;

@Component
@Profile("stub")
public class ERPServiceStubAdapter extends ERPServiceAdapter {

    @Autowired
    private ServiceStubHttpClient baseHttpClient;

    private static Logger logger = LoggerFactory.getLogger(ERPServiceStubAdapter.class);

    @Override
    public CloseableHttpResponse get(String serviceUrl, MediaType mediaType) throws IOException {
        return baseHttpClient.getStubResponse(HttpMethod.GET, serviceUrl, getContentType(mediaType),null);
    }

    @Override
    public CloseableHttpResponse post(String serviceUrl, StringEntity entity, MediaType mediaType) throws IOException {
        return baseHttpClient.getStubResponse(HttpMethod.POST, serviceUrl, getContentType(mediaType), entity);
    }

    @Override
    public CloseableHttpResponse put(String serviceUrl, StringEntity entity, MediaType mediaType) throws IOException {
        return baseHttpClient.getStubResponse(HttpMethod.PUT, serviceUrl, getContentType(mediaType), entity);
    }
}
