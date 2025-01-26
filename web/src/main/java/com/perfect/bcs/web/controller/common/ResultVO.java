package com.perfect.bcs.web.controller.common;

import java.io.Serializable;
import lombok.Data;
import org.apache.commons.lang3.SystemUtils;

/**
 * @author liangbo 梁波
 * @date 2025-01-26 12:24
 */
@Data
public class ResultVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * <pre>
     *      0代表成功，其它值代表失败
     *      大于0，代表业务提示，可以讲 message 展示给用户（不符合业务规则导致的失败）
     *      小于0，代表系统错误提示，不能展示给用户（因为系统的一些Bug导致的失败）
     * </pre>
     */
    private Integer code = 0;

    /**
     * <pre>
     *      针对code的描述:
     *      注意：展现给用户时,需要考虑国际化
     * </pre>
     */
    private String message = "成功";

    /**
     * 系统错误提示 : 为了便于排查问题
     */
    private String errorMessage;

    /**
     * 结果数据
     */
    private T data;

    /**
     * 版本号
     */
    private String version;

    /**
     * 发起请求id ：通常由发起方生成，代表唯一的一次请求
     */
    private String requestId;

    /**
     * <pre>
     *  发起请求时间戳 ： 代表在什么时间点发出请求
     *  请使用时间格式：yyyy-MM-dd HH:mm:ss，例如：2019-05-09 09:08:01
     *  注意：如果小于10，前面补0
     * </pre>
     */
    private String requestTimestamp;

    /**
     * <pre>
     *  应用接收到请求的时间戳 ： 代表在什么时间点接收到请求
     *  请使用时间格式：yyyy-MM-dd HH:mm:ss，例如：2019-05-09 09:08:01
     *  注意：如果小于10，前面补0
     *  DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
     * </pre>
     */
    private String receivedTimestamp;

    /**
     * 响应id ：通常由响应方生成，代表唯一的一次响应
     */
    private String responseId;

    /**
     * <pre>
     *  请求响应时间戳 ： 代表在什么时间点发出响应请求
     *  请使用时间格式：yyyy-MM-dd HH:mm:ss，例如：2019-05-09 09:08:01
     *  注意：如果小于10，前面补0
     * </pre>
     */
    private String responseTimestamp;

    /**
     * 后端应用处理的时长，单位毫秒
     */
    private Integer responseDuration;

    public ResultVO() {
    }

    public ResultVO(T data) {
        this.data = data;
    }

    public ResultVO(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return new StringBuilder("requestId=").append(requestId)
                                              .append(SystemUtils.LINE_SEPARATOR)
                                              .append("code=")
                                              .append(code)
                                              .append(SystemUtils.LINE_SEPARATOR)
                                              .append("message=")
                                              .append(message)
                                              .append(SystemUtils.LINE_SEPARATOR)
                                              .append("errorMessage=")
                                              .append(errorMessage)
                                              .append(SystemUtils.LINE_SEPARATOR)
                                              .append("version=")
                                              .append(version)
                                              .append(SystemUtils.LINE_SEPARATOR)
                                              .append("responseDuration=")
                                              .append(responseDuration)
                                              .append(SystemUtils.LINE_SEPARATOR)
                                              .toString();
    }

}