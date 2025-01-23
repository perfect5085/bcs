package com.perfect.bcs.web.handler;

import com.alibaba.fastjson.JSONObject;
import com.perfect.bcs.web.util.RequestUtil;
import com.shuhong.common.shared.api.BizException;
import com.shuhong.common.shared.api.ResultVO;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局默认异常处理类
 *
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger    = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Logger bizLogger = LoggerFactory.getLogger("bizErrorLogger");

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResultVO defaultExceptionHandler(HttpServletRequest request, Throwable e) {
        //loggerUrl(request, logger);
        ResultVO jsonResult = new ResultVO(RequestUtil.getRequestId(request));
        logger.error("System_error:requestId=" + jsonResult.getRequestId(), e);
        jsonResult.setCode(-1);
        jsonResult.setMessage(messageSource.getMessage("-1", null, request.getLocale()));
        jsonResult.setErrorMessage(e.getMessage());

        return jsonResult;
    }

    @ExceptionHandler(BizException.class)
    @ResponseBody
    public ResultVO defaultBizExcetionHandler(HttpServletRequest request, BizException e) {
        //loggerUrl(request, bizLogger);

        ResultVO jsonResult = new ResultVO();
        String msg = MessageFormatter.format("BizErrorCode= {} RequestId= {}", e.getCode(), jsonResult.getRequestId())
                                     .getMessage();
        bizLogger.error(msg, e);

        jsonResult.setCode(e.getCode());
        jsonResult.setMessage(messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), request.getLocale()));
        jsonResult.setErrorMessage(e.getMessage());

        return jsonResult;
    }

    //private void loggerUrl(HttpServletRequest request, Logger logger) {
    //    String url = request.getRequestURL()
    //                        .toString();
    //    try {
    //        url = URLDecoder.decode(url, "utf-8");
    //    } catch (Throwable e) {
    //        logger.error("decode_error_url=" + url, e);
    //    }
    //
    //    StringBuilder sb = new StringBuilder();
    //    sb.append(IOUtils.LINE_SEPARATOR)
    //      .append("    requestUrl=")
    //      .append(url);
    //    sb.append(IOUtils.LINE_SEPARATOR)
    //      .append("    requestParam=")
    //      .append(JSONObject.toJSONString(request.getParameterMap()));
    //    logger.error(sb.toString());
    //}

}
