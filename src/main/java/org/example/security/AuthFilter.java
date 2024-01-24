package org.example.security;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@WebFilter("/*")
public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        boolean authenticated = defaultAuthentication(request, new String[]{"/login", "/new"});
        if(authenticated) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
        else {
            response.sendRedirect("/login");
        }
    }

    private boolean defaultAuthentication(HttpServletRequest request, String[] mappings) {
        for(String mapping : mappings) {
            if(request.getRequestURI().endsWith(mapping)) {
                return true;
            }
        }
        return !request.getMethod().equals("GET") || request.getSession().getAttribute("user") != null;
    }
}
