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

    /**
     * Has user authorised the application.
     * Webhook costs = 0
     */
    @Column(nullable = false)
    private boolean twitchAuthorised;

    /**
     * If the user is to be observed even without authorization.
     * Webhook costs = 1
     */
    @Column(nullable = false)
    private boolean vip;
}
