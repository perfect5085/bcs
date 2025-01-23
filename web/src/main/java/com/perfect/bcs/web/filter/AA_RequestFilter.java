package com.perfect.bcs.web.filter;

import cn.hutool.core.util.IdUtil;
import com.perfect.bcs.web.util.RequestUtil;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 * Filter的执行顺序由Java文件的名字升序排序
 *
 * @author liangbo 梁波
 * @since 2016-08-18 15:52
 */
@WebFilter(urlPatterns = "/*")
public class AA_RequestFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {

        RequestUtil.setRequestId((HttpServletRequest) servletRequest, IdUtil.objectId());
        RequestUtil.setResponseId((HttpServletRequest) servletRequest, IdUtil.objectId());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
