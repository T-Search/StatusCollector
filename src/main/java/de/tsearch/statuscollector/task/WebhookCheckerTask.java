package de.tsearch.statuscollector.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.tsearch.statuscollector.database.postgres.entity.Broadcaster;
import de.tsearch.statuscollector.database.postgres.repository.BroadcasterRepository;
import de.tsearch.statuscollector.service.twitch.WebhookService;
import de.tsearch.statuscollector.service.twitch.entity.EventEnum;
import de.tsearch.statuscollector.service.twitch.entity.webhook.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class WebhookCheckerTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    //TODO Check stream status from inactive webhooks
    private static final Set<EventEnum> subscriptionTypes = new HashSet<>();

    static {
        subscriptionTypes.add(EventEnum.STREAM_ONLINE);
        subscriptionTypes.add(EventEnum.STREAM_OFFLINE);
        subscriptionTypes.add(EventEnum.USER_UPDATE);
    }

    private final WebhookService webhookService;
    private final BroadcasterRepository broadcasterRepository;
    private final String webhookHost;

    public WebhookCheckerTask(WebhookService webhookService,
                              BroadcasterRepository broadcasterRepository,
                              @Value("${webhook.host}") String webhookHost) {
        this.webhookService = webhookService;
        this.broadcasterRepository = broadcasterRepository;
        this.webhookHost = webhookHost;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 10 * 1000)
    protected void checkWebhooks() {
        LOGGER.info("Check webhooks");
        final List<Subscription> allSubscriptions = webhookService.getAllSubscriptions()
                //Beachte nur Webhooks mit dem gleichen Host
                .stream().filter(subscription -> subscription.getTransport().getCallback().startsWith(webhookHost))
                .collect(Collectors.toList());

        //Liste nach nicht funktionsfähigen Subscriptions filtern und löschen
        final Set<UUID> subscriptionsToDelete = new HashSet<>();
        for (Subscription subscription : allSubscriptions) {
            if (subscription.getStatus().equals("enabled") || subscription.getStatus().equals("webhook_callback_verification_pending")) {
                continue;
            }
            //Subscription ist nicht mehr in einem funktionsfähigen Status
            LOGGER.info("Subscription " + subscription.getId() + " is not working. Status: " + subscription.getStatus());
            subscriptionsToDelete.add(subscription.getId());
        }
        subscriptionsToDelete.forEach(webhookService::deleteWebhook);

        for (Broadcaster broadcaster : broadcasterRepository.findAll()) {
            for (EventEnum subscriptionType : subscriptionTypes) {
                final Optional<Subscription> subscriptionOptional = allSubscriptions.stream()
                        .filter(subscription -> subscription.getType().equals(subscriptionType.getWebhookEventType()))
                        .filter(subscription -> String.valueOf(broadcaster.getId()).equals(subscription.getCondition().getBroadcasterUserID()) ||
                                String.valueOf(broadcaster.getId()).equals(subscription.getCondition().getUserId()))
                        .findAny();

                if (subscriptionOptional.isPresent()) {
                    //Broadcaster hat eine aktive Webhook
                    allSubscriptions.remove(subscriptionOptional.get());
                } else {
                    //Broadcaster hat keine aktive Webhook
                    LOGGER.info("Broadcaster " + broadcaster.getId() + " has no subscription for type " + subscriptionType);
                    try {
                        webhookService.requestNewWebhook(broadcaster.getId(), subscriptionType, getOrGenerateNewSecret(broadcaster).toString());
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Cannot create webhooks for broadcaster " + broadcaster.getId() + " for " + subscriptionType, e);
                    }
                }
            }
        }

        //Alle noch übrig gebliebenen Webhooks werden nicht mehr benötigt -> Löschen
        allSubscriptions.forEach(subscription -> webhookService.deleteWebhook(subscription.getId()));
        LOGGER.info("Webhook check completed");
    }

    private UUID getOrGenerateNewSecret(Broadcaster broadcaster) {
        if (broadcaster.getTwitchWebhookSecret() != null) {
            return broadcaster.getTwitchWebhookSecret();
        } else {
            UUID uuid = UUID.randomUUID();
            broadcaster.setTwitchWebhookSecret(uuid);
            broadcasterRepository.save(broadcaster);
            return uuid;
        }
    }
}
