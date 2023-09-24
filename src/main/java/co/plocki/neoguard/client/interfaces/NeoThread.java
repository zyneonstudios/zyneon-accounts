package co.plocki.neoguard.client.interfaces;

import co.plocki.neoguard.client.NeoGuardClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NeoThread {

    private final String name;

    public NeoThread(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<NeoThread> searchThreads(String searchTerm) throws Exception {
        debug("Calling 'searchThreads' method.");

        JSONObject object = new JSONObject();
        object.put("type", "search");
        object.put("searchType", "threads");
        object.put("searchTerm", searchTerm);
        object.put("dataThread", searchTerm);

        JSONObject resp = NeoGuardClient.sendEncryptedData(object);

        if(resp.getJSONObject("data").getJSONObject("encryptedData").getJSONObject("response").getString("status-code").equalsIgnoreCase("SUCCEED")) {
            JSONArray array = resp.getJSONObject("data").getJSONObject("encryptedData").getJSONObject("response").getJSONArray("matchingThreads");

            List<NeoThread> threads = new ArrayList<>();
            for (Object o : array) {
                threads.add(new NeoThread((String) o));
            }

            return threads;
        }

        return new ArrayList<>();
    }

    public boolean delete() throws Exception {
        if(name != null && !name.equals("")) {
            return deleteThread(name);
        }
        return false;
    }

    public boolean deleteThread(String thread) throws Exception {
        debug("Calling 'deleteThread' method.");

        JSONObject object = new JSONObject();
        object.put("type", "delete");
        object.put("deleteType", "thread");
        object.put("dataThread", thread);

        JSONObject resp = NeoGuardClient.sendEncryptedData(object);

        return resp.getJSONObject("data").getJSONObject("encryptedData").getJSONObject("response").getString("status-code").equalsIgnoreCase("SUCCEED");
    }

    private void debug(String message) {
        //System.out.println("Debug [" + NeoGuardClient.class.getSimpleName() + "]: " + message);
    }

}
