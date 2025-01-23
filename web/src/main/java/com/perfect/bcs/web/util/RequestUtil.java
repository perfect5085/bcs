package com.perfect.bcs.web.util;

import javax.servlet.http.HttpServletRequest;
import org.springframework.util.Assert;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
public class RequestUtil {

    public static final String KEY_REQUEST_ID  = "requestId";
    public static final String KEY_RESPONSE_ID = "responseId";

    /**
     * 设置当前请求的request id
     *
     * @param request
     * @param requestId
     */
    public static void setRequestId(HttpServletRequest request, String requestId) {
        Assert.notNull(requestId, "requestId is required!");
        request.setAttribute(KEY_REQUEST_ID, requestId);
    }

    public static String getRequestId(HttpServletRequest request) {
        return (String) request.getAttribute(KEY_REQUEST_ID);
    }

    /**
     * 设置当前请求的response id
     */
    public static void setResponseId(HttpServletRequest request, String responseId) {
        Assert.notNull(responseId, "responseId is required!");
        request.setAttribute(KEY_RESPONSE_ID, responseId);
    }

}
