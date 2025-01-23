package com.perfect.bcs.web.config;

import com.perfect.bcs.web.interceptor.CrossDomainInterceptor;
import com.perfect.bcs.web.interceptor.OptionsRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author liangbo 梁波
 * @date 2020-02-03 14:41
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CrossDomainInterceptor crossDomainInterceptor;
    @Autowired
    private OptionsRequestInterceptor optionsRequestInterceptor;

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        // 拦截所有 /admin/** 的访问地址
        interceptorRegistry.addInterceptor(crossDomainInterceptor);
        interceptorRegistry.addInterceptor(optionsRequestInterceptor);

    }

    @Bean(name = "messageSource")
    public MessageSource getMessageSource() {
        ReloadableResourceBundleMessageSource messageBundle =
            new ReloadableResourceBundleMessageSource();
        messageBundle.setBasename("classpath:i18n/messages");
        messageBundle.setDefaultEncoding("UTF-8");
        return messageBundle;
    }


}
