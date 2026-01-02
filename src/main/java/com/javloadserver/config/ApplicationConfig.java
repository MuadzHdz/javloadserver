package com.javloadserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ApplicationConfig {

    @Bean
    public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
        ByteArrayHttpMessageConverter arrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
        arrayHttpMessageConverter.setSupportedMediaTypes(Arrays.asList(
            MediaType.APPLICATION_OCTET_STREAM,
            MediaType.IMAGE_PNG,
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_GIF,
            MediaType.APPLICATION_PDF,
            MediaType.TEXT_PLAIN
        ));
        return arrayHttpMessageConverter;
    }

    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(byteArrayHttpMessageConverter());
    }
}