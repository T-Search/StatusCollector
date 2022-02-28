package de.tsearch.statuscollector.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.tsearch.statuscollector.service.twitch.entity.webhook.Subscription;
import lombok.Data;

@Data
public class WebhookContent {
    @JsonProperty("subscription")
    private Subscription subscription;
    @JsonProperty("event")
    private JsonNode event;

    @JsonProperty(value = "challenge", required = false)
    private String challenge;

}
