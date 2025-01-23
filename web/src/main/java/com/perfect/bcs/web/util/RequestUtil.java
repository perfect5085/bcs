package com.perfect.bcs.web.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Splitter;
import java.net.URLEncoder;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
public class RequestUtil {

    public static final String KEY_TOKEN = "token";
    public static final String KEY_HOSP_CODE = "hospCode";
    public static final String KEY_REQUEST_ID = "requestId";
    public static final String KEY_RESPONSE_ID = "responseId";
    public static final String KEY_DEVICE_ID = "deviceId";

    /**
     * 系统访问开始时间key
     */
    public static final String KEY_ACCESS_START_TIME = "_accessStartTime";

    /**
     * 获取设备id
     *
     * @param request
     * @return
     */
    public static String getDeviceId(HttpServletRequest request) {
        return getValue(request, KEY_DEVICE_ID);
    }

    /**
     * 获取User Agent
     *
     * @param request
     * @return
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("user-agent");
    }

    /**
     * 获取token
     *
     * @param request
     * @return
     */
    public static String getToken(HttpServletRequest request) {
        String token = getValue(request, KEY_TOKEN);
        if (StrUtil.isBlank(token)) {
            token = "system-make-" + IdUtil.objectId();
        }
        if (StringUtils.length(token) > 120) {
            throw new RuntimeException("Token is illegal!");
        }

        return token;
    }


    public static String getDomainUrl(HttpServletRequest request) {

        String protocol = request.getProtocol();
        String serverName = request.getServerName();
        int port = request.getServerPort();

        StringBuilder sb = new StringBuilder();

        if (StringUtils.containsIgnoreCase(protocol, "http/")) {
            if (80 == port) {
                sb.append("http://")
                    .append(serverName);
            } else {
                sb.append("http://")
                    .append(serverName)
                    .append(":")
                    .append(port);
            }
        } else if (StringUtils.containsIgnoreCase(protocol, "https/")) {
            if (443 == port) {
                sb.append("https://")
                    .append(serverName);
            } else {
                sb.append("https://")
                    .append(serverName)
                    .append(":")
                    .append(port);
            }
        }

        return sb.toString();
    }

    public static String getParameter(String url, String key) {
        int questionMarkIndex = StringUtils.indexOf(url, "?");
        if (questionMarkIndex < 0) {
            return null;
        }

        String queryString = StringUtils.substring(url, questionMarkIndex + 1);

        List<String> parameterKeyValueList = Splitter.on("&")
            .splitToList(queryString);
        if (CollectionUtils.isNotEmpty(parameterKeyValueList)) {
            for (String str : parameterKeyValueList) {
                List<String> keyValueList = Splitter.on("=")
                    .splitToList(str);
                if (CollectionUtils.isNotEmpty(keyValueList) && keyValueList.size() == 2) {
                    if (StringUtils.equalsIgnoreCase(key, keyValueList.get(0))) {
                        return keyValueList.get(1);
                    }
                }
            }
        }
        return null;
    }

    public static String getRequestHospCode(HttpServletRequest request) {
        return getValue(request, KEY_HOSP_CODE);
    }

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

    public static String getResponseId(HttpServletRequest request) {
        return (String) request.getAttribute(KEY_RESPONSE_ID);
    }

    public static boolean isFromSwagger(HttpServletRequest request) {
        // 判断是否支持来自swagger的请求
        String referer = request.getHeader("Referer");
        if (StringUtils.isNotBlank(referer)
            && StringUtils.containsIgnoreCase(referer,
                                              "swagger-ui.html")) {
            // swagger 默认拥有所有权限
            return true;
        }
        return false;
    }


    public static String getValue(HttpServletRequest request, String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        // 从request对象中获取
        String value = (String) request.getAttribute(key);

        // 从 request 参数中获取
        if (StringUtils.isBlank(value)) {
            value = request.getParameter(key);
        }

        // 从header中获取token
        if (StringUtils.isBlank(value)) {
            value = request.getHeader(key);
        }

        return value;
    }

    public static void setFileDownloadHeader(HttpServletRequest request,
                                             HttpServletResponse response, String fileName)
        throws Throwable {
        String userAgent = request.getHeader("USER-AGENT");

        String finalFileName = null;
        // IE 浏览器
        if (StringUtils.contains(userAgent, "MSIE") || StringUtils.contains(userAgent, "Trident")
            || StringUtils.contains(userAgent, "Edge")) {
            finalFileName = URLEncoder.encode(fileName, "UTF8");
        }
        // Firefox, google等其他浏览器
        else {
            finalFileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
        }

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM.toString());
        // 解决Firefox下载英文+中文组合的文件名的问题
        response.setHeader("Content-Disposition", "attachment; filename=\"" + finalFileName + "\"");
    }

}
