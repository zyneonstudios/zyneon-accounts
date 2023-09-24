package co.plocki.neoguard.client.request;

import co.plocki.neoguard.client.interfaces.NeoArray;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class NeoResponse {

    private HashMap<NeoArray, List<Object>> data;

    public NeoResponse(JSONObject dat) {
        data = new HashMap<>();

        dat = dat.getJSONObject("response");
        JSONArray array = dat.getJSONArray("arrayData");
        array.forEach(jsonObject -> {
            JSONObject object = (JSONObject) jsonObject;

            List<Object> objs = new ArrayList<>();
            object.getJSONArray("rows").forEach(json -> {
                JSONObject object1 = (JSONObject) json;
                objs.add(object1.get("data"));
            });

            data.put(new NeoArray(object.getString("array")), objs);
        });
    }

    public List<Object> getArrayObjects(NeoArray array) {
        return Objects.requireNonNullElse(data.get(array), new ArrayList<>());
    }

    public Object getObject(NeoArray array, int index) {
        return data.get(array).get(index);
    }

    public HashMap<NeoArray, List<Object>> getRawData() {
        return data;
    }

}
