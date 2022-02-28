package de.tsearch.statuscollector.service.twitch.entity.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class Subscription {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("version")
    private String version;
    @JsonProperty("status")
    private String status;
    @JsonProperty("cost")
    private long cost;
    @JsonProperty("condition")
    private Condition condition;
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("transport")
    private Transport transport;
}
