package de.tsearch.statuscollector.database.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwitchMessageId {

    @Id
    private String id;

    @Column
    private byte tries;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date incomeTime;

    public TwitchMessageId(String id, byte tries) {
        this.id = id;
        this.tries = tries;
    }
}
