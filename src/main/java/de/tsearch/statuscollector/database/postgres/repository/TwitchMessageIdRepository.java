package de.tsearch.statuscollector.database.postgres.repository;

import de.tsearch.statuscollector.database.postgres.entity.TwitchMessageId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwitchMessageIdRepository extends CrudRepository<TwitchMessageId, String> {
}
