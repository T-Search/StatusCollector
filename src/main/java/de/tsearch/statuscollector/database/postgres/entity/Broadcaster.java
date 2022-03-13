package de.tsearch.statuscollector.database.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.UUID;

@RedisHash("common.broadcaster")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Broadcaster {
    @Id
    private long id;
    private String displayName;
    private StreamStatus status;
    private UUID twitchWebhookSecret;
}
