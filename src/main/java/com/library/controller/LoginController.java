package com.library.controller;

import com.library.response.MessageCode;
import com.library.response.MessageSeverity;
import com.library.response.ResponseMessage;
import org.springframework.security.core.userdetails.User;
import com.library.response.ServiceResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/login")
public class LoginController {

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse login(@PathVariable( "userId" ) final String userId) throws Exception {

        com.library.model.User user = new com.library.model.User();
        user.setUserId(userId);
        RequestContextHolder.currentRequestAttributes().setAttribute("user", user, RequestAttributes.SCOPE_SESSION);
        addAuthorities(userId, Collections.singletonList("ADMIN"));

        ServiceResponse serviceResponse = new ServiceResponse();
        serviceResponse.addMessage(getResponseMessage(MessageCode.SUCCESS, MessageSeverity.SUCCESS));
        return serviceResponse;
    }

    public void addAuthorities(String userName , List<String> businessActivities) {
        List<GrantedAuthority> listofAuthorities = new ArrayList<>();

        if(businessActivities != null ){
            for(String businessActivity : businessActivities){
                listofAuthorities.add(new SimpleGrantedAuthority(businessActivity));
            }
        }

        UserDetails userDetails = new User(userName, "N/A", true, true, true, true, listofAuthorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private ResponseMessage getResponseMessage(MessageCode messageCode, MessageSeverity messageSeverity) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessageCode(messageCode);
        responseMessage.setMessage(messageCode.value());
        responseMessage.setSeverity(messageSeverity);
        return responseMessage;
    }
}
