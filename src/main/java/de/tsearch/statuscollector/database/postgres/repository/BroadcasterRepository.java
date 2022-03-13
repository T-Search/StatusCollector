package de.tsearch.statuscollector.database.postgres.repository;

import de.tsearch.statuscollector.database.postgres.entity.Broadcaster;
import org.springframework.data.repository.CrudRepository;

public interface BroadcasterRepository extends CrudRepository<Broadcaster, Long> {
}
