package de.tsearch.statuscollector;

import de.tsearch.statuscollector.database.postgres.entity.Broadcaster;
import de.tsearch.statuscollector.database.postgres.entity.StreamStatus;
import de.tsearch.statuscollector.database.postgres.repository.BroadcasterRepository;
import de.tsearch.tclient.UserClient;
import de.tsearch.tclient.http.respone.User;
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
    CommandLineRunner addRemoveBroadcasterIds(@Value("#{'${import.broadcasterIds:}'.split(',')}") List<Long> addBroadcasterIds,
                                              @Value("#{'${remove.broadcasterIds:}'.split(',')}") List<Long> removeBroadcasterIds,
                                              @Value("#{'${import.broadcasterNames:}'.split(',')}") List<String> addBroadcasterNames,
                                              @Value("#{'${remove.broadcasterNames:}'.split(',')}") List<String> removeBroadcasterNames,
                                              BroadcasterRepository broadcasterRepository,
                                              UserClient userClient) {
        return args -> {
            for (Long broadcasterId : addBroadcasterIds) {
                if (broadcasterId == null) continue;
                final Optional<Broadcaster> broadcasterOptional = broadcasterRepository.findById(broadcasterId);

                if (broadcasterOptional.isEmpty()) {
                    final Broadcaster broadcaster = new Broadcaster();
                    broadcaster.setId(broadcasterId);
                    broadcaster.setStatus(StreamStatus.OFFLINE);
                    broadcaster.setVip(true);
                    broadcaster.setTwitchAuthorised(false);
                    broadcasterRepository.save(broadcaster);
                    LOGGER.info("Added broadcaster " + broadcasterId + " to database!");
                }
            }

            for (Long broadcasterId : removeBroadcasterIds) {
                if (broadcasterId == null) continue;
                final Optional<Broadcaster> broadcasterOptional = broadcasterRepository.findById(broadcasterId);

                if (broadcasterOptional.isPresent()) {
                    broadcasterRepository.delete(broadcasterOptional.get());
                    LOGGER.info("Removed broadcaster " + broadcasterId + " from database!");
                }
            }

            if (addBroadcasterNames.size() == 1 && addBroadcasterNames.get(0).equals("")) addBroadcasterNames.clear();
            if (removeBroadcasterNames.size() == 1 && removeBroadcasterNames.get(0).equals(""))
                removeBroadcasterNames.clear();

            List<User> users = userClient.getUserByUsername(addBroadcasterNames);
            for (User user : users) {
                long broadcasterId;
                try {
                    broadcasterId = Long.parseLong(user.getId());
                } catch (NumberFormatException e) {
                    continue;
                }
                final Optional<Broadcaster> broadcasterOptional = broadcasterRepository.findById(broadcasterId);

                if (broadcasterOptional.isEmpty()) {
                    final Broadcaster broadcaster = new Broadcaster();
                    broadcaster.setId(broadcasterId);
                    broadcaster.setStatus(StreamStatus.OFFLINE);
                    broadcaster.setDisplayName(user.getDisplayName());
                    broadcaster.setVip(true);
                    broadcaster.setTwitchAuthorised(false);
                    broadcasterRepository.save(broadcaster);
                    LOGGER.info("Added broadcaster " + broadcasterId + " to database!");
                }
            }

            users = userClient.getUserByUsername(removeBroadcasterNames);
            for (User user : users) {
                long broadcasterId;
                try {
                    broadcasterId = Long.parseLong(user.getId());
                } catch (NumberFormatException e) {
                    continue;
                }
                final Optional<Broadcaster> broadcasterOptional = broadcasterRepository.findById(broadcasterId);

                if (broadcasterOptional.isPresent()) {
                    broadcasterRepository.delete(broadcasterOptional.get());
                    LOGGER.info("Removed broadcaster " + broadcasterId + " from database!");
                }
            }
        };
    }

}
