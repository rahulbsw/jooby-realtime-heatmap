package org.jooby.sse.heatmap.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jooby.sse.heatmap.domain.Location;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An in-memory database.
 *
 * @author rjain
 */
@Singleton
public class DB {

    private Cache<Integer, Location> db = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build();

    public List<Location> findAll(final int start, final int max) {
        return db.asMap().values().stream()
                .sorted((p1, p2) -> p1.getId() - p2.getId())
                .collect(Collectors.toList());
    }

    public Location find(final int id) {
        return db.getIfPresent(id);
    }

    public void save(final Location location) {
        db.put(location.getId(), location);
    }

    public void save(final int id, final String name) {

        this.save(Location.generateRandomLocation(id, name));
    }

    public void delete(final int id) {
        db.invalidate(id);
    }

}
