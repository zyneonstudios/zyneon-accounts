package com.zyneonstudios.accounts;

import co.plocki.neoguard.client.NeoGuardClient;
import com.zyneonstudios.accounts.account.Account;
import com.zyneonstudios.accounts.manager.AccountManager;
import com.zyneonstudios.accounts.token.AuthenticationToken;
import com.zyneonstudios.accounts.token.Token;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

public class AccountSystem {

    private static final Logger logger = LogManager.getLogger(AccountSystem.class);

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        AccountSystem accountSystem = new AccountSystem();
        accountSystem.start();

        Runtime.getRuntime().addShutdownHook(new Thread(accountSystem::stop));
    }

    private Undertow undertow;

    private static final AccountManager accountManager = new AccountManager();
    private static final AuthenticationToken tokenManager = new AuthenticationToken();

    public void start() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        // Initialize NeoGuardClient and set up Undertow routes
        NeoGuardClient client = new NeoGuardClient();
        client.start();

        if (undertow == null) {
            undertow = Undertow.builder()
                    .setHandler(new RoutingHandler()
                            .post("/login", this::loginHandler)
                            .post("/logout", this::logoutHandler)
                            .post("/refresh", this::refreshTokenHandler)
                            .get("/api/application", this::applicationApiHandler)
                            .delete("/account/{username}", this::deleteAccountHandler)
                            .post("/account", this::createAccountHandler)
                            .get("/information", this::getAllInformationHandler)
                            .post("/account/updateData", this::changeUserDataHandler)).build();
        }

        undertow.start();
    }

    public void stop() {
        if(undertow != null) {
            undertow.stop();
            undertow = null;
        }
    }

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    public static String generateNewToken() {
        byte[] randomBytes = new byte[256];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    private void loginHandler(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((httpServerExchange, requestBody) -> {
            try {
                JSONObject requestJson = new JSONObject(requestBody);
                String username = requestJson.getString("username");
                String password = requestJson.getString("password");
                boolean requestNonExpiringToken = requestJson.optBoolean("nonExpiringToken", false);

                // Check if the account already exists
                Account account = accountManager.getAccount(username);

                if (account != null && password.equals(account.getPassword())) {
                    // Generate an authentication token
                    AuthenticationToken authToken = new AuthenticationToken(username, generateNewToken(), requestNonExpiringToken);
                    authToken.saveToken();

                    // Respond with the authentication token
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("authToken", authToken.getTokenValue());
                    respondWithJson(exchange, responseJson);
                } else {
                    respondWithError(exchange, "Invalid credentials", 401);
                }

            } catch (Exception e) {
                respondWithError(exchange, "Internal server error", 500);
                logger.error("Exception in loginHandler: " + e.getMessage());
            }
        });
    }


    private void logoutHandler(HttpServerExchange exchange) {
        // Extract the authentication token from the request body
        AtomicReference<String> authTokenRef = new AtomicReference<>(null);
        exchange.getRequestReceiver().receiveFullString((httpServerExchange, requestBody) -> {
            try {
                JSONObject requestJson = new JSONObject(requestBody);
                if (requestJson.has("authToken")) {
                    authTokenRef.set(requestJson.getString("authToken"));
                    // Continue with token validation and deletion
                    handleLogout(exchange, authTokenRef.get());
                } else {
                    respondWithError(exchange, "Invalid request format: authToken missing", 400);
                }
            } catch (JSONException e) {
                respondWithError(exchange, "Invalid request format: JSON parsing error", 400);
            }
        });
    }

    private void handleLogout(HttpServerExchange exchange, String authToken) {
        JSONObject responseJson = new JSONObject();

        if (authToken != null) {
            try {
                // Delete the authentication token from the database
                AuthenticationToken token = AuthenticationToken.getAuthenticationToken(authToken);
                if (token != null) {
                    token.deleteToken();
                    responseJson.put("message", "Logout successful");
                    respondWithJson(exchange, responseJson);
                    return;
                }
            } catch (Exception e) {
                respondWithError(exchange, "Internal server error", 500);
                logger.error("Exception in handleLogout: " + e.getMessage());
                return;
            }
        }

        // If the authentication token is not provided or invalid
        respondWithError(exchange, "Invalid or missing authentication token", 401);
    }


    private void refreshTokenHandler(HttpServerExchange exchange) {
        // Extract the authentication token from the request headers or query parameters
        String authToken = extractAuthTokenFromRequest(exchange);

        if (authToken != null) {
            try {
                // Retrieve the authentication token based on the provided authToken
                AuthenticationToken token = AuthenticationToken.getAuthenticationToken(authToken);

                if (token != null) {
                    // Generate a new token with the same username and a new token value
                    AuthenticationToken newToken = new AuthenticationToken(token.getUsername(), generateNewToken(), token.isTemporary());
                    newToken.saveToken();

                    // Respond with the new authentication token
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("authToken", newToken.getTokenValue());
                    respondWithJson(exchange, responseJson);
                    return;
                }
            } catch (Exception e) {
                respondWithError(exchange, "Internal server error", 500);
                logger.error("Exception in refreshTokenHandler: " + e.getMessage());
            }
        }

        // If the authentication token is not provided or invalid
        respondWithError(exchange, "Invalid or missing authentication token", 401);
    }

    private String extractAuthTokenFromRequest(HttpServerExchange exchange) {
        AtomicReference<String> token = new AtomicReference<>(null);

        exchange.getRequestReceiver().receiveFullString((httpServerExchange, s) -> {
            try {
                JSONObject requestJson = new JSONObject(s);

                if (requestJson.has("authToken")) {
                    token.set(requestJson.getString("authToken"));
                }
            } catch (Exception e) {
                logger.error("Exception in extractAuthTokenFromRequest: " + e.getMessage());
            }
        });

        return token.get();
    }

    private static String extractRequestBody(HttpServerExchange exchange) {
        AtomicReference<String> body = new AtomicReference<>("");
        exchange.getRequestReceiver().receiveFullString((httpServerExchange, s) -> body.set(s));
        return body.get();
    }

    private static JSONObject getAdminViewUserData(String username) {
        try {
            JSONObject userData = accountManager.getUserData(username, "*");
            return userData != null ? userData : new JSONObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    private static JSONObject getUserData(String username, Token token) {
        try {
            // Check if the token is authorized to access the data for the given username
            if (token.isAdminToken() || token.getUsername().equals(username)) {
                JSONObject userData = accountManager.getUserData(username, "*");
                return userData != null ? userData : new JSONObject();
            } else {
                return new JSONObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void applicationApiHandler(HttpServerExchange exchange) {
        String applicationToken = extractApplicationTokenFromRequest(exchange);

        if (applicationToken != null) {
            Token token = Token.getApplicationToken(applicationToken);

            try {
                assert token != null;
                boolean isAdminToken = token.isAdminToken();

                String requestBody = extractRequestBody(exchange);
                JSONObject requestData = new JSONObject(requestBody);

                String action = requestData.getString("action");
                JSONObject responseJson = new JSONObject();

                switch (action) {
                    case "viewUserData":
                        String usernameToView = requestData.getString("username");
                        JSONObject userData = isAdminToken ? getAdminViewUserData(usernameToView) : getUserData(usernameToView, token);
                        responseJson.put("data", userData);
                        break;

                    case "modifyUserData":
                        String usernameToModify = requestData.getString("username");
                        String dataKey = requestData.getString("dataKey");
                        JSONObject modifiedData = requestData.getJSONObject("modifiedData");

                        boolean b = false;

                        if(isAdminToken) {
                            b = modifyUserData(usernameToModify, dataKey, modifiedData);
                        }

                        boolean modificationResult = b;
                        responseJson.put("success", modificationResult);
                        break;

                    case "deleteAccount":
                        String accountToDelete = requestData.getString("username");

                        boolean b1 = false;

                        if(isAdminToken) {
                            b1 = deleteAccount(accountToDelete);
                        }

                        boolean deletionResult = b1;
                        responseJson.put("success", deletionResult);
                        break;

                    default:
                        respondWithError(exchange, "Unknown action", 400);
                        return;
                }

                respondWithJson(exchange, responseJson);

            } catch (Exception e) {
                respondWithError(exchange, "Internal server error", 500);
                logger.error("Exception in applicationApiHandler: " + e.getMessage());
            }
        } else {
            respondWithError(exchange, "Invalid application token", 401);
        }
    }

    private void respondWithError(HttpServerExchange exchange, String errorMessage, int statusCode) {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", errorMessage);
        exchange.setStatusCode(statusCode);
        respondWithJson(exchange, errorResponse);
    }

    private void respondWithJson(HttpServerExchange exchange, JSONObject jsonResponse) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(jsonResponse.toString());
    }

    private static boolean deleteAccount(String username) {
        try {
            // Delete the account and associated data
            accountManager.deleteAccount(username);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean modifyUserData(String username, String key,  JSONObject modifiedData) {
        try {
            // Modify user data
            return accountManager.updateUserData(username, key, modifiedData);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String extractApplicationTokenFromRequest(HttpServerExchange exchange) {
        // Extract the request body

        AtomicReference<String> token = new AtomicReference<>(null);

        exchange.getRequestReceiver().receiveFullString((httpServerExchange, s) -> {
            try {
                // Parse the request body as JSON
                JSONObject requestJson = new JSONObject(s);

                // Check if the JSON contains a field for the application token (e.g., "appToken")
                if (requestJson.has("appToken")) {
                    token.set(requestJson.getString("appToken"));
                }
            } catch (JSONException e) {
                // Handle JSON parsing exception
                e.printStackTrace();
            }
        });

        return token.get();  // Return null if the token is not found in the request body
    }

    private void deleteAccountHandler(HttpServerExchange exchange) {
        // Extract the request body to get the username to delete
        String requestBody = extractRequestBody(exchange);
        JSONObject requestData = new JSONObject(requestBody);
        String usernameToDelete = requestData.getString("username");

        AuthenticationToken token = AuthenticationToken.getAuthenticationToken(extractAuthTokenFromRequest(exchange));

        // Check if the token has permission to delete the specified account
        assert token != null;
        if (token.getUsername().equals(usernameToDelete)) {
            try {
                // Attempt to delete the account
                boolean deletionSuccess = deleteAccount(usernameToDelete);

                // Prepare the response JSON
                JSONObject responseJson = new JSONObject();
                responseJson.put("success", deletionSuccess);

                // Respond with the result
                respondWithJson(exchange, responseJson);
                return;
            } catch (Exception e) {
                // Handle exceptions appropriately (e.g., send an error response)
                e.printStackTrace();
            }
        } else {
            respondWithError(exchange, "Token isn't admin token", 400);
        }
    }



    private void createAccountHandler(HttpServerExchange exchange) {
        // Extract the request body to get account information
        JSONObject requestBodyJson = new JSONObject(extractRequestBody(exchange));

        if (requestBodyJson.has("username") && requestBodyJson.has("password")) {
            try {
                String username = requestBodyJson.getString("username");
                String password = requestBodyJson.getString("password");

                // Call the AccountManager's createAccount method
                accountManager.createAccount(username, password);

                // Prepare a success response
                JSONObject responseJson = new JSONObject();
                responseJson.put("success", true);

                // Respond with the success response
                respondWithJson(exchange, responseJson);
                return;
            } catch (Exception e) {
                // Handle exceptions appropriately (e.g., send an error response)
                e.printStackTrace();
            }
        } else {
            respondWithError(exchange, "Invalid body", 400);
        }
    }


    private void getAllInformationHandler(HttpServerExchange exchange) {
        // Extract the application token from the request
        String applicationToken = extractApplicationTokenFromRequest(exchange);

        if (applicationToken != null) {
            try {
                // Check if the application token is an admin token
                Token token = Token.getApplicationToken(applicationToken);
                if (token != null && token.isAdminToken()) {
                    // Token is an admin token, proceed to fetch user information

                    // Extract the username of the user whose information is requested
                    String requestedUsername = null;
                    JSONObject requestBodyJson = new JSONObject(extractRequestBody(exchange));
                    if (requestBodyJson != null && requestBodyJson.has("username")) {
                        requestedUsername = requestBodyJson.getString("username");
                    }

                    // Fetch the user account information
                    Account userAccount = accountManager.getAccount(requestedUsername);

                    // Prepare a JSON response with the user's information
                    JSONObject responseJson = new JSONObject();
                    if (userAccount != null) {
                        responseJson.put("username", userAccount.getUsername());

                        // Decode password from Base64 and replace each character with *
                        String decodedPassword = decodeBase64Password(userAccount.getPassword());
                        responseJson.put("password", maskPassword(decodedPassword));

                        // Add other user account information as needed
                    }

                    // Respond with the information
                    respondWithJson(exchange, responseJson);
                    return;
                }
            } catch (Exception e) {
                // Handle exceptions appropriately (e.g., send an error response)
                e.printStackTrace();
            }
        }

        // If the token is not provided, is invalid, or not an admin token
        respondWithError(exchange, "Token isn't admin token", 401);
    }

    private void changeUserDataHandler(HttpServerExchange exchange) {
        // Extract the authentication token from the request headers or query parameters
        String authToken = extractAuthTokenFromRequest(exchange);

        if (authToken != null) {
            // Extract the request body to get user data modifications
            String requestBody = extractRequestBody(exchange);
            JSONObject requestData = new JSONObject(requestBody);

            try {
                // Retrieve the authentication token based on the provided authToken
                AuthenticationToken token = AuthenticationToken.getAuthenticationToken(authToken);

                if (token != null) {
                    // Check if the token is authorized to modify user data for the given username
                    if (token.getUsername().equals(requestData.getString("username"))) {
                        String usernameToModify = requestData.getString("username");
                        String dataKey = requestData.getString("dataKey");
                        JSONObject modifiedData = requestData.getJSONObject("modifiedData");

                        boolean modificationResult = modifyUserData(usernameToModify, dataKey, modifiedData);

                        // Prepare the response JSON
                        JSONObject responseJson = new JSONObject();
                        responseJson.put("success", modificationResult);

                        // Respond with the result
                        respondWithJson(exchange, responseJson);
                        return;
                    } else {
                        respondWithError(exchange, "Unauthorized to modify user data", 401);
                    }
                } else {
                    respondWithError(exchange, "Invalid authentication token", 401);
                }
            } catch (Exception e) {
                respondWithError(exchange, "Internal server error", 500);
                logger.error("Exception in changeUserDataHandler: " + e.getMessage());
            }
        } else {
            respondWithError(exchange, "Invalid or missing authentication token", 401);
        }
    }


    private static String decodeBase64Password(String base64Password) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Password);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private static String maskPassword(String password) {
        StringBuilder maskedPassword = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            maskedPassword.append('*');
        }
        return maskedPassword.toString();
    }


}
