package de.tsearch.statuscollector.service.twitch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tsearch.statuscollector.service.twitch.entity.Wrapper;
import kong.unirest.HttpRequest;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class GenericTwitchClient<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericTwitchClient.class);


    final Map<String, String> standardHeader;
    final ObjectMapper objectMapper;
    final SimpleDateFormat rfcDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    private final Class<T> type;


    public GenericTwitchClient(@Value("${twitch.clientid}") String clientId,
                               @Value("${twitch.apptoken}") String token,
                               ObjectMapper objectMapper, Class<T> type) {
        this.objectMapper = objectMapper;
        this.type = type;
        this.standardHeader = Map.of("Authorization", "Bearer " + token,
                "Client-ID", clientId,
                "Accept", "*/*",
                "User-Agent", "TSearch");
    }

    List<T> requestWithCursorFollowing(HttpRequest<?> request) {
        return this.requestWithCursorFollowing(request, Integer.MAX_VALUE);
    }

    List<T> requestWithCursorFollowing(HttpRequest<?> request, int maxLevel) {
        String cursor = null;
        boolean retry = false;
        int currentLevel = 0;

        request.headers(standardHeader);

        List<T> data = new ArrayList<>();

        do {
            if (cursor != null) request.queryString("after", cursor);

            try {
                HttpResponse<String> response = request.asString();
                String ratelimit = response.getHeaders().getFirst("Ratelimit-Remaining");
                if (ratelimit != null) LOGGER.debug("Ratelimit-Remaining: " + ratelimit);

                if (response.getStatus() == 200) {
                    Wrapper<JsonNode> dataWrapper = objectMapper.readValue(response.getBody(), new TypeReference<>() {
                    });
                    for (JsonNode jsonNode : dataWrapper.getData()) {
                        data.add(objectMapper.treeToValue(jsonNode, type));
                    }

                    if (dataWrapper.getPagination() != null) {
                        cursor = dataWrapper.getPagination().getCursor();
                    } else {
                        cursor = null;
                    }

                    retry = false;
                    currentLevel++;
                } else if (response.getStatus() == 429) {
                    retry = true;
                    LOGGER.warn("Rate-Limit reached!");
                    Thread.sleep(500);
                }
            } catch (JsonProcessingException | UnirestException | InterruptedException e) {
                e.printStackTrace();
            }
        } while ((cursor != null || retry) && currentLevel < maxLevel);

        return data;
    }

}
