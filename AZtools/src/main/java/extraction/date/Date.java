package extraction.date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Date {

    private String date;

    public String getDate() {  return date; }

    public Date(JSONObject xmlJSONObj, int num) throws Exception {
        if(num==0){
            this.date = extractDate_fromCermineXML(xmlJSONObj);
        }
        else{
            this.date = extractDate_fromPMCXML(xmlJSONObj);
        }

    }

    private String extractDate_fromCermineXML(JSONObject xmlJSONObj) {
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
            return Integer.toString(date);
        }
        return "0";
    }

    private String extractDate_fromPMCXML(JSONObject xmlJSONObj) {
        JSONObject value = null;
        try {
            value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (value.has("pub-date")) {
            Object item = null;
            try {
                item = value.get("pub-date");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (item instanceof JSONObject){
                JSONObject entry = (JSONObject) item;
                String month = "";
                String day = "";
                String year = "";
                if(entry.has("month"))
                    month = Integer.toString(entry.getInt("month"))+"/";
                if(entry.has("day"))
                    day = Integer.toString(entry.getInt("day"))+"/";
                if(entry.has("year"))
                    year = Integer.toString(entry.getInt("year"));
                String date = month+day+year;
                return date;
            }
            else if (item instanceof JSONArray){
                JSONArray list = (JSONArray) item;
                for(int i=0;i<list.length();i++){
                    JSONObject entry = list.getJSONObject(i);
                    String month = "";
                    String day = "";
                    String year = "";
                    if(entry.has("month"))
                        month = Integer.toString(entry.getInt("month"))+"/";
                    if(entry.has("day"))
                        day = Integer.toString(entry.getInt("day"))+"/";
                    if(entry.has("year"))
                        year = Integer.toString(entry.getInt("year"));
                    String date = month+day+year;
                    return date;
                }
            }
        }
        return "0";
    }
}
