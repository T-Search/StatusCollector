package de.tsearch.statuscollector.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class WebhookContentStreamOnlineEvent {
    @JsonProperty("id")
    private String id;
    @JsonProperty("broadcaster_user_id")
    private long broadcasterUserID;
    @JsonProperty("broadcaster_user_login")
    private String broadcasterUserLogin;
    @JsonProperty("broadcaster_user_name")
    private String broadcasterUserName;
    @JsonProperty("type")
    private String type;
    @JsonProperty("started_at")
    private Date startedAt;
}
