package de.tsearch.statuscollector.task;

import de.tsearch.statuscollector.database.postgres.entity.Broadcaster;
import de.tsearch.statuscollector.database.postgres.repository.BroadcasterRepository;
import de.tsearch.tclient.WebhookClient;
import de.tsearch.tclient.data.EventEnum;
import de.tsearch.tclient.http.respone.webhook.Subscription;
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

    private final WebhookClient webhookClient;
    private final BroadcasterRepository broadcasterRepository;
    private final String webhookHost;

    public WebhookCheckerTask(WebhookClient webhookClient,
                              BroadcasterRepository broadcasterRepository,
                              @Value("${webhook.host}") String webhookHost) {
        this.webhookClient = webhookClient;
        this.broadcasterRepository = broadcasterRepository;
        this.webhookHost = webhookHost;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 10 * 1000)
    protected void checkWebhooks() {
        LOGGER.info("Check webhooks");
        final List<Subscription> allSubscriptions = webhookClient.getAllSubscriptions()
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
        subscriptionsToDelete.forEach(webhookClient::deleteWebhook);

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
                    webhookClient.requestNewWebhook(broadcaster.getId(), subscriptionType, getOrGenerateNewSecret(broadcaster).toString(), webhookHost + "/webhook/" + broadcaster.getId());
                }
            }
        }

        //Alle noch übrig gebliebenen Webhooks werden nicht mehr benötigt -> Löschen
        allSubscriptions.forEach(subscription -> webhookClient.deleteWebhook(subscription.getId()));
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
