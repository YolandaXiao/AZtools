package extraction.date;

import org.json.JSONException;
import org.json.JSONObject;

public class Date {

    private int date;

    public int getDate() {  return date; }

    public Date(JSONObject xmlJSONObj) throws Exception {
        this.date = extractDate(xmlJSONObj);
    }

    private int extractDate(JSONObject xmlJSONObj) {
        JSONObject value = null;
        try {
            value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (value.has("pub-date")) {
            int date = 0;
            try {
                date = value.getJSONObject("pub-date").getInt("year");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return date;
        }
        return 0;
    }
}
