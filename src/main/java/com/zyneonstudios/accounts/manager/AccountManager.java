package com.zyneonstudios.accounts.manager;

import co.plocki.neoguard.client.interfaces.NeoArray;
import co.plocki.neoguard.client.interfaces.NeoThread;
import co.plocki.neoguard.client.post.NeoPost;
import co.plocki.neoguard.client.request.NeoRequest;
import co.plocki.neoguard.client.request.NeoResponse;
import com.zyneonstudios.accounts.account.Account;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class AccountManager {

    // Method to create a new account
    public void createAccount(String username, String password) throws Exception {
        NeoThread thread = new NeoThread("accounts");

        JSONObject obj = new JSONObject();
        obj.put("username", username);
        obj.put("password", password);
        obj.put("creation_timestamp", System.currentTimeMillis());
        obj.put("uuid", UUID.randomUUID().toString());

        NeoArray userArray = new NeoArray(username);

        NeoPost create = new NeoPost(thread, List.of(userArray), List.of(obj));
        create.post();
    }

    // Method to retrieve an account by username
    public Account getAccount(String username) throws Exception {
        NeoThread thread = new NeoThread("accounts");

        NeoRequest request = new NeoRequest(thread, List.of(new NeoArray(username)));
        NeoResponse response = request.request();

        List<Object> objects = response.getArrayObjects(new NeoArray(username));

        if (!objects.isEmpty()) {
            JSONObject jsonObject = (JSONObject) objects.get(0);

            return new Account(username, jsonObject.getString("password"),
                    jsonObject.getLong("creation_timestamp"), jsonObject.getString("uuid"));
        }

        return null;
    }

    // Method to update an account
    public boolean updateAccount(Account account, Account newAccount) throws Exception {
        NeoThread accounts = new NeoThread("accounts");
        NeoArray userArray = new NeoArray(account.getUsername());

        NeoRequest request = new NeoRequest(accounts, List.of(userArray));
        NeoResponse response = request.request();

        List<Object> objects = response.getArrayObjects(userArray);

        if (!objects.isEmpty()) {
            JSONObject jsonObject = (JSONObject) objects.get(0);

            if (jsonObject.getString("uuid").equalsIgnoreCase(account.getUuid())) {
                if (Objects.equals(account.getUuid(), newAccount.getUuid())) {

                    List<JSONObject> objs = new ArrayList<>();
                    objects.forEach(o -> objs.add((JSONObject) o));

                    objs.remove(jsonObject);

                    JSONObject obj = new JSONObject();
                    obj.put("username", newAccount.getUsername());
                    obj.put("password", newAccount.getPassword());
                    obj.put("creation_timestamp", account.getCreation_timestamp());
                    obj.put("uuid", account.getUuid());

                    objs.add(obj);

                    if(!account.getUsername().equalsIgnoreCase(newAccount.getUsername())) {
                        userArray.deleteArray(accounts.getName(), account.getUsername());

                        userArray = new NeoArray(newAccount.getUsername());
                        new NeoPost(accounts, List.of(userArray), List.of(objs));
                    } else {
                        NeoPost post = new NeoPost(accounts, List.of(userArray), objs);
                        return post.post();
                    }
                }
            }
        }
        return false;
    }

    // Method to delete an account
    public void deleteAccount(String username) throws Exception {
        NeoThread accounts = new NeoThread("accounts");
        NeoArray userArray = new NeoArray(username);

        NeoRequest request = new NeoRequest(accounts, List.of(userArray));
        NeoResponse response = request.request();

        List<Object> objects = response.getArrayObjects(userArray);

        if (!objects.isEmpty()) {
            JSONObject jsonObject = (JSONObject) objects.get(0);

            if (jsonObject.getString("username").equalsIgnoreCase(username)) {
                userArray.deleteArray(accounts.getName(), username);
            }
        }
    }

    // Method to change username
    public boolean changeUsername(String oldUsername, String newUsername) throws Exception {
        NeoThread accounts = new NeoThread("accounts");
        NeoArray oldUserArray = new NeoArray(oldUsername);
        NeoArray newUserArray = new NeoArray(newUsername);

        NeoRequest request = new NeoRequest(accounts, List.of(oldUserArray));
        NeoResponse response = request.request();

        List<Object> objects = response.getArrayObjects(oldUserArray);

        if (!objects.isEmpty()) {
            List<JSONObject> objs = new ArrayList<>();
            objects.forEach(o -> objs.add((JSONObject) o));

            NeoPost post = new NeoPost(accounts, List.of(newUserArray), objs);
            post.post();

            return oldUserArray.deleteArray(accounts.getName(), oldUsername);
        }

        return false;
    }

    // Method to add user-specific data
    public boolean addUserData(String username, String dataKey, JSONObject data) throws Exception {
        NeoThread accounts = new NeoThread("accounts");
        NeoArray userArray = new NeoArray(username);

        NeoRequest request = new NeoRequest(accounts, List.of(userArray));
        NeoResponse response = request.request();

        List<Object> objects = response.getArrayObjects(userArray);

        if (!objects.isEmpty()) {
            JSONObject jsonObject = (JSONObject) objects.get(0);
            if(jsonObject.has("accountDataStorage")) {
                JSONObject dataS = jsonObject.getJSONObject("accountDataStorage");
                dataS.put(dataKey, data);
            } else {

                JSONObject o = new JSONObject();
                o.put(dataKey, data);

                jsonObject.put("accountDataStorage", o);
            }

            NeoPost post = new NeoPost(accounts, List.of(userArray), objects);
            return post.post();
        }

        return false;
    }

    // Method to get user-specific data for a given username
    public JSONObject getUserData(String username, String dataKey) throws Exception {
        NeoThread accounts = new NeoThread("accounts");
        NeoArray userArray = new NeoArray(username);

        NeoRequest request = new NeoRequest(accounts, List.of(userArray));
        NeoResponse response = request.request();

        List<Object> objects = response.getArrayObjects(userArray);

        if(dataKey.equalsIgnoreCase("*")) {
            JSONObject jsonObject = (JSONObject) objects.get(0);
            return jsonObject.has("accountDataStorage") ? jsonObject.getJSONObject("accountDataStorage") : null;
        }

        if (!objects.isEmpty()) {
            JSONObject jsonObject = (JSONObject) objects.get(0);
            return jsonObject.has("accountDataStorage") ? Objects.requireNonNullElse(jsonObject.getJSONObject("accountDataStorage").getJSONObject(dataKey), null) : null;
        }

        return null;
    }

    // Method to update user-specific data for a given username and data key
    public boolean updateUserData(String username, String dataKey, JSONObject updatedData) throws Exception {
        NeoThread accounts = new NeoThread("accounts");
        NeoArray userArray = new NeoArray(username);

        NeoRequest request = new NeoRequest(accounts, List.of(userArray));
        NeoResponse response = request.request();

        List<Object> objects = response.getArrayObjects(userArray);

        if (!objects.isEmpty()) {
            JSONObject jsonObject = (JSONObject) objects.get(0);

            if (jsonObject.has("accountDataStorage")) {
                JSONObject dataStorage = jsonObject.getJSONObject("accountDataStorage");

                if (dataStorage.has(dataKey)) {
                    dataStorage.put(dataKey, updatedData);

                    NeoPost post = new NeoPost(accounts, List.of(userArray), objects);
                    return post.post();
                }
            }
        }

        return false;
    }

    public boolean updatePassword(String username, String newPassword) throws Exception {
        NeoThread accounts = new NeoThread("accounts");
        NeoArray userArray = new NeoArray(username);

        NeoRequest request = new NeoRequest(accounts, List.of(userArray));
        NeoResponse response = request.request();

        List<Object> objects = response.getArrayObjects(userArray);

        if (!objects.isEmpty()) {
            JSONObject jsonObject = (JSONObject) objects.get(0);

            jsonObject.put("password", newPassword);

            NeoPost post = new NeoPost(accounts, List.of(userArray), List.of(jsonObject));
            post.post();
        }

        return false;
    }

    // Method to delete user-specific data for a given username and data key
    public boolean deleteUserData(String username, String dataKey) throws Exception {
        NeoThread accounts = new NeoThread("accounts");
        NeoArray userArray = new NeoArray(username);

        NeoRequest request = new NeoRequest(accounts, List.of(userArray));
        NeoResponse response = request.request();

        List<Object> objects = response.getArrayObjects(userArray);

        if (!objects.isEmpty()) {
            JSONObject jsonObject = (JSONObject) objects.get(0);

            if (jsonObject.has("accountDataStorage")) {
                JSONObject dataStorage = jsonObject.getJSONObject("accountDataStorage");

                if (dataStorage.has(dataKey)) {
                    dataStorage.remove(dataKey);

                    NeoPost post = new NeoPost(accounts, List.of(userArray), objects);
                    return post.post();
                }
            }
        }

        return false;
    }

    public boolean hasUserData(String username, String dataKey) throws Exception {
        NeoThread accounts = new NeoThread("accounts");
        NeoArray userArray = new NeoArray(username);

        NeoRequest request = new NeoRequest(accounts, List.of(userArray));
        NeoResponse response = request.request();

        List<Object> objects = response.getArrayObjects(userArray);

        if (!objects.isEmpty()) {
            JSONObject jsonObject = (JSONObject) objects.get(0);

            if (jsonObject.has("accountDataStorage")) {
                JSONObject dataStorage = jsonObject.getJSONObject("accountDataStorage");

                return dataStorage.has(dataKey);
            }
        }

        return false;
    }

}
