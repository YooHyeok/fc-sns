package com.fc.sns.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        ResourceResolver resolver = new ReactResourceResolver();
        registry.addResourceHandler("/**")
                .resourceChain(true)
                .addResolver(resolver);
    }

    public class ReactResourceResolver implements ResourceResolver {

        private static final String REACT_DIR = "/static/";
        private static final String REACT_STATIC_DIR = "static";

        private Resource index = new ClassPathResource(REACT_DIR + "index.html");
        private List<String> staticExtension = Arrays.asList("png", "jpg", "io", "json", "js", "html");

        private Resource resolve(String requestPath) {
            if (requestPath == null) {
                return null;
            }

            /*if (staticExtension.contains(requestPath)
                    || requestPath.startsWith(REACT_STATIC_DIR)) {
                return new ClassPathResource(REACT_DIR + requestPath);
            } else {
                return index;
            }*/

            /**
             * 어떤 경로 요청이 들어오든지 간에 (예: /authentication) /static 디렉토리에서 리소스를 탐색해야한다.
             */
            for (String extension : staticExtension) {
                if (requestPath.endsWith("." + extension)) {
                    String adjustedPath = requestPath.startsWith("authentication/") ? requestPath.substring("authentication/".length()) : requestPath;
                    Resource resource = new ClassPathResource(REACT_DIR + adjustedPath);
                    if (resource.exists() && resource.isReadable()) {
                        return resource;
                    }
                }
            }
            return index;
        }

        @Override
        public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
            return resolve(requestPath);
        }

        @Override
        public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
            Resource resolvedResource = resolve(resourcePath);
            if (resolvedResource == null) {
                return null;
            }
            try {
                return resolvedResource.getURL().toString();
            } catch (IOException e) {
                return resolvedResource.getFilename();
            }
        }
    }
}