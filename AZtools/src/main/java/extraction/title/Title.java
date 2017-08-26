package extraction.title;

import org.json.JSONException;
import org.json.JSONObject;

public class Title {

    private String title;

    public String getTitle() {  return title; }

    public Title(JSONObject xmlJSONObj) throws Exception {
//        if(num==0){
        this.title = extractTitle(xmlJSONObj);
//        }
//        else{
//            this.title = extractTitle_fromPMCXML(xmlJSONObj);
//        }

    }

    private String extractTitle(JSONObject xmlJSONObj) {
        String title = "";
        try {
            JSONObject article = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
            if(article.has("title-group")){
                title = article.getJSONObject("title-group").getString("article-title");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return title;
    }

//    private String extractTitle_fromPMCXML(JSONObject xmlJSONObj) {
//        String title = "";
//        try {
//            JSONObject article = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
//            if(article.has("title-group")){
//                title = article.getJSONObject("title-group").getString("article-title");
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return title;
//    }
}
