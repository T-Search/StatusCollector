package de.tsearch.statuscollector;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.logging.Logger;

@SpringBootApplication
public class StatusCollectorApplication {

    private static final Logger LOGGER = Logger.getLogger(StatusCollectorApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(StatusCollectorApplication.class, args);
    }

    @Bean
    CommandLineRunner afterStartRunner() {
        return args -> {
        };
    }

}
