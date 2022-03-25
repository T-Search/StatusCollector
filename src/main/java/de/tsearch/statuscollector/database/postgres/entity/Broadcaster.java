package de.tsearch.statuscollector.database.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Broadcaster {
    @Id
    private long id;
    @Column
    private String displayName;
    @Enumerated(EnumType.STRING)
    private StreamStatus status;
    @Column
    private UUID twitchWebhookSecret;
}
