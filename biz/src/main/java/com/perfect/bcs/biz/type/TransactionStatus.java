package com.perfect.bcs.biz.type;

/**
 * 交易的状态
 *
 * @author liangbo 梁波
 * @date 2025-01-23 09:14
 */
public interface TransactionStatus {

    /**
     * started 开始
     */
    String STARTED = "started";
    /**
     * success 成功
     */
    String SUCCESS = "success";
    /**
     * failed 失败;
     */
    String FAILED  = "failed";
}
