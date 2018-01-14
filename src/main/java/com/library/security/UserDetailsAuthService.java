package com.library.security;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserDetailsAuthService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken>, UserDetailsService{
	
	@Override
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
		String userName = (String)token.getPrincipal();
		UserDetails userDetails = loadUserByUsername(userName);
		return userDetails;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDetails userDetails;
		List<String> listOfBusinessActivities = new ArrayList<>();
		listOfBusinessActivities.add("ROLE_ADMIN");
		listOfBusinessActivities.add("ROLE_USER");

		List<GrantedAuthority> authorities = new ArrayList<>();
		for(String businessActivity : listOfBusinessActivities){
			authorities.add(new SimpleGrantedAuthority(businessActivity));
		}
		userDetails = new User(username, "N/A", true, true, true, true, authorities);
		return userDetails;
	}
}
