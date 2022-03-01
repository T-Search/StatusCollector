package de.tsearch.statuscollector.database.redis.repository;

import de.tsearch.statuscollector.database.redis.entity.TwitchMessageId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwitchMessageIdRepository extends CrudRepository<TwitchMessageId, String> {
}
