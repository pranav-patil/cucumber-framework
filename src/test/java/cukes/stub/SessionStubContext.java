package cukes.stub;

import com.library.model.User;
import com.library.service.SessionContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

@Component
@Profile("stub")
public class SessionStubContext implements SessionContext {

    public static final String DEFAULT_SECRET_KEY = "7DYIvrqV93YrLFnOGIfgRA==";
    private IvParameterSpec ivParameterSpec;
    private SecretKey secretKey;
    private User user;

    public SessionStubContext() {
        user = createDealerUser();
        this.secretKey = getSecretKey("AES", DEFAULT_SECRET_KEY);
        setInitialVector("DEFAULT_INITIAL_VECTOR_CHARS");
    }

    public static String generateSecurityKey(String algorithm, int numberOfBytes) throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(algorithm);
        generator.init(numberOfBytes);
        SecretKey secretKey = generator.generateKey();
        if (secretKey != null) {
            return Base64.encodeBase64String(secretKey.getEncoded());
        }
        return null;
    }

    public static SecretKey getSecretKey(String algorithm, String stringKey) {
        byte[] encodedKey = Base64.decodeBase64(stringKey);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, algorithm);
    }

    @Override
    public IvParameterSpec getIvParamSpec() {
        return ivParameterSpec;
    }

    @Override
    public SecretKey getSecurityKey() throws Exception {
        return secretKey;
    }

    @Override
    public User getLoggedInUser() {
        return user;
    }

    private User createDealerUser() {
        User mockUser = new User();
        mockUser.setFirstName("JOHN");
        mockUser.setLastName("ABRAMS");
        mockUser.setAddress("123 Some cool street");
        mockUser.setCountry("USA");
        mockUser.setCurrency("USD");
        mockUser.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
        mockUser.setEmailAddress("JohnAbrams@example.com");
        mockUser.setLocale(Locale.US);
        mockUser.setRoles(new ArrayList<>());
        return mockUser;
    }

    public void addUserRoles(String... userRoles) {
        if(userRoles != null && userRoles.length > 0) {
            user.setRoles(Arrays.asList(userRoles));
        }
    }

    public void setUserId(String userId) {
        user.setUserId(userId);
    }

    public void setFirstName(String firstName) {
        user.setFirstName(firstName);
    }

    public void setLastName(String lastName) {
        user.setLastName(lastName);
    }

    public void setInitialVector(String text) {
        ivParameterSpec = new IvParameterSpec(getBytes(text, 16));
    }

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    private byte[] getBytes(String string, int numberOfBytes) {
        if(numberOfBytes > string.length()) {
            string = string + StringUtils.repeat("x", numberOfBytes - string.length());
        }
        return Arrays.copyOfRange(string.getBytes(), 0, numberOfBytes);
    }
}
