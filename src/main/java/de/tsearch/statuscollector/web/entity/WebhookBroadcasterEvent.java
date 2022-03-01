package de.tsearch.statuscollector.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public abstract class WebhookBroadcasterEvent extends WebhookEvent {
    @JsonProperty("broadcaster_user_id")
    private long broadcasterUserID;
}
