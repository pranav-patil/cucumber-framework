package com.library.security;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

//@WebFilter(filterName="siteFilter", urlPatterns="/*")
public class SiteFilter implements Filter{

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		SiteRequestWrapper requestWrapper = new SiteRequestWrapper((HttpServletRequest) request);
		filterChain.doFilter(requestWrapper, response);
	}

	@Override
	public void destroy() {
	}
}
