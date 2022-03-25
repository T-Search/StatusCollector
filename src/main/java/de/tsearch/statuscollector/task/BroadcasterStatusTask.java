package de.tsearch.statuscollector.task;

import de.tsearch.statuscollector.database.postgres.entity.Broadcaster;
import de.tsearch.statuscollector.database.postgres.entity.StreamStatus;
import de.tsearch.statuscollector.database.postgres.repository.BroadcasterRepository;
import de.tsearch.tclient.StreamClient;
import de.tsearch.tclient.http.respone.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BroadcasterStatusTask {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StreamClient streamClient;
    private final BroadcasterRepository broadcasterRepository;

    public BroadcasterStatusTask(StreamClient streamClient, BroadcasterRepository broadcasterRepository) {
        this.streamClient = streamClient;
        this.broadcasterRepository = broadcasterRepository;
    }

    @Scheduled(fixedRate = 3 * 60 * 60 * 1000, initialDelay = 20 * 1000)
    protected void checkBroadcasterStatus() {
        logger.info("Check all broadcaster status");
        List<Broadcaster> broadcasters = new ArrayList<>();
        broadcasterRepository.findAll().forEach(broadcasters::add);

        final List<Stream> onlineStreams = streamClient.getOnlineStreams(broadcasters.stream().map(Broadcaster::getId).collect(Collectors.toList()));

        for (Stream onlineStream : onlineStreams) {
            final Optional<Broadcaster> broadcasterOptional = broadcasters.stream().filter(broadcaster -> broadcaster.getId() == onlineStream.getUserID()).findAny();
            if (broadcasterOptional.isPresent()) {
                final Broadcaster broadcaster = broadcasterOptional.get();
                broadcaster.setStatus(StreamStatus.ONLINE);
                broadcasterRepository.save(broadcaster);
                broadcasters.remove(broadcaster);
            }
        }

        for (Broadcaster broadcaster : broadcasters) {
            broadcaster.setStatus(StreamStatus.OFFLINE);
            broadcasterRepository.save(broadcaster);
        }
        logger.info("Updated all broadcaster status");
    }
}
