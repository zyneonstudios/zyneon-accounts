package co.plocki.neoguard.client.interfaces;

import co.plocki.neoguard.client.NeoGuardClient;
import com.zyneonstudios.accounts.AccountSystem;
import org.apache.logging.log4j.Level;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NeoArray {

    private final String name;

    public NeoArray(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean deleteArray(String thread, String array) throws Exception {
        debug("Calling 'deleteArray' method.");

        JSONObject object = new JSONObject();
        object.put("type", "delete");
        object.put("deleteType", "array");
        object.put("dataThread", thread);
        object.put("array", array);

        JSONObject resp = NeoGuardClient.sendEncryptedData(object);

        return resp.getJSONObject("data").getJSONObject("encryptedData").getJSONObject("response").getString("status-code").equalsIgnoreCase("SUCCEED");
    }

    public List<NeoArray> searchArrays(String searchTerm, String dataThread) throws Exception {
        debug("Calling 'searchArrays' method.");

        JSONObject object = new JSONObject();
        object.put("type", "search");
        object.put("searchType", "arrays");
        object.put("searchTerm", searchTerm);
        object.put("dataThread", dataThread);

        JSONObject resp = NeoGuardClient.sendEncryptedData(object);

        if(resp.getJSONObject("data").getJSONObject("encryptedData").getJSONObject("response").getJSONArray("matchingArrays").length() != 0) {
            List<NeoArray> arrays = new ArrayList<>();
            for (Object o : resp.getJSONObject("data").getJSONObject("encryptedData").getJSONObject("response").getJSONArray("matchingArrays")) {
                arrays.add(new NeoArray((String) o));
            }
            return arrays;
        }

        return new ArrayList<>();
    }

    private static void debug(String message) {
        if(AccountSystem.debug) {
            AccountSystem.logger.log(Level.DEBUG, "Debug [" + NeoGuardClient.class.getSimpleName() + "]: " + message);
        }
    }

}
