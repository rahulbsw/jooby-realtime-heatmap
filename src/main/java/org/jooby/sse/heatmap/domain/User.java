package org.jooby.sse.heatmap.domain;


/**
 * Created by rjain on 6/27/17.
 */
public class User {
    private String username;
    private String passwordHash;

    public User(String username, String password) {
        this.username = username.toUpperCase();
        this.passwordHash = (password);

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toUpperCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }


}
