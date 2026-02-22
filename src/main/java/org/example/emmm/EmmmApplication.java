package org.example.emmm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableJpaAuditing
@SpringBootApplication
public class EmmmApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmmmApplication.class, args);
    }

}
