package de.tsearch.statuscollector.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tsearch.statuscollector.database.redis.entity.Broadcaster;
import de.tsearch.statuscollector.database.redis.entity.StreamStatus;
import de.tsearch.statuscollector.database.redis.repository.BroadcasterRepository;
import de.tsearch.statuscollector.service.twitch.entity.EventEnum;
import de.tsearch.statuscollector.service.twitch.entity.webhook.Condition;
import de.tsearch.statuscollector.web.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("webhook")
public class WebhookController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper;
    private final BroadcasterRepository broadcasterRepository;

    public WebhookController(ObjectMapper objectMapper, BroadcasterRepository broadcasterRepository) {
        this.objectMapper = objectMapper;
        this.broadcasterRepository = broadcasterRepository;
    }

    @PostMapping("{broadcasterId:\\d+}")
    public ResponseEntity<?> request(@RequestHeader("Twitch-Eventsub-Message-Type") String messageType,
                                     @RequestBody String content) {

        switch (messageType) {
            case "webhook_callback_verification":
                return verifyWebhook(content);
            case "notification":
                return notification(content);
            case "revocation":
                break;
            default:
                logger.error("Unknown message type: " + messageType);
                break;
        }

        return ResponseEntity.badRequest().build();
    }

    private ResponseEntity<String> verifyWebhook(String content) {
        WebhookContentChallenge webhookContent;
        try {
            webhookContent = objectMapper.readValue(content, WebhookContentChallenge.class);
        } catch (JsonProcessingException e) {
            logger.error("Cannot parse json webhook notification", e);
            return ResponseEntity.badRequest().build();
        }

        if (webhookContent.getChallenge() != null) {
            final Condition condition = webhookContent.getSubscription().getCondition();
            logger.info("Accept webhook challenge for broadcaster id " + (condition.getBroadcasterUserID() != null ? condition.getBroadcasterUserID() : condition.getUserId()) + " for type " + webhookContent.getSubscription().getType());
            return ResponseEntity.ok(webhookContent.getChallenge());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    private ResponseEntity<Void> notification(String content) {
        WebhookContent<JsonNode> webhookContent;
        try {
            webhookContent = objectMapper.readValue(content, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            logger.error("Cannot parse json webhook notification", e);
            return ResponseEntity.badRequest().build();
        }

        EventEnum eventEnum = EventEnum.getByWebhookEventType(webhookContent.getSubscription().getType());

        if (eventEnum != null) {
            WebhookEvent event;
            try {
                event = (WebhookEvent) objectMapper.treeToValue(webhookContent.getEvent(), eventEnum.getContentClass());
            } catch (JsonProcessingException e) {
                logger.error("Cannot parse webhook notification content", e);
                return ResponseEntity.badRequest().build();
            }

            switch (eventEnum) {
                case STREAM_ONLINE -> streamOnline((WebhookContentStreamOnlineEvent) event);
                case STREAM_OFFLINE -> streamOffline((WebhookContentStreamOfflineEvent) event);
                case USER_UPDATE -> userUpdate((WebhookContentUserUpdateEvent) event);
            }


        }
        return ResponseEntity.ok().build();
    }

    private void streamOffline(WebhookContentStreamOfflineEvent event) {
        logger.info("Broadcaster " + event.getBroadcasterUserID() + " went offline");

        final Optional<Broadcaster> broadcasterOptional1 = broadcasterRepository.findById(event.getBroadcasterUserID());
        if (broadcasterOptional1.isPresent()) {
            final Broadcaster broadcaster = broadcasterOptional1.get();
            broadcaster.setStatus(StreamStatus.OFFLINE);
            broadcaster.setDisplayName(event.getBroadcasterUserName());
            broadcasterRepository.save(broadcaster);
        }
    }

    private void streamOnline(WebhookContentStreamOnlineEvent event) {
        logger.info("Broadcaster " + event.getBroadcasterUserID() + " went online");

        final Optional<Broadcaster> broadcasterOptional1 = broadcasterRepository.findById(event.getBroadcasterUserID());
        if (broadcasterOptional1.isPresent()) {
            final Broadcaster broadcaster = broadcasterOptional1.get();
            broadcaster.setStatus(StreamStatus.ONLINE);
            broadcaster.setDisplayName(event.getBroadcasterUserName());
            broadcasterRepository.save(broadcaster);
        }
    }

    private void userUpdate(WebhookContentUserUpdateEvent event) {
        logger.info("Broadcaster " + event.getUserID() + " has updated!");

        final Optional<Broadcaster> broadcasterOptional1 = broadcasterRepository.findById(event.getUserID());
        if (broadcasterOptional1.isPresent()) {
            final Broadcaster broadcaster = broadcasterOptional1.get();
            broadcaster.setStatus(StreamStatus.ONLINE);
            broadcaster.setDisplayName(event.getUserName());
            broadcasterRepository.save(broadcaster);
        }
    }
}
