package de.tsearch.statuscollector.service.twitch.entity.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Transport {
    @JsonProperty("method")
    private String method;
    @JsonProperty("callback")
    private String callback;
    @JsonProperty("secret")
    private String secret;
}
