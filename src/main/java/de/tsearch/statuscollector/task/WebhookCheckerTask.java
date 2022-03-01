package de.tsearch.statuscollector.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.tsearch.statuscollector.database.redis.entity.Broadcaster;
import de.tsearch.statuscollector.database.redis.repository.BroadcasterRepository;
import de.tsearch.statuscollector.service.twitch.WebhookService;
import de.tsearch.statuscollector.service.twitch.entity.EventEnum;
import de.tsearch.statuscollector.service.twitch.entity.webhook.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Component
public class WebhookCheckerTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final WebhookService webhookService;
    private final BroadcasterRepository broadcasterRepository;

    public WebhookCheckerTask(WebhookService webhookService, BroadcasterRepository broadcasterRepository) {
        this.webhookService = webhookService;
        this.broadcasterRepository = broadcasterRepository;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 10 * 1000)
    protected void checkWebhooks() {
        LOGGER.info("Check webhooks");
        final List<Subscription> allSubscriptions = webhookService.getAllSubscriptions();

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
            Set<EventEnum> subscriptionTypes = new HashSet<>();
            subscriptionTypes.add(EventEnum.STREAM_ONLINE);
            subscriptionTypes.add(EventEnum.STREAM_OFFLINE);

            for (EventEnum subscriptionType : subscriptionTypes) {
                final Optional<Subscription> subscriptionOptional = allSubscriptions.stream()
                        .filter(subscription -> subscription.getType().equals(subscriptionType.getWebhookEventType()))
                        .filter(subscription -> subscription.getCondition().getBroadcasterUserID().equals(String.valueOf(broadcaster.getId())))
                        .findAny();

                if (subscriptionOptional.isPresent()) {
                    //Broadcaster hat eine aktive Webhook
                    allSubscriptions.remove(subscriptionOptional.get());
                } else {
                    //Broadcaster hat keine aktive Webhook
                    LOGGER.info("Broadcaster " + broadcaster.getId() + " has no subscription for type " + subscriptionType);
                    try {
                        webhookService.requestNewWebhook(broadcaster.getId(), subscriptionType, UUID.randomUUID().toString());
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Cannot create webhooks for broadcaster " + broadcaster.getId() + " for " + subscriptionType, e);
                    }
                }
            }
        }

        //Alle noch übrig gebliebenen Webhooks werden nicht mehr benötigt -> Löschen
        allSubscriptions.forEach(subscription -> webhookService.deleteWebhook(subscription.getId()));
    }
}
