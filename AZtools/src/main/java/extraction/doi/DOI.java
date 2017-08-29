package extraction.doi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DOI {

    private String doi;

    public String getDoi() {  return doi; }

    public DOI(JSONObject xmlJSONObj, int num) throws Exception {
        if(num==0){
            this.doi = extractDOI_fromCermineXML(xmlJSONObj);
        }
        else{
            this.doi = extractDOI_fromPMCXML(xmlJSONObj);
        }

    }

    private String extractDOI_fromCermineXML(JSONObject xmlJSONObj) {
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

    private String extractDOI_fromPMCXML(JSONObject xmlJSONObj) {
        JSONObject value = null;
        try {
            value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (value.has("article-id")) {
            JSONArray list = value.getJSONArray("article-id");
            for(int i=0;i<list.length();i++){
                JSONObject entry = list.getJSONObject(i);
                if(entry.getString("pub-id-type").equals("doi")){
                    return entry.getString("content");
                }
            }
        }
        return "None";
    }
}
