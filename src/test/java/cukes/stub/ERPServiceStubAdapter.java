package cukes.stub;


import com.library.dao.ERPServiceAdapter;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicStatusLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static cukes.type.ContentType.getContentType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Component
@Profile("stub")
public class ERPServiceStubAdapter extends ERPServiceAdapter {

    @Autowired
    private GenericStubService genericStubService;

    @Override
    public CloseableHttpResponse get(String serviceUrl, MediaType mediaType) throws IOException {
        String stubResponse = genericStubService.getStubResponse(HttpMethod.GET, serviceUrl, getContentType(mediaType), null);
        return createMockCloseableHttpResponse(stubResponse);
    }

    @Override
    public CloseableHttpResponse post(String serviceUrl, StringEntity entity, MediaType mediaType) throws IOException {
        String stubResponse = genericStubService.getStubResponse(HttpMethod.POST, serviceUrl, getContentType(mediaType), getString(entity));
        return createMockCloseableHttpResponse(stubResponse);
    }

    @Override
    public CloseableHttpResponse put(String serviceUrl, StringEntity entity, MediaType mediaType) throws IOException {
        String stubResponse = genericStubService.getStubResponse(HttpMethod.PUT, serviceUrl, getContentType(mediaType), getString(entity));
        return createMockCloseableHttpResponse(stubResponse);
    }

    private CloseableHttpResponse createMockCloseableHttpResponse(String responseString) throws IOException {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);

        byte[] bytes = responseString.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(bytes);

        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.OK.value(), "OK"));
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(inputStream);
        when(entity.getContentLength()).thenReturn((long)bytes.length);

        return response;
    }

    private String getString(StringEntity stringEntity) throws IOException {
        String requestPayload = null;
        if(stringEntity != null) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(stringEntity.getContent(), writer, Charset.defaultCharset());
            requestPayload = writer.toString();
        }
        return requestPayload;
    }
}
