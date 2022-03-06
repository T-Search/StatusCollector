package de.tsearch.statuscollector;

import de.tsearch.statuscollector.database.redis.entity.Broadcaster;
import de.tsearch.statuscollector.database.redis.entity.StreamStatus;
import de.tsearch.statuscollector.database.redis.repository.BroadcasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

@EnableScheduling
@SpringBootApplication
public class StatusCollectorApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public static void main(String[] args) {
        SpringApplication.run(StatusCollectorApplication.class, args);
    }

    @Bean
    CommandLineRunner addRemoveBroadcasterIds(@Value("#{'${import.broadcasterIds:}'.split(',')}") List<Long> addBroadcasters,
                                              @Value("#{'${remove.broadcasterIds:}'.split(',')}") List<Long> removeBroadcasters,
                                              BroadcasterRepository broadcasterRepository) {
        return args -> {
            for (Long broadcasterId : addBroadcasters) {
                if (broadcasterId == null) continue;
                final Optional<Broadcaster> broadcasterOptional = broadcasterRepository.findById(broadcasterId);

                if (broadcasterOptional.isEmpty()) {
                    final Broadcaster broadcaster = new Broadcaster();
                    broadcaster.setId(broadcasterId);
                    broadcaster.setStatus(StreamStatus.OFFLINE);
                    broadcasterRepository.save(broadcaster);
                    LOGGER.info("Added broadcaster " + broadcasterId + " to database!");
                }
            }

            for (Long broadcasterId : removeBroadcasters) {
                if (broadcasterId == null) continue;
                final Optional<Broadcaster> broadcasterOptional = broadcasterRepository.findById(broadcasterId);

                if (broadcasterOptional.isPresent()) {
                    broadcasterRepository.delete(broadcasterOptional.get());
                    LOGGER.info("Removed broadcaster " + broadcasterId + " from database!");
                }
            }
        };
    }

}
