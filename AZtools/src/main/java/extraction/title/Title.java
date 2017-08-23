package extraction.title;

import org.json.JSONException;
import org.json.JSONObject;

public class Title {

    private String title;

    public String getTitle() {  return title; }

    public Title(JSONObject xmlJSONObj) throws Exception {
        this.title = extractTitle(xmlJSONObj);
    }

    private String extractTitle(JSONObject xmlJSONObj) {
        String title = "";
        try {
            title = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("title-group").getString("article-title");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return title;
    }
}
