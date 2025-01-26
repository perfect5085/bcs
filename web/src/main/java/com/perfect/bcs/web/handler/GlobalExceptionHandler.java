package com.perfect.bcs.web.handler;

import cn.hutool.core.util.StrUtil;
import com.perfect.bcs.biz.common.BizException;
import com.perfect.bcs.web.controller.common.ResultVO;
import com.perfect.bcs.web.util.RequestUtil;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

        ResultVO jsonResult = new ResultVO();
        String msg = MessageFormatter.format("BizErrorCode= {} RequestId= {}", e.getCode(), jsonResult.getRequestId())
                                     .getMessage();
        bizLogger.error(msg, e);

        jsonResult.setCode(e.getCode());
        jsonResult.setMessage(messageSource.getMessage(String.valueOf(e.getCode()), e.getArgs(), request.getLocale()));
        jsonResult.setErrorMessage(e.getMessage());

        return jsonResult;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResultVO defaultConstraintViolationException(HttpServletRequest request, ConstraintViolationException e) {

        ResultVO jsonResult = new ResultVO();
        logger.error("System_error:requestId=" + jsonResult.getRequestId(), e);

        int code = -2;
        jsonResult.setCode(code);
        jsonResult.setMessage(messageSource.getMessage(String.valueOf(code), null, request.getLocale()));

        String message = e.getMessage();
        int index = StrUtil.indexOf(message, '.');
        if (index >= 0) {
            message = StrUtil.subSuf(message, index + 1);
        }
        jsonResult.setErrorMessage("参数校验失败:" + message);

        return jsonResult;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResultVO defaultMethodArgumentNotValidException(HttpServletRequest request,
                                                           MethodArgumentNotValidException e) {

        ResultVO jsonResult = new ResultVO();
        logger.error("System_error:requestId=" + jsonResult.getRequestId(), e);

        int code = -2;
        jsonResult.setCode(code);
        jsonResult.setMessage(messageSource.getMessage(String.valueOf(code), null, request.getLocale()));

        BindingResult bindingResult = e.getBindingResult();
        StringBuilder sb = new StringBuilder("参数校验失败:");
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getField()).append(":").append(fieldError.getDefaultMessage()).append(",");
        }
        String msg = sb.toString();
        msg = msg.substring(0, msg.length() - 1);
        jsonResult.setErrorMessage(msg);

        return jsonResult;
    }

}
