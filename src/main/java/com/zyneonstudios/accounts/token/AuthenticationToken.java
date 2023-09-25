package com.zyneonstudios.accounts.token;

import co.plocki.neoguard.client.interfaces.NeoArray;
import co.plocki.neoguard.client.interfaces.NeoThread;
import co.plocki.neoguard.client.post.NeoPost;
import co.plocki.neoguard.client.request.NeoRequest;
import co.plocki.neoguard.client.request.NeoResponse;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class AuthenticationToken {

    private String username;
    private long creationTimestamp;
    private String tokenValue;
    private boolean isTemporary;

    public AuthenticationToken(String username, String tokenValue, boolean isTemporary) {
        this.username = username;
        this.creationTimestamp = System.currentTimeMillis();
        this.tokenValue = new String(Base64.getEncoder().encode(tokenValue.getBytes(StandardCharsets.UTF_8)));
        this.isTemporary = isTemporary;
    }

    public AuthenticationToken(String username, String tokenValue, long creationTimestamp, boolean isTemporary) {
        this.username = username;
        this.creationTimestamp = creationTimestamp;
        this.tokenValue = new String(Base64.getEncoder().encode(tokenValue.getBytes(StandardCharsets.UTF_8)));
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

    public void saveToken() throws Exception {
        NeoThread thread = new NeoThread("auth_tokens");
        NeoArray tokenArray = new NeoArray(tokenValue);

        JSONObject obj = new JSONObject();
        obj.put("username", this.username);
        obj.put("creationTimestamp", this.creationTimestamp);
        obj.put("tokenValue", this.tokenValue);
        obj.put("isTemporary", this.isTemporary);

        NeoPost create = new NeoPost(thread, List.of(tokenArray), List.of(obj));
        create.post();
    }

    public void deleteToken() throws Exception {
        NeoThread thread = new NeoThread("auth_tokens");
        NeoArray tokenArray = new NeoArray(tokenValue);

        tokenArray.deleteArray(thread.getName(), tokenValue);
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

    public static AuthenticationToken getAuthenticationToken(String token) {
        try {
            // Retrieve the authentication token from the "auth_tokens" thread in the Neo database
            NeoThread thread = new NeoThread("auth_tokens");
            NeoArray tokenArray = new NeoArray(token);

            NeoRequest request = new NeoRequest(thread, List.of(tokenArray));
            NeoResponse response = request.request();

            List<Object> objects = response.getArrayObjects(tokenArray);

            if (!objects.isEmpty()) {
                JSONObject jsonObject = (JSONObject) objects.get(0);
                // Deserialize the JSON object into an AuthenticationToken
                return AuthenticationToken.fromJSONObject(jsonObject);
            }
        } catch (Exception e) {
            // Handle exceptions appropriately (e.g., log the error)
            e.printStackTrace();
        }

        return null;  // Return null if token not found or an error occurred
    }
}
