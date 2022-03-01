package de.tsearch.statuscollector.service.twitch.entity;

import de.tsearch.statuscollector.web.entity.WebhookContentStreamOfflineEvent;
import de.tsearch.statuscollector.web.entity.WebhookContentStreamOnlineEvent;
import lombok.Getter;

@Getter
public enum EventEnum {
    STREAM_OFFLINE("stream.offline", WebhookContentStreamOfflineEvent.class),
    STREAM_ONLINE("stream.online", WebhookContentStreamOnlineEvent.class);

    private final Class contentClass;
    private final String webhookEventType;

    EventEnum(String webhookEventType, Class contentClass) {
        this.webhookEventType = webhookEventType;
        this.contentClass = contentClass;
    }

    public static EventEnum getByWebhookEventType(String type) {
        for (EventEnum value : EventEnum.values()) {
            if (value.webhookEventType.equals(type)) return value;
        }

        return null;
    }
}
