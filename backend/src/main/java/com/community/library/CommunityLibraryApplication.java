package com.community.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommunityLibraryApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityLibraryApplication.class, args);
    }
}
