package co.plocki.neoguard.client.post;

import co.plocki.neoguard.client.NeoGuardClient;
import co.plocki.neoguard.client.interfaces.NeoArray;
import co.plocki.neoguard.client.interfaces.NeoThread;
import com.zyneonstudios.accounts.AccountSystem;
import org.apache.logging.log4j.Level;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class NeoPost {

    private final NeoThread thread;
    private final List<NeoArray> arrays;
    private final List<?> data;

    public NeoPost(NeoThread thread, List<NeoArray> arrays, List<?> data) {
        this.thread = thread;
        this.arrays = arrays;
        this.data = data;
    }

    /***
     * Post data to update it or insert new data
     * @return success statement
     * @throws Exception
     */
    public boolean post() throws Exception {
        debug("Calling 'post' method.");

        JSONObject object = new JSONObject();
        object.put("dataThread", thread.getName());
        object.put("type", "POST");

        JSONArray arrayList = new JSONArray();
        arrays.forEach(neoArray -> {
            arrayList.put(neoArray.getName());
        });

        object.put("arrays", arrayList);
        object.put("data", new JSONArray(data));

        JSONObject resp = NeoGuardClient.sendEncryptedData(object);


        if(resp.getString("status-code").equalsIgnoreCase("SUCCESSFUL")) {
            if(resp.getJSONObject("data").getString("status-code").equalsIgnoreCase("SUCCESSFUL")) {
                return resp.getJSONObject("data").getJSONObject("encryptedData").getJSONObject("response").getString("status-code").equalsIgnoreCase("SUCCEED");
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    private static void debug(String message) {
        if(AccountSystem.debug) {
            AccountSystem.logger.log(Level.DEBUG, "Debug [" + NeoGuardClient.class.getSimpleName() + "]: " + message);
        }
    }
}
