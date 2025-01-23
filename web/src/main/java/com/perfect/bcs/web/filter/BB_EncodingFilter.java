package com.perfect.bcs.web.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * Filter的执行顺序由Java文件的名字升序排序
 * 
 * @author liangbo 梁波
 * @since 2016-08-18 15:52
 */
@WebFilter(urlPatterns = "/*")
public class BB_EncodingFilter implements Filter {

    private CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter("UTF-8", true, true);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        characterEncodingFilter.doFilter(servletRequest, servletResponse, filterChain);

    }

    @Override
    public void destroy() {

    }
}
