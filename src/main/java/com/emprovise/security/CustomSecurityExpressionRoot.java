package com.emprovise.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.FilterInvocation;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CustomSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private static Logger LOGGER = LoggerFactory.getLogger(CustomSecurityExpressionRoot.class);

    private Object filterObject;
    private Object returnObject;
    private Object target;
    private Set<String> userRoles;
    /*
     * This is for emulating WebSecurityExpressionRoot
     */
    public HttpServletRequest request;

    public CustomSecurityExpressionRoot(Authentication a) {
        super(a);
    }

    public CustomSecurityExpressionRoot(Authentication a, FilterInvocation fi) {
        super(a);
        this.request = fi.getRequest();
    }

    /**
     * Checks given roles against the given regex expression
     *
     * @param regex
     *            to match agains roles
     * @return true if at least 1 authority matches the regex, otherwise false
     */
    public boolean hasRegexRole(String regex) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hasRegexRole: " + regex);
        }

        boolean found = false;

        Set<String> authorities = getCustomAuthoritySet();

        for (String authority : authorities) {

            if (authority.matches(regex)) {
                found = true;
                break;
            }

        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hasRegexRole returns " + found);
        }

        return found;
    }

    /**
     * Note: this does not return hierchacal roles like
     * org.springframework.security
     * .access.expression.SecurityExpressionRoot.getAuthoritySet()
     *
     * @return set of authorities
     */
    private Set<String> getCustomAuthoritySet() {

        if (userRoles == null) {
            userRoles = new HashSet<String>();
            Collection<? extends GrantedAuthority> userAuthorities = authentication.getAuthorities();

            userRoles = AuthorityUtils.authorityListToSet(userAuthorities);
        }

        return userRoles;
    }

    @Override
    public void setFilterObject(Object o) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object o) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public Object getThis() {
        return target;
    }

    void setThis(Object target) {
        this.target = target;
    }
}