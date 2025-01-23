package com.perfect.bcs.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @authoror 梁波
 * @since 2016-07-19
 */
@Component
public class CrossDomainInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CrossDomainInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        response.addHeader("Access-Control-Allow-Origin", "*");
        response.setHeader(
                "Access-Control-Allow-Headers",
                "Last-Modified, Origin, X-Requested-With, Content-Type, Accept, token, deviceId");
        response.setHeader("Access-Control-Allow-Methods", "GET, PUT, DELETE, POST, OPTIONS");

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {

    }

}
