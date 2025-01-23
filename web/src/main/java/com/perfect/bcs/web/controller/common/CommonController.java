package com.perfect.bcs.web.controller.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
@Slf4j
public class CommonController {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected MessageSource messageSource;

    /**
     * 标准化 pageIndex
     */
    protected Integer normalizePageIndex(Integer pageIndex) {
        if (pageIndex == null || pageIndex < 1) {
            // 默认从第一页开始
            return 1;
        }
        return pageIndex;
    }

    /**
     * 标准化 pageSize
     */
    protected Integer normalizePageSize(Integer pageSize) {
        if (null == pageSize || pageSize < 1) {
            // 默认获取20个
            pageSize = 20;
        }
        return pageSize;
    }

}
