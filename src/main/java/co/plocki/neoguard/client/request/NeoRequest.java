package co.plocki.neoguard.client.request;

import co.plocki.neoguard.client.NeoGuardClient;
import co.plocki.neoguard.client.interfaces.NeoArray;
import co.plocki.neoguard.client.interfaces.NeoThread;
import co.plocki.neoguard.client.util.AESUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class NeoRequest {

    private final NeoThread thread;
    private final List<NeoArray> array;

    public NeoRequest(NeoThread thread, List<NeoArray> arrays) {
        this.thread = thread;
        this.array = arrays;
    }

    public NeoResponse request() throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(NeoGuardClient.PASSPHRASE.getBytes(StandardCharsets.UTF_8)), "AES");

        debug("Calling 'request' method.");

        JSONObject requestData = new JSONObject();
        requestData.put("type", "GET");
        requestData.put("dataThread", thread);

        JSONArray arrays = new JSONArray();
        arrays.put(array);

        requestData.put("arrays", arrays);

        JSONObject encData = new JSONObject();
        encData.put("encryptedData", new String(Base64.getEncoder().encode(requestData.toString().getBytes(StandardCharsets.UTF_8))));

        JSONObject object = new JSONObject();
        object.put("key", NeoGuardClient.SESSION_KEY);
        object.put("data", new String(AESUtil.encrypt(keySpec.getEncoded(), encData.toString().getBytes(StandardCharsets.UTF_8))));


        JSONObject response = NeoGuardClient.sendRequestSync(object, false);

        try {
            response.put("data", new JSONObject(new String(AESUtil.decrypt(keySpec.getEncoded(), response.getString("data").getBytes(StandardCharsets.UTF_8)))));
        } catch (JSONException exception) {

            if(response.getString("status-code").equalsIgnoreCase("FAILED")) {
                try {
                    if(response.getJSONObject("data").getString("status-code").equalsIgnoreCase("KEY-TIMEOUT")) {
                        NeoGuardClient.SESSION_KEY = NeoGuardClient.connectAndAuthenticate();
                        return request();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException("Error on self-catch - please contact a administrator - Error-Code: E58");
                }
            }
        }
        JSONObject data = response.getJSONObject("data");
        data.put("encryptedData", new JSONObject(new String(Base64.getDecoder().decode(data.getString("encryptedData")))));

        response.put("data", data);
        if(data.getString("status-code").equalsIgnoreCase("SUCCESSFUL")) {
            return new NeoResponse(data);
        } else {
            return null;
        }
    }

    private static void debug(String message) {
        //System.out.println("Debug [" + NeoGuardClient.class.getSimpleName() + "]: " + message);
    }

}
