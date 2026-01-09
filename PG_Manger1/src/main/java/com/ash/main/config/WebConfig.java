package com.ash.main.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://127.0.0.1:5500", 
                		"http://localhost:5500",
                		"https://pgmanagerbackend.onrender.com", 
                		"https://ashish1900.github.io",
                		"/otphttps://res.cloudinary.com/**" )
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	
    	Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String location = basePath.toUri().toString();
        
		
            registry.addResourceHandler("/qr/**")
                    .addResourceLocations(location + "qr/")
                    .setCachePeriod(0); // disable caching for testing
            
            registry.addResourceHandler("/receipt/**")
                    .addResourceLocations(location + "paymentReceipt/")
                    .setCachePeriod(0);
    }
}
