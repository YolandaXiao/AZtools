package extraction.abstrakt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yinxuexiao on 8/25/17.
 */
public class Abstract {

    private String abstrakt;

    public String getAbstrakt() {  return abstrakt; }

    public Abstract(JSONObject xmlJSONObj, int num) throws Exception {
        if(num==0){
            this.abstrakt = extractAbstract_fromCermineXML(xmlJSONObj);
        }
        else{
            this.abstrakt = extractAbstract_fromPMCXML(xmlJSONObj);
        }

    }

    private String extractAbstract_fromCermineXML(JSONObject xmlJSONObj) {
        String abstrakt = "";
        try {
            abstrakt = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("abstract").getString("p");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return abstrakt;
    }

    private String extractAbstract_fromPMCXML(JSONObject xmlJSONObj) {
        String abstrakt = "";
        try {
            JSONArray abstract_arr = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("abstract").getJSONArray("p");
            for(int i=0;i<abstract_arr.length();i++){
                Object item = null;
                try {
                    item = abstract_arr.get(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (item instanceof String){
                    abstrakt += (String) item;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return abstrakt;
    }
}
