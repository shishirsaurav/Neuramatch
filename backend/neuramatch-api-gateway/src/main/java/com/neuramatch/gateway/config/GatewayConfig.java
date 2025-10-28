package com.neuramatch.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class GatewayConfig implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(@NonNull ServerCodecConfigurer configurer) {
        DefaultPartHttpMessageReader partReader = new DefaultPartHttpMessageReader();
        partReader.setMaxInMemorySize(10 * 1024 * 1024); // 10MB

        MultipartHttpMessageReader multipartReader = new MultipartHttpMessageReader(partReader);

        configurer.defaultCodecs().multipartReader(multipartReader);
        configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024); // 10MB
    }
}
