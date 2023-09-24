package co.plocki.json;

import org.json.JSONObject;

import java.io.IOException;

public class ButifyJSON {

    public static void main(String[] args) throws IOException {
        String json = new JSONFile("notbuty.json").get("json").toString();
        JSONFile file = new JSONFile("butiful.json");
        file.put("json", new JSONObject(json));
        file.save();
    }

}
