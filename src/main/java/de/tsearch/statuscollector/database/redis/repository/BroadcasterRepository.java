package de.tsearch.statuscollector.database.redis.repository;

import de.tsearch.statuscollector.database.redis.entity.Broadcaster;
import org.springframework.data.repository.CrudRepository;

public interface BroadcasterRepository extends CrudRepository<Broadcaster, Long> {
}
