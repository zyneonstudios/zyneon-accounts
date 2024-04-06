package com.zyneonstudios.accounts.token;

import com.zyneonstudios.accounts.AccountSystem;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Token {

    private final String tokenValue;
    private final String username;
    private final long creationTimestamp;
    private final boolean isAdminToken;  // Indicates if it's an admin token

    public Token(String username, String tokenValue, boolean isAdminToken) {
        this.tokenValue = tokenValue;
        this.username = username;
        this.creationTimestamp = System.currentTimeMillis();
        this.isAdminToken = isAdminToken;
    }

    // Getters for Token class

    public String getTokenValue() {
        return tokenValue;
    }

    public String getUsername() {
        return username;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public boolean isAdminToken() {
        return isAdminToken;
    }

    // Save the token to the database
    public String saveToken() throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "INSERT INTO tokens (tokenValue, username, creationTimestamp, isAdminToken) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, this.tokenValue);
                statement.setString(2, this.username);
                statement.setLong(3, this.creationTimestamp);
                statement.setBoolean(4, this.isAdminToken);
                statement.executeUpdate();
            }
        }
        return tokenValue;
    }

    // Delete the token from the database
    public void deleteToken() throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "DELETE FROM tokens WHERE tokenValue = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, this.tokenValue);
                statement.executeUpdate();
            }
        }
    }

    // Additional methods for Token class
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("tokenValue", tokenValue);
        obj.put("username", username);
        obj.put("creationTimestamp", creationTimestamp);
        obj.put("isAdminToken", isAdminToken);
        return obj;
    }

    public static Token fromJSONObject(JSONObject jsonObject) {
        String tokenValue = jsonObject.getString("tokenValue");
        String username = jsonObject.getString("username");
        long creationTimestamp = jsonObject.getLong("creationTimestamp");
        boolean isAdminToken = jsonObject.getBoolean("isAdminToken");
        return new Token(username, tokenValue, isAdminToken);
    }

    public static Token getApplicationToken(String tokenValue) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "SELECT * FROM tokens WHERE tokenValue = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, tokenValue);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String username = resultSet.getString("username");
                        long creationTimestamp = resultSet.getLong("creationTimestamp");
                        boolean isAdminToken = resultSet.getBoolean("isAdminToken");
                        return new Token(username, tokenValue, isAdminToken);
                    }
                }
            }
        }
        return null;
    }

    // Other methods and functionalities specific to Token can be added here
}
