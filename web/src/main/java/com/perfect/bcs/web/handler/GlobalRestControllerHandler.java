package com.perfect.bcs.web.handler;

import com.alibaba.fastjson.JSONObject;
import com.perfect.bcs.web.controller.common.ResultVO;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
@RestControllerAdvice(basePackages = {"com.perfect.bcs.web.controller"})
public class GlobalRestControllerHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> aClass) {
        // 如果接口返回的类型本身就是ResultVO那就没有必要进行额外的操作，返回false
        return !returnType.getGenericParameterType()
            .equals(ResultVO.class);
    }

    @Override
    public Object beforeBodyWrite(Object data, MethodParameter returnType, MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // String类型不能直接包装，所以要进行些特别的处理
        if (returnType.getGenericParameterType()
            .equals(String.class) || data instanceof String) {
            // 将数据包装在ResultVO里后，再转换为json字符串响应给前端
            return JSONObject.toJSONString(new ResultVO(data));

        }
        // 将原本的数据包装在ResultVO里
        return new ResultVO(data);
    }
}
