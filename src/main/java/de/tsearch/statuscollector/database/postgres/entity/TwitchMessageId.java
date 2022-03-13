package de.tsearch.statuscollector.database.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwitchMessageId {

    @Id
    private String id;

    @TimeToLive
    private int timeout = 60;

    public TwitchMessageId(String id) {
        this.id = id;
    }
}
