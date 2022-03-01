package de.tsearch.statuscollector;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tsearch.statuscollector.database.redis.repository.BroadcasterRepository;
import de.tsearch.statuscollector.service.twitch.StreamsService;
import de.tsearch.statuscollector.service.twitch.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.invoke.MethodHandles;

@EnableScheduling
@SpringBootApplication
public class StatusCollectorApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public static void main(String[] args) {
        SpringApplication.run(StatusCollectorApplication.class, args);
    }

    @Bean
    CommandLineRunner afterStartRunner(WebhookService webhookService, ObjectMapper objectMapper, StreamsService streamsService, BroadcasterRepository broadcasterRepository) {
        return args -> {
        };
    }

}
