package co.plocki.neoguard.client.interfaces;

import co.plocki.neoguard.client.NeoGuardClient;
import org.json.JSONObject;

public class NeoRow {

    public boolean updateRow(String thread, String array, String row, Object newData) throws Exception {
        debug("Calling 'updateRow' method.");

        JSONObject object = new JSONObject();
        object.put("type", "update");
        object.put("dataThread", thread);
        object.put("array", array);
        object.put("row", row);
        object.put("newData", newData);

        JSONObject resp = NeoGuardClient.sendEncryptedData(object);

        return resp.getJSONObject("data").getJSONObject("encryptedData").getJSONObject("response").getString("status-code").equalsIgnoreCase("SUCCEED");
    }

    public boolean deleteRow(String thread, String array, String row) throws Exception {
        debug("Calling 'deleteRow' method.");

        JSONObject object = new JSONObject();
        object.put("type", "delete");
        object.put("deleteType", "row");
        object.put("dataThread", thread);
        object.put("array", array);
        object.put("row", row);

        JSONObject resp = NeoGuardClient.sendEncryptedData(object);

        return resp.getJSONObject("data").getJSONObject("encryptedData").getJSONObject("response").getString("status-code").equalsIgnoreCase("SUCCEED");
    }

    private void debug(String message) {
        //System.out.println("Debug [" + NeoGuardClient.class.getSimpleName() + "]: " + message);
    }

}
