package extraction.title;

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
            JSONObject article = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
            if (article.has("title-group")) {
                title = article.getJSONObject("title-group").getString("article-title");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return title;
    }
}
