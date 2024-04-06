package com.zyneonstudios.accounts.token;

import com.zyneonstudios.accounts.AccountSystem;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class AuthenticationToken {
    private String username;
    private long creationTimestamp;
    private String tokenValue;
    private boolean isTemporary;

    public AuthenticationToken(String username, String tokenValue, boolean isTemporary) {
        this.username = username;
        this.creationTimestamp = System.currentTimeMillis();
        this.tokenValue = new String(Base64.getEncoder().encode(tokenValue.getBytes()));
        this.isTemporary = isTemporary;
    }

    public AuthenticationToken(String username, String tokenValue, long creationTimestamp, boolean isTemporary) {
        this.username = username;
        this.creationTimestamp = creationTimestamp;
        this.tokenValue = new String(Base64.getEncoder().encode(tokenValue.getBytes()));
        this.isTemporary = isTemporary;
    }

    public AuthenticationToken() {}

    public String getUsername() {
        return username;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    public void saveToken() throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "INSERT INTO auth_tokens (username, creationTimestamp, tokenValue, isTemporary) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, this.username);
                statement.setLong(2, this.creationTimestamp);
                statement.setString(3, this.tokenValue);
                statement.setBoolean(4, this.isTemporary);
                statement.executeUpdate();
            }
        }
    }

    public void deleteToken() throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "DELETE FROM auth_tokens WHERE tokenValue = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, this.tokenValue);
                statement.executeUpdate();
            }
        }
    }

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("username", username);
        obj.put("creationTimestamp", creationTimestamp);
        obj.put("tokenValue", tokenValue);
        obj.put("isTemporary", isTemporary);
        return obj;
    }

    public static AuthenticationToken fromJSONObject(JSONObject jsonObject) {
        String username = jsonObject.getString("username");
        long creationTimestamp = jsonObject.getLong("creationTimestamp");
        String tokenValue = jsonObject.getString("tokenValue");
        boolean isTemporary = jsonObject.getBoolean("isTemporary");
        return new AuthenticationToken(username, tokenValue, creationTimestamp, isTemporary);
    }

    public static AuthenticationToken getAuthenticationToken(String token) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "SELECT * FROM auth_tokens WHERE tokenValue = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, token);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String username = resultSet.getString("username");
                        long creationTimestamp = resultSet.getLong("creationTimestamp");
                        String tokenValue = resultSet.getString("tokenValue");
                        boolean isTemporary = resultSet.getBoolean("isTemporary");
                        return new AuthenticationToken(username, tokenValue, creationTimestamp, isTemporary);
                    }
                }
            }
        }
        return null;
    }
}
