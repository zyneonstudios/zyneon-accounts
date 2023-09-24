package com.zyneonstudios.accounts.token;

import co.plocki.neoguard.client.interfaces.NeoArray;
import co.plocki.neoguard.client.interfaces.NeoThread;
import co.plocki.neoguard.client.post.NeoPost;
import co.plocki.neoguard.client.request.NeoRequest;
import co.plocki.neoguard.client.request.NeoResponse;
import org.json.JSONObject;

import java.util.Base64;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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

    // Save the token to the Neo database
    public void saveToken() throws Exception {
        NeoThread thread = new NeoThread("tokens");
        NeoArray tokenArray = new NeoArray(tokenValue);

        JSONObject obj = new JSONObject();
        obj.put("tokenValue", this.tokenValue);
        obj.put("username", this.username);
        obj.put("creationTimestamp", this.creationTimestamp);
        obj.put("isAdminToken", this.isAdminToken);

        NeoPost create = new NeoPost(thread, List.of(tokenArray), List.of(obj));
        create.post();
    }

    // Delete the token from the Neo database
    public void deleteToken() throws Exception {
        NeoThread thread = new NeoThread("tokens");
        NeoArray tokenArray = new NeoArray(tokenValue);

        tokenArray.deleteArray(thread.getName(), tokenValue);
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

    public static Token getApplicationToken(String tokenValue) {
        try {
            // Retrieve the token from the "tokens" thread in the Neo database
            NeoThread thread = new NeoThread("tokens");
            NeoArray tokenArray = new NeoArray(tokenValue);

            NeoRequest request = new NeoRequest(thread, List.of(tokenArray));
            NeoResponse response = request.request();

            List<Object> objects = response.getArrayObjects(tokenArray);

            if (!objects.isEmpty()) {
                JSONObject jsonObject = (JSONObject) objects.get(0);
                // Deserialize the JSON object into a Token
                return Token.fromJSONObject(jsonObject);
            }
        } catch (Exception e) {
            // Handle exceptions appropriately (e.g., log the error)
            e.printStackTrace();
        }

        return null;  // Return null if token not found or an error occurred
    }

    // Other methods and functionalities specific to Token can be added here
}
