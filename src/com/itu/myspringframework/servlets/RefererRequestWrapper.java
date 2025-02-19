package com.itu.myspringframework.servlets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class RefererRequestWrapper extends HttpServletRequestWrapper {
    private String newReferer;

    public RefererRequestWrapper(HttpServletRequest request, String newReferer) {
        super(request);
        this.newReferer = newReferer;
    }

    @Override
    public String getHeader(String name) {
        if ("Referer".equalsIgnoreCase(name)) {
            return newReferer;
        }
        return super.getHeader(name);
    }
}
