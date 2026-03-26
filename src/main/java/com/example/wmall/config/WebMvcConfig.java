package com.example.wmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * Web MVC 相关配置
 * 主要用于静态资源（上传文件）的访问映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 与 application.yml 中 creation.upload.base-path 保持一致
     * 例如：./creation/upload/
     */
    @Value("${creation.upload.base-path}")
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将相对路径转换为绝对路径
        String absolutePath = Paths.get(basePath).toFile().getAbsolutePath();
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath = absolutePath + File.separator;
        }
        // 访问地址：/creation/upload/**   ->   本地目录：basePath
    registry.addResourceHandler("/creation/upload/**")
            .addResourceLocations("file:" + absolutePath);

        // 访问地址：/creation/**   ->   本地目录：basePath（例如：D:/project/creation/upload/）
        registry.addResourceHandler("/creation/**")
                .addResourceLocations("file:" + absolutePath);

    }
}
