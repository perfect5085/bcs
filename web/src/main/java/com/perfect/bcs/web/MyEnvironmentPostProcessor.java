package com.perfect.bcs.web;


import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * 自定义环境处理，在运行SpringApplication之前加载任意配置文件到Environment环境中
 *
 * @author liangbo 梁波
 * @date 2021-12-09 17:40
 */
@Slf4j
@Component
public class MyEnvironmentPostProcessor implements EnvironmentPostProcessor {


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        // 获取激活的profile集合
        List<String> profileList = getActiveProfiles(environment);
        environment.setActiveProfiles(profileList.toArray(new String[0]));

        Map<String, Resource> resourceMap = getResourceMap();

        //循环添加
        for (String profile : profileList) {
            //从classpath路径下面查找文件
            Resource resource = resourceMap.get(profile);
            //加载成PropertySource对象，并添加到Environment环境中
            environment.getPropertySources()
                .addLast(loadProfiles(resource));
        }
    }

    private List<String> getActiveProfiles(ConfigurableEnvironment environment) {

        String springActiveProfileKey = "spring.profiles.active";
        // 从environment中获取profile名称，通过jvm参数、命令行参数、默认application.properties任意一种方式配置都可以获取到
        String jvmOrCmdlineActiveProfile = environment.getProperty(springActiveProfileKey);
        if (log.isWarnEnabled()) {
            log.warn("jvmOrCmdlineActiveProfile = {}", jvmOrCmdlineActiveProfile);
        }

        String activeProfile;
        // 如果是本地IDEA，Eclipse等启动， defaultActiveProfile 为空
        if (StrUtil.isBlank(jvmOrCmdlineActiveProfile)) {
            // 读取默认： application.properties
            Resource resource = new ClassPathResource("application.properties");
            try {
                properties.load(resource.getInputStream());
            } catch (Throwable e) {
                throw new RuntimeException("找不到" + jvmOrCmdlineActiveProfile + "配置文件", e);
            }

            // 读取指定激活的 profile
            activeProfile = properties.getProperty(springActiveProfileKey);
        } else {
            activeProfile = jvmOrCmdlineActiveProfile;
        }
        // 避免 null
        activeProfile = StrUtil.trimToEmpty(activeProfile);

        // 总是第一个读取默认的配置文件： application-default.properties
        List<String> profileList = Lists.newArrayList("default");
        // 然后读取指定的配置文件，后面的配置文件会覆盖前面配置文件的值
        profileList.add(activeProfile);

        return profileList;
    }


    /**
     * 从 profile 文件夹中读取所有的配置文件
     *
     * @return
     */
    private Map<String, Resource> getResourceMap() {

        String profilePattern = "classpath:profile/**/application*";
        Map<String, Resource> resourceMap = Maps.newHashMap();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resourceArray = resolver.getResources(profilePattern);
            for (Resource resource : resourceArray) {

                String fileName = resource.getFilename();
                // 查找出： properties yml 的文件
                if (StrUtil.endWith(fileName, ".properties")
                    || StrUtil.endWith(fileName, ".yml")
                    || StrUtil.endWith(fileName, ".yaml")) {

                    // 解析 profile 的名称
                    // 1 移除前缀
                    fileName = StrUtil.removePrefix(fileName, "application");
                    fileName = StrUtil.removePrefix(fileName, "-");
                    // 2 移除后缀
                    fileName = StrUtil.removeSuffix(fileName, ".properties");
                    fileName = StrUtil.removeSuffix(fileName, ".yml");
                    fileName = StrUtil.removeSuffix(fileName, ".yaml");

                    resourceMap.put(fileName, resource);
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("读取 " + profilePattern + " 异常", e);
        }
        return resourceMap;
    }

    /**
     * 缓存 Properties对象
     */
    private final Properties properties = new Properties();

    /**
     * 加载单个配置文件
     */
    private PropertySource<?> loadProfiles(Resource resource) {
        if (!resource.exists()) {
            throw new IllegalArgumentException("异常：配置文件 " + resource.getFilename() + " 不存在");
        }

        try {
            String resourceName = resource.getFilename();
            if (StrUtil.endWith(resourceName, ".properties")) {
                properties.load(resource.getInputStream());
                return new PropertiesPropertySource(resource.getFilename(), properties);

            } else {
                YamlPropertySourceLoader sourceLoader = new YamlPropertySourceLoader();
                List<PropertySource<?>> propertySources = sourceLoader.load(resource.getFilename(), resource);
                return propertySources.get(0);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("解析配置文件失败: " + resource.getFilename(), ex);
        }
    }
}
