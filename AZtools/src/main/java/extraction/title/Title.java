package extraction.title;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yinxuexiao on 8/10/17.
 */
public class Title {

    public String title;

    public String getTitle() {  return title; }

    public Title(JSONObject xmlJSONObj) throws Exception {
        this.title = extractTitle(xmlJSONObj);
    }

    private String extractTitle(JSONObject xmlJSONObj) {
        String title = null;
        try {
            title = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("title-group").getString("article-title");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return title;
    }
}
