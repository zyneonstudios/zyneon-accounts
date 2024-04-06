package com.zyneonstudios.accounts.manager;

import com.zyneonstudios.accounts.AccountSystem;
import com.zyneonstudios.accounts.account.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;

import java.util.UUID;

public class AccountManager {

    public AccountManager() {
    }

    public void createAccount(String username, String password) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "INSERT INTO accounts (username, password, creation_timestamp, uuid) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.setString(2, password);
                statement.setLong(3, System.currentTimeMillis());
                statement.setString(4, UUID.randomUUID().toString());
                statement.executeUpdate();
            }
        }
    }

    public Account getAccount(String username) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "SELECT * FROM accounts WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new Account(username, resultSet.getString("password"),
                                resultSet.getLong("creation_timestamp"), resultSet.getString("uuid"));
                    }
                }
            }
        }
        return null;
    }

    public boolean updateAccount(Account account, Account newAccount) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "UPDATE accounts SET username = ?, password = ? WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newAccount.getUsername());
                statement.setString(2, newAccount.getPassword());
                statement.setString(3, account.getUsername());
                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0;
            }
        }
    }

    public void deleteAccount(String username) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "DELETE FROM accounts WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.executeUpdate();
            }
        }
    }

    public boolean changeUsername(String oldUsername, String newUsername) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String updateSql = "UPDATE accounts SET username = ? WHERE username = ?";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setString(1, newUsername);
                updateStatement.setString(2, oldUsername);
                int rowsAffected = updateStatement.executeUpdate();
                if (rowsAffected > 0) {
                    // Update successful, delete old entry
                    String deleteSql = "DELETE FROM accounts WHERE username = ?";
                    try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                        deleteStatement.setString(1, oldUsername);
                        return deleteStatement.executeUpdate() > 0;
                    }
                }
            }
        }
        return false;
    }

    public boolean addUserData(String username, String dataKey, String data) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String selectSql = "SELECT accountDataStorage FROM accounts WHERE username = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
                selectStatement.setString(1, username);
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Account exists, update data
                        String accountDataStorage = resultSet.getString("accountDataStorage");
                        if (accountDataStorage != null) {
                            // Append data to existing accountDataStorage
                            accountDataStorage += "," + dataKey + ":" + data;
                        } else {
                            // No existing accountDataStorage, create new one
                            accountDataStorage = "{" + dataKey + ":" + data + "}";
                        }
                        String updateSql = "UPDATE accounts SET accountDataStorage = ? WHERE username = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                            updateStatement.setString(1, accountDataStorage);
                            updateStatement.setString(2, username);
                            return updateStatement.executeUpdate() > 0;
                        }
                    }
                }
            }
        }
        return false;
    }

    public JSONObject getUserData(String username, String dataKey) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "SELECT accountDataStorage FROM accounts WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        JSONObject accountDataStorage = new JSONObject(resultSet.getString("accountDataStorage"));
                        if (dataKey.equals("*")) {
                            return accountDataStorage;
                        } else {
                            return accountDataStorage.optJSONObject(dataKey);
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean updateUserData(String username, String dataKey, JSONObject updatedData) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "SELECT accountDataStorage FROM accounts WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        JSONObject accountDataStorage = new JSONObject(resultSet.getString("accountDataStorage"));
                        accountDataStorage.put(dataKey, updatedData);
                        String updateSql = "UPDATE accounts SET accountDataStorage = ? WHERE username = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                            updateStatement.setString(1, accountDataStorage.toString());
                            updateStatement.setString(2, username);
                            return updateStatement.executeUpdate() > 0;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean updatePassword(String username, String newPassword) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "UPDATE accounts SET password = ? WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newPassword);
                statement.setString(2, username);
                return statement.executeUpdate() > 0;
            }
        }
    }

    public boolean deleteUserData(String username, String dataKey) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "SELECT accountDataStorage FROM accounts WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        JSONObject accountDataStorage = new JSONObject(resultSet.getString("accountDataStorage"));
                        if (accountDataStorage.has(dataKey)) {
                            accountDataStorage.remove(dataKey);
                            String updateSql = "UPDATE accounts SET accountDataStorage = ? WHERE username = ?";
                            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                                updateStatement.setString(1, accountDataStorage.toString());
                                updateStatement.setString(2, username);
                                return updateStatement.executeUpdate() > 0;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasUserData(String username, String dataKey) throws SQLException {
        try (Connection connection = AccountSystem.getDriver().getHikariDataSource().getConnection()) {
            String sql = "SELECT accountDataStorage FROM accounts WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        JSONObject accountDataStorage = new JSONObject(resultSet.getString("accountDataStorage"));
                        return accountDataStorage.has(dataKey);
                    }
                }
            }
        }
        return false;
    }

}
