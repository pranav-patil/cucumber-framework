package com.library.security;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


public class UserPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,  String targetType, Object role) {
        if ((authentication == null) || (targetType == null) || !(role instanceof String)) {
            return false;
        }
        return hasPrivilege(authentication, targetType.toUpperCase(), role.toString().toUpperCase());
    }

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object role) {
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

    private boolean hasPrivilege(Authentication auth, String targetType, String role) {
        for (GrantedAuthority grantedAuth : auth.getAuthorities()) {
            if (grantedAuth.getAuthority().startsWith(targetType)) {
                if (grantedAuth.getAuthority().contains(role)) {
                    return true;
                }
            }
        }
        return false;
    }
}
