package de.tsearch.statuscollector.service.twitch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class Stream {
    @JsonProperty("id")
    private String id;
    @JsonProperty("user_id")
    private Long userID;
    @JsonProperty("user_login")
    private String userLogin;
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("game_id")
    private String gameID;
    @JsonProperty("game_name")
    private String gameName;
    @JsonProperty("type")
    private String type;
    @JsonProperty("title")
    private String title;
    @JsonProperty("viewer_count")
    private long viewerCount;
    @JsonProperty("started_at")
    private Date startedAt;
    @JsonProperty("language")
    private String language;
    @JsonProperty("thumbnail_url")
    private String thumbnailURL;
    @JsonProperty("is_mature")
    private boolean mature;
}
