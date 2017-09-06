package extraction.abstrakt;

import org.json.JSONArray;
import org.json.JSONObject;

public class Abstract {

    private String abstrakt;

    public String getAbstrakt() {
        return abstrakt;
    }

    public Abstract(JSONObject xmlJSONObj, int num) throws Exception {
        if(num == 0) {
            this.abstrakt = extractAbstract_fromCermineXML(xmlJSONObj);
        }
        else {
            this.abstrakt = extractAbstract_fromPMCXML(xmlJSONObj);
        }
    }

    private String extractAbstract_fromCermineXML(JSONObject xmlJSONObj) {
        String abstrakt;
        try {
            abstrakt = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("abstract").getString("p");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return abstrakt;
    }

    private String extractAbstract_fromPMCXML(JSONObject xmlJSONObj) {
        String abstrakt = "";
        try {
            JSONObject section = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
            if (section.has("abstract")) {
                JSONObject group = section.getJSONObject("abstract");
                if (group.has("p")) {
                    Object item = group.get("p");
                    if (item instanceof String){
                        abstrakt += (String) item;
                    } else if (item instanceof JSONArray) {
                        JSONArray abstract_arr = (JSONArray) item;
                        for (int i=0; i < abstract_arr.length(); i++) {
                            Object item2 = abstract_arr.get(i);
                            if (item2 instanceof String){
                                abstrakt += (String) item2;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return abstrakt;
    }
}
