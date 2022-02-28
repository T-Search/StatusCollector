package de.tsearch.statuscollector.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebhookContentStreamOfflineEvent {
    @JsonProperty("broadcaster_user_id")
    private long broadcasterUserID;
    @JsonProperty("broadcaster_user_login")
    private String broadcasterUserLogin;
    @JsonProperty("broadcaster_user_name")
    private String broadcasterUserName;
}
