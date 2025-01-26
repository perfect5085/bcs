package com.perfect.bcs.biz.common;

import lombok.Data;

/**
 * 业务异常类，主要通过code来传递异常种类信息
 *
 * @author liangbo 梁波
 * @date 2025-01-26 12:25
 */
@Data
public class BizException extends RuntimeException {

    /**
     * 错误码
     */
    private int code;

    /**
     * 格式化错误信息时，填充的参数
     */
    private Object[] args;

    /**
     * 数据，协议前端如何正确的处理异常
     */
    private Object data;

    public BizException() {

    }

    public BizException(int code) {
        super("errorCode=" + code);
        this.code = code;
    }

    public BizException(int code, Object... args) {
        super("errorCode=" + code);
        this.code = code;
        this.args = args;
    }
}
