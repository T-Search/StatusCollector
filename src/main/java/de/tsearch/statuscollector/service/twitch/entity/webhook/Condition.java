package de.tsearch.statuscollector.service.twitch.entity.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Condition {
    @JsonProperty("broadcaster_user_id")
    private String broadcasterUserID;
}
