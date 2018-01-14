package cukes.steps;

import cucumber.api.java.en.Given;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;

public class LoginSteps extends BaseStepDefinition {

    @Given("^User \"(.*?)\" login with id \"(.*?)\" and businessActivity \"(.*?)\"$")
    public void login(String userName, String userId, String businessActivity) {

        setUserDetails(userId, userName);
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = Collections.singletonList(new SimpleGrantedAuthority(businessActivity));
        UserDetails userDetails = new User(userName, "N/A", true, true, true, true, simpleGrantedAuthorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Given("^User \"(.*?)\" logout$")
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    private void setUserDetails(String userId, String userName) {

        if(userName != null) {
            userName = userName.trim();
            int endIndex = userName.lastIndexOf(" ");

            if(endIndex > 0) {
                sessionUtility.setFirstName(userName.substring(0, endIndex));
                sessionUtility.setLastName(userName.substring(endIndex + 1));
            } else {
                sessionUtility.setFirstName(userName);
                sessionUtility.setLastName("");
            }
        }

        sessionUtility.setUserId(userId);
    }
}
