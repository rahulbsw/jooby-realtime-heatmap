package org.jooby.sse.heatmap.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jooby.sse.heatmap.domain.User;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An in-memory database.
 *
 * @author rjain
 */
@Singleton
public class AuthDB {
    private final User EMPTY_USER = new User("----", "-----");

    private Cache<String, User> db = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build();

    public AuthDB() {
        try {
            this.save("rjain", "rjain");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<User> findAll(final int start, final int max) {
        return db.asMap().values().stream()
                .sorted((p1, p2) -> p1.getUsername().compareTo(p2.getUsername()))
                .collect(Collectors.toList());
    }

    public User findByUserPass(final String username, final String password) {
        User u = new User(username, password);
        Optional.ofNullable(find(username)).orElse(EMPTY_USER).equals(u);
        return u;
    }

    User find(final String username) {
        return db.getIfPresent(username.toUpperCase());
    }

    public void save(final User user) throws Exception {
        if (find(user.getUsername()) == null)
            db.put(user.getUsername(), user);
        else
            throw new Exception("User already Exists");
    }

    public void save(final String username, final String password) throws Exception {

        this.save(new User(username, password));
    }

    public void delete(final String username) {
        db.invalidate(username.toUpperCase());
    }

}
