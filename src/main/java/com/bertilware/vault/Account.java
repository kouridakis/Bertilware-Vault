package com.bertilware.vault;

import java.io.Serializable;

public class Account implements Serializable {
    private String service;
    private String username;
    private String password;
    private long creationTime;

    public Account(String service, String username, String password) {
        this.service = service;
        this.username = username;
        this.password = password;
        creationTime = System.currentTimeMillis();
    }

    // Getters
    public String getService() {
        return service;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public long getCreationTime() {
        return creationTime;
    }

    // Setters
    public void setService(String service) {
        this.service = service;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public boolean isNewerThan(Account other) {
        return creationTime > other.getCreationTime();
    }

    public boolean isAlternativeOf(Account other) {
        return service.equals(other.getService()) && username.equals(other.getUsername());
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", service, username);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Account)) {
            return false;
        }
        Account other = (Account) obj;
        return other.getService().equals(service) &&
                other.getUsername().equals(username) &&
                other.getPassword().equals(password);
    }
}
