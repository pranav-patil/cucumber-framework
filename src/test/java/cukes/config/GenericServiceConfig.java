package cukes.config;

import cukes.dto.GenericServiceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class GenericServiceConfig {

    @Value("${ERP_SERVICE_SCHEME}")
    private String erpScheme;
    @Value("${ERP_SERVICE_HOST}")
    private String erpHost;
    @Value("${ERP_SERVICE_PORT}")
    private int erpPort;

    public static final String ERP_SERVICE = "ERP";

    @Bean("genericServiceMap")
    public Map<String, GenericServiceType> genericServiceMap() {
        Map<String, GenericServiceType> genericServiceMap = new HashMap<>();
        genericServiceMap.put(ERP_SERVICE, erpService());
        return genericServiceMap;
    }

    private GenericServiceType erpService() {
        GenericServiceType erpService = new GenericServiceType();
        erpService.setScheme(erpScheme);
        erpService.setHost(erpHost);
        erpService.setPort(erpPort);
        erpService.setUrlPattern("^/internal/erp/(.*)$");
        erpService.setResponseFilePath("/cukes/service-stub-response/");
        return erpService;
    }
}
