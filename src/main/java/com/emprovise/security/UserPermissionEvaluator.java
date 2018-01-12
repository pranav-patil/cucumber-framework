package com.emprovise.security;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


public class UserPermissionEvaluator implements PermissionEvaluator{

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,  String targetType, Object role) {
    	return false;
    }

	@Override
	public boolean hasPermission(Authentication authentication, Object arg1, Object role) {
		UserDetails principal = (UserDetails) authentication.getPrincipal();
        Collection<GrantedAuthority> listofAuthorities = (Collection<GrantedAuthority>) principal.getAuthorities();
        
        for(GrantedAuthority simpleGrantedAuthority : listofAuthorities){
            String authorisedRole = simpleGrantedAuthority.getAuthority();
            if(authorisedRole.equalsIgnoreCase(role.toString())){
                return true;
            }
        }
        return false;
	}
	
	
}
