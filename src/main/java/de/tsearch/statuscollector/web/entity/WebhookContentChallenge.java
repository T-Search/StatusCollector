package de.tsearch.statuscollector.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.tsearch.tclient.http.respone.webhook.Subscription;
import lombok.Data;

@Data
public class WebhookContentChallenge {
    @JsonProperty(value = "subscription", required = true)
    private Subscription subscription;

    @JsonProperty(value = "challenge", required = true)
    private String challenge;
}
