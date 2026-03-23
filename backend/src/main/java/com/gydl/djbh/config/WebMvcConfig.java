package com.gydl.djbh.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring MVC 配置 - 强制所有响应使用 UTF-8 编码，
 * 使用 Spring Boot 自动配置的 ObjectMapper（含 JavaTimeModule）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 覆盖消息转换器，确保 JSON 响应使用 UTF-8，并保留 Spring Boot 的 ObjectMapper 配置
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // String converter - force UTF-8
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        List<MediaType> stringMediaTypes = new ArrayList<>();
        stringMediaTypes.add(new MediaType("text", "plain", StandardCharsets.UTF_8));
        stringMediaTypes.add(new MediaType("text", "html", StandardCharsets.UTF_8));
        stringMediaTypes.add(new MediaType("*", "*", StandardCharsets.UTF_8));
        stringConverter.setSupportedMediaTypes(stringMediaTypes);
        converters.add(stringConverter);

        // JSON converter - use auto-configured ObjectMapper (with JavaTimeModule, etc.) + force UTF-8
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        List<MediaType> jsonMediaTypes = new ArrayList<>();
        jsonMediaTypes.add(new MediaType("application", "json", StandardCharsets.UTF_8));
        jsonMediaTypes.add(new MediaType("application", "*+json", StandardCharsets.UTF_8));
        jsonConverter.setSupportedMediaTypes(jsonMediaTypes);
        converters.add(jsonConverter);
    }
}
