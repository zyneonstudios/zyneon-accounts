package co.plocki.neoguard.client;

import co.plocki.json.JSONFile;
import co.plocki.neoguard.client.util.AESUtil;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class NeoGuardClient {

    public static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static String SERVER_URL;
    public static String PASSPHRASE;
    public static String SESSION_KEY;
    public static boolean ssl;


    public void start() throws IOException, KeyManagementException, NoSuchAlgorithmException {

        JSONFile jsonFile = new JSONFile("config/client_config.json");
        if(jsonFile.isNew()) {
            JSONObject obj = jsonFile.getFileObject();

            JSONObject settings = new JSONObject();
            settings.put("passphrase", "YOUR_PASSPHRASE");
            settings.put("server_url", "http://localhost:5551/json-transfer");
            settings.put("ssl", false);

            obj.put("settings", settings);

            jsonFile.save();

            System.err.println("Please configure the client config: " + jsonFile.getFile().getAbsolutePath());
            //System.exit(1);
            //return;
        }

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new TrustAllManager()}, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new TrustAllHostnameVerifier());

        SERVER_URL = jsonFile.get("settings").getString("server_url");
        //PASSPHRASE = jsonFile.get("settings").getString("passphrase");
        ssl = jsonFile.get("settings").getBoolean("ssl");

        SESSION_KEY = connectAndAuthenticate();
    }

    private static void debug(String message) {
        //System.out.println("Debug [" + NeoGuardClient.class.getSimpleName() + "]: " + message);
    }

    public static String connectAndAuthenticate() throws IOException {
        debug("Calling 'connectAndAuthenticate' method.");

        JSONObject requestObj = new JSONObject();
        JSONObject dataObj = new JSONObject();
        dataObj.put("passphrase", PASSPHRASE);

        requestObj.put("data", dataObj);

        JSONObject responseObj = sendRequestSync(requestObj, true);

        System.out.println(responseObj);

        return responseObj.getJSONObject("data").getString("key");
    }

    public static JSONObject sendEncryptedData(JSONObject data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(PASSPHRASE.getBytes(StandardCharsets.UTF_8)), "AES");

        debug("Calling 'sendEncryptedData' method.");

        JSONObject requestObj = new JSONObject();
        JSONObject dataObj = new JSONObject();

        dataObj.put("request", data); // Wrap the original request in a "request" object
        dataObj.put("encryptedData", Base64.getEncoder().encodeToString(data.toString().getBytes(StandardCharsets.UTF_8)));

        requestObj.put("key", SESSION_KEY);
        requestObj.put("data", new String(AESUtil.encrypt(keySpec.getEncoded(), dataObj.toString().getBytes(StandardCharsets.UTF_8))));

        JSONObject response = sendRequestSync(requestObj, false);

        try {
            JSONObject responseData = new JSONObject(new String(AESUtil.decrypt(keySpec.getEncoded(), response.getString("data").getBytes(StandardCharsets.UTF_8))));
            response.put("data", responseData);

            if (responseData.has("encryptedData")) {
                JSONObject decryptedData = new JSONObject(new String(Base64.getDecoder().decode(responseData.getString("encryptedData"))));
                response.getJSONObject("data").put("encryptedData", decryptedData);
            }
        } catch (JSONException exception) {
            if (response.getString("status-code").equalsIgnoreCase("FAILED")) {
                try {
                    if (response.getJSONObject("data").getString("status-code").equalsIgnoreCase("KEY-TIMEOUT")) {
                        SESSION_KEY = connectAndAuthenticate();
                        return sendEncryptedData(data);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException("Error on self-catch - please contact an administrator - Error-Code: E58");
                }
            }
        }

        return response;
    }

    public static JSONObject sendRequestSync(JSONObject requestObj, boolean isConnect) throws IOException {
        debug("Calling 'sendRequest' method.");

        URL url = new URL(SERVER_URL);
        if(ssl) {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            if (!isConnect) {
                connection.setRequestProperty("DATA", "true");
            } else {
                connection.setRequestProperty("CONNECT", "true");
            }

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestObj.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream is = connection.getInputStream()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    String responseData = responseBuilder.toString();

                    if (!responseData.isEmpty()) {
                        try {
                            return new JSONObject(responseData);
                        } catch (JSONException e) {
                            return new JSONObject();
                        }
                    }
                }
            } else {
                System.err.println("Error on handling request");
            }
        } else {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            if (!isConnect) {
                connection.setRequestProperty("DATA", "true");
            } else {
                connection.setRequestProperty("CONNECT", "true");
            }

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestObj.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream is = connection.getInputStream()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    String responseData = responseBuilder.toString();

                    if (!responseData.isEmpty()) {
                        try {
                            return new JSONObject(responseData);
                        } catch (JSONException e) {
                            return new JSONObject();
                        }
                    }
                }
            } else {
                // Handle non-HTTP_OK response codes (e.g., HTTP error)
                // You can retrieve the error response using connection.getErrorStream()
            }
        }

        return new JSONObject();
    }

}
