package com.example.jobplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.jobplatform.config.DeepseekProperties;

@SpringBootApplication
@EnableConfigurationProperties(DeepseekProperties.class)
public class JobPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobPlatformApplication.class, args);
    }
}
