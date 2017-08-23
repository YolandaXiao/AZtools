package extraction.doi;

import org.json.JSONException;
import org.json.JSONObject;

public class DOI {

    private String doi;

    public String getDoi() {  return doi; }

    public DOI(JSONObject xmlJSONObj) throws Exception {
        this.doi = extractDOI(xmlJSONObj);
    }

    private String extractDOI(JSONObject xmlJSONObj) {
        JSONObject value = null;
        try {
            value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (value.has("article-id")) {
            String DOI = null;
            try {
                DOI = value.getJSONObject("article-id").getString("content");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return DOI;
        }
        return "None";
    }
}
