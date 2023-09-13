package com.example.foodsocialproject.controller.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path postUploadDir1 = Paths.get("./post-images");
        String postUploadPath = postUploadDir1.toFile().getAbsolutePath();
        registry.addResourceHandler("/post-images/**").addResourceLocations("file:/"+postUploadPath+"/");

        Path avatarUploadDir = Paths.get("./avatar-images");
        String avatarUploadPath = avatarUploadDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/avatar-images/**").addResourceLocations("file:/"+avatarUploadPath+"/");

        Path stepsUploadDir = Paths.get("./steps-images");
        String stepsUploadPath = stepsUploadDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/steps-images/**").addResourceLocations("file:/"+stepsUploadPath+"/");

        exposeDirectory("product-images", registry);
    }
    private void exposeDirectory(String dirName, ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(dirName);
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        if (dirName.startsWith("../")) dirName = dirName.replace("../", "");

        registry.addResourceHandler("/" + dirName + "/**").addResourceLocations("file:/"+ uploadPath + "/");
    }
}
