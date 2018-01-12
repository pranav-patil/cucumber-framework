package cukes.config;


import cukes.stub.SessionStubContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class MvcFilter implements Filter {

    private static final String TEST_SESSION_ID = "TEST_SESSION";
    @Autowired
    SessionStubContext sessionContext;
    @Autowired
    private LogbackCapture logbackCapture;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        logbackCapture.startCapture();
        if(servletRequest instanceof HttpServletRequest) {
            final RequestAttributes requestAttributes = new ServletRequestAttributes((HttpServletRequest)servletRequest);
            requestAttributes.setAttribute("sessionid", TEST_SESSION_ID ,RequestAttributes.SCOPE_REQUEST);
            requestAttributes.setAttribute("user", sessionContext.getLoggedInUser(), RequestAttributes.SCOPE_SESSION);
            try {
                requestAttributes.setAttribute("SecretKey", sessionContext.getSecurityKey() ,RequestAttributes.SCOPE_SESSION);
            } catch (Exception ex) {
                throw new ServletException(ex);
            }
            requestAttributes.setAttribute("IvParamSpec", sessionContext.getIvParamSpec() ,RequestAttributes.SCOPE_SESSION);
            RequestContextHolder.setRequestAttributes(requestAttributes);
        }
        filterChain.doFilter(servletRequest, servletResponse);
        logbackCapture.stopCapture();
    }

    @Override
    public void destroy() {
    }
}
