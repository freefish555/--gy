package com.gydl.djbh.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
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
 * Spring MVC 配置 - 强制所有响应使用 UTF-8 编码
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 覆盖消息转换器，确保 JSON 响应使用 UTF-8
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

        // JSON converter - force UTF-8
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        List<MediaType> jsonMediaTypes = new ArrayList<>();
        jsonMediaTypes.add(new MediaType("application", "json", StandardCharsets.UTF_8));
        jsonMediaTypes.add(new MediaType("application", "*+json", StandardCharsets.UTF_8));
        jsonConverter.setSupportedMediaTypes(jsonMediaTypes);
        converters.add(jsonConverter);
    }
}
