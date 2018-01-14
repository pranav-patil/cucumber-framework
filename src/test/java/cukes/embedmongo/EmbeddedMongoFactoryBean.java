package cukes.embedmongo;

import static java.util.Collections.singletonList;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongoImportExecutable;
import de.flapdoodle.embed.mongo.MongoImportProcess;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.config.IMongoImportConfig;
import de.flapdoodle.embed.mongo.config.MongoImportConfigBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@linkplain FactoryBean} for EmbedMongo that runs MongoDB as a managed
 * process and exposes preconfigured instance of {@link MongoClient}.
 *
 * This class is a wrapper for {@link EmbeddedMongoBuilder} from this package.
 */
public class EmbeddedMongoFactoryBean implements FactoryBean<MongoClient>, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedMongoFactoryBean.class);

    private final EmbeddedMongoBuilder builder = new EmbeddedMongoBuilder();
    private MongoClient mongoClient;
    private MongodExecutable mongodExecutable;
    private MongodProcess mongod;

    private boolean parallel = false;
    private boolean dropOnImport = true;
    private boolean upsertOnImport = true;
    private Map<String, String> importMap;

    private String defaultImportDatabase;
    private String defaultFilePath;

    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;

    public MongoClient getObject() throws IOException, InterruptedException {

        addProxySelector();
        mongodExecutable = builder.buildMongodExecutable();
        LOG.info("Starting embedded MongoDB instance");
        mongod = mongodExecutable.start();
        mongoClient = new MongoClient(builder.getBindIp(), builder.getPort());
        importDocuments();
        return mongoClient;
    }

    public void importDocuments() throws InterruptedException, IOException {
        List<MongoImportProcess> pendingMongoProcess = new ArrayList<>();

        if(!StringUtils.isBlank(defaultFilePath)) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(defaultFilePath);
            Resource defaultDirectory = null;

            for (Resource resource : resources) {
                if(resource.exists()) {
                    defaultDirectory = resource;
                    break;
                }
            }
            if(defaultDirectory != null) {
                defaultFilePath = defaultDirectory.getFile().getAbsolutePath();
            }
        }

        for(Map.Entry<String, String> entry : importMap.entrySet()) {

            String importFile = entry.getValue();

            LOG.info("Import " + importFile);
            verify(importFile);

            if(!StringUtils.isBlank(defaultFilePath)) {
                importFile = defaultFilePath + "/" + importFile;
            }

            MongoImportExecutable mongoImport = builder.buildMongoImportExecutable(defaultImportDatabase, entry.getKey(), importFile, true,
                                                                                    upsertOnImport, dropOnImport);
            MongoImportProcess importProcess = mongoImport.start();

            if(parallel){
                pendingMongoProcess.add(importProcess);
            }else{
                waitFor(importProcess);
            }
        }

        for(MongoImportProcess importProcess: pendingMongoProcess){
            waitFor(importProcess);
        }
    }

    private void waitFor(MongoImportProcess importProcess) throws InterruptedException {
        int code = importProcess.waitFor();

        if(code != 0){
            throw new RuntimeException("Cannot import '" + importProcess.getConfig().getImportFile() + "'");
        }

        LOG.info("Import return code: " + code);
    }

    public Class<MongoClient> getObjectType() {
        return MongoClient.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() {
        if (mongoClient != null) {
            LOG.info("Stopping embedded MongoDB instance");
            mongoClient.close();
        }

        if (mongod != null) {
            mongod.stop();
        }
        if (mongodExecutable != null) {
            mongodExecutable.stop();
        }
    }

    /**
     * The version of MongoDB to run e.g. 2.1.1, 1.6 v1.8.2, V2_0_4. When no
     * version is provided, then {@link de.flapdoodle.embed.mongo.distribution.Version.Main#PRODUCTION PRODUCTION}
     * is used by default. The value must not be empty.
     */
    public void setVersion(String version) {
        builder.version(version);
    }

    /**
     * The port MongoDB should run on. When no port is provided, then some free
     * server port is automatically assigned. The value must be between 0 and 65535.
     */
    public void setPort(int port) {
        builder.port(port);
    }

    /**
     * An IPv4 or IPv6 address for the MongoDB instance to be bound to during
     * its execution. Default is a {@linkplain java.net.InetAddress#getLoopbackAddress()
     * loopback address}. The value must not be empty.
     */
    public void setBindIp(String bindIp) {
        builder.bindIp(bindIp);
    }

    private void verify(String file) {
        Validate.notBlank(file, "Import file is required");
        Validate.isTrue(StringUtils.isNotBlank(defaultImportDatabase), "Database is required you can either define a defaultImportDatabase or a database on import tags");
    }

    private void addProxySelector() {

        // Add authenticator with proxyUser and proxyPassword
        if (proxyUser != null && proxyPassword != null) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                }
            });
        }

        final ProxySelector defaultProxySelector = ProxySelector.getDefault();
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(final URI uri) {
                if (!StringUtils.isBlank(proxyHost) && uri.getHost().equals("downloads.mongodb.org")) {
                    return singletonList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
                } else {
                    return defaultProxySelector.select(uri);
                }
            }

            @Override
            public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
            }
        });
    }

    public String getDefaultFilePath() {
        return defaultFilePath;
    }

    public void setDefaultFilePath(String defaultFilePath) {
        this.defaultFilePath = defaultFilePath;
    }

    public String getDefaultImportDatabase() {
        return defaultImportDatabase;
    }

    public void setDefaultImportDatabase(String defaultImportDatabase) {
        this.defaultImportDatabase = defaultImportDatabase;
    }

    public boolean isDropOnImport() {
        return dropOnImport;
    }

    public void setDropOnImport(boolean dropOnImport) {
        this.dropOnImport = dropOnImport;
    }

    public boolean isUpsertOnImport() {
        return upsertOnImport;
    }

    public void setUpsertOnImport(boolean upsertOnImport) {
        this.upsertOnImport = upsertOnImport;
    }

    public boolean isParallel() {
        return parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    public Map<String, String> getImportMap() {
        return importMap;
    }

    public void setImportMap(Map<String, String> importMap) {
        this.importMap = importMap;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }
}