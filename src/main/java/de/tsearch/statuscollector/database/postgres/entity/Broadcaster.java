package de.tsearch.statuscollector.database.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
    @Column
    private StreamStatus status;
    @Column
    private UUID twitchWebhookSecret;
}
