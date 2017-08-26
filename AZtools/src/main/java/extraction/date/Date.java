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
            JSONArray list = value.getJSONArray("pub-date");
            for(int i=0;i<list.length();i++){
                JSONObject entry = list.getJSONObject(i);
                if(entry.getString("pub-type").equals("pmc-release")){
                    String month = Integer.toString(entry.getInt("month"));
                    String year = Integer.toString(entry.getInt("year"));
                    String day = Integer.toString(entry.getInt("day"));
                    String date = month+"/"+day+"/"+year;
                    return date;
                }
            }
        }
        return "0";
    }
}
