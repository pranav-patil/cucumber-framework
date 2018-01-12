package com.emprovise.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class SiteRequestWrapper extends HttpServletRequestWrapper {

    public SiteRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public String getHeader(String headerName) {
        if ("SM_USER".equalsIgnoreCase(headerName)) {
            return "USER01";
        }

        return super.getHeader(headerName);
    }
}
