package com.perfect.bcs.web.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.google.common.base.Predicate;
import io.swagger.annotations.Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
@Configuration
@EnableSwagger2WebMvc
@EnableKnife4j
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        Predicate<RequestHandler> predicate = (input) -> {

            Class<?> declaringClass = input.declaringClass();

            // 被注解的类
            if (declaringClass.isAnnotationPresent(RestController.class)
                && declaringClass.isAnnotationPresent(Api.class)) {
                return true;
            }

            return false;

        };

        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        docket.apiInfo(apiInfo());
        docket.useDefaultResponseMessages(false);

        ApiSelectorBuilder apiSelectorBuilder = docket.select();
        apiSelectorBuilder.apis(predicate);
        apiSelectorBuilder.apis(RequestHandlerSelectors.basePackage("com.perfect.bcs.web"));
        apiSelectorBuilder.paths(PathSelectors.any());

        return apiSelectorBuilder.build();

    }

    private ApiInfo apiInfo() {

        return new ApiInfoBuilder()
            // 大标题
            .title("接口")
            // 描述
            .description("接口文档")
            .termsOfServiceUrl("https://www.perfect.com").
            // 联系人
                contact(new Contact("梁波", "", "perfect5085@163.com"))
            // 版本
            .version("1.0")
            .build();
    }
}
