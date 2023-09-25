package com.zyneonstudios.accounts.account;

public class Account {

    private final String username;
    private final String password;
    private final long creation_timestamp;
    private final String uuid;

    public Account(String username, String password, long creation_timestamp, String uuid) {
        this.username = username;
        this.password = password;
        this.creation_timestamp = creation_timestamp;
        this.uuid = uuid;
    }

    public long getCreation_timestamp() {
        return creation_timestamp;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

}
