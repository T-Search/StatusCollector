package de.tsearch.statuscollector.service.twitch;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tsearch.statuscollector.service.twitch.entity.Stream;
import kong.unirest.GetRequest;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StreamsService extends GenericTwitchClient<Stream> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamsService.class);

    public StreamsService(@Value("${twitch.clientid}") String clientId,
                          @Value("${twitch.apptoken}") String token,
                          ObjectMapper objectMapper) {
        super(clientId, token, objectMapper, Stream.class);
    }

    public List<Stream> getOnlineStreams(List<Long> broadcasterIds) {
        ArrayList<Stream> onlineStreams = new ArrayList<>();
        final int batchSize = 100;
        for (int round = 0; round < Math.ceil(((float) broadcasterIds.size()) / batchSize); round++) {
            List<Long> list = broadcasterIds.subList(round * batchSize, Math.min(broadcasterIds.size(), (round + 1) * batchSize));
            GetRequest request = Unirest
                    .get("https://api.twitch.tv/helix/streams")
                    .queryString("first", batchSize)
                    .queryString("user_id", list);
            onlineStreams.addAll(requestWithCursorFollowing(request, 1));
        }

        return onlineStreams;
    }

    public List<Stream> getStreams() {
        GetRequest request = Unirest
                .get("https://api.twitch.tv/helix/streams")
                .queryString("first", 100)
                .queryString("language", "de");
        return requestWithCursorFollowing(request, 5);
    }
}
