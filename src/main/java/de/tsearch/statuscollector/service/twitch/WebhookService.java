package de.tsearch.statuscollector.service.twitch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tsearch.statuscollector.service.twitch.entity.EventEnum;
import de.tsearch.statuscollector.service.twitch.entity.webhook.Condition;
import de.tsearch.statuscollector.service.twitch.entity.webhook.Subscription;
import de.tsearch.statuscollector.service.twitch.entity.webhook.Transport;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WebhookService extends GenericTwitchClient<Subscription> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookService.class);

    private final String webhookHost;


    public WebhookService(@Value("${twitch.clientid}") String clientId,
                          @Value("${twitch.apptoken}") String token,
                          ObjectMapper objectMapper,
                          @Value("${webhook.host}") String webhookHost) {
        super(clientId, token, objectMapper, Subscription.class);
        this.webhookHost = webhookHost;
    }

    public List<Subscription> getAllSubscriptions() {
        return requestWithCursorFollowing(Unirest.get("https://api.twitch.tv/helix/eventsub/subscriptions"));
    }

    public void requestNewWebhook(long broadcasterId, EventEnum eventEnum, String secret) throws JsonProcessingException, UnirestException {
        LOGGER.debug("Request new webhook for broadcaster id " + broadcasterId + " for event " + eventEnum);

        Subscription subscription = new Subscription();
        subscription.setType(eventEnum.getWebhookEventType());
        subscription.setVersion("1");
        subscription.setCondition(new Condition(String.valueOf(broadcasterId), String.valueOf(broadcasterId)));

        Transport transport = new Transport();
        transport.setMethod("webhook");
        transport.setCallback(webhookHost + "/webhook/" + broadcasterId);
        transport.setSecret(secret);
        subscription.setTransport(transport);

        HttpResponse<String> response = Unirest
                .post("https://api.twitch.tv/helix/eventsub/subscriptions")
                .headers(standardHeader)
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(subscription))
                .asString();
        if (response.getStatus() == 202) {
            LOGGER.info("Requested new webhook for broadcaster id " + broadcasterId + " for event " + eventEnum);
        } else {
            LOGGER.error("Cannot request new webhook for broadcaster id " + broadcasterId + ". Status: " + response.getStatus() + " - Body: " + response.getBody());
        }
    }

    public void deleteWebhook(UUID webhookId) {
        try {
            Unirest
                    .delete("https://api.twitch.tv/helix/eventsub/subscriptions")
                    .headers(standardHeader).queryString("id", webhookId.toString())
                    .asString();
            LOGGER.info("Deleted webhook id " + webhookId);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
}
