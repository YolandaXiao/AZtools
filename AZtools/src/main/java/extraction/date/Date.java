package extraction.date;

import org.json.JSONArray;
import org.json.JSONObject;

public class Date {

    private String date;

    public String getDate() {  return date; }

    public Date(JSONObject xmlJSONObj, int num) throws Exception {
        if (num == 0) {
            this.date = extractDate_fromCermineXML(xmlJSONObj);
        }
        else {
            this.date = extractDate_fromPMCXML(xmlJSONObj);
        }

    }

    private String extractDate_fromCermineXML(JSONObject xmlJSONObj) {
        try {
            JSONObject value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
            if (value.has("pub-date")) {
                int date = value.getJSONObject("pub-date").getInt("year");
                return Integer.toString(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String extractDate_fromPMCXML(JSONObject xmlJSONObj) {
        try {
            JSONObject value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
            if (value.has("pub-date")) {
                Object item = value.get("pub-date");
                if (item instanceof JSONObject) {
                    JSONObject entry = (JSONObject) item;
                    String month = "";
                    String day = "";
                    String year = "";
                    if (entry.has("month")) {
                        month = Integer.toString(entry.getInt("month")) + "/";
                    }
                    if (entry.has("day")) {
                        day = Integer.toString(entry.getInt("day")) + "/";
                    }
                    if (entry.has("year")) {
                        year = Integer.toString(entry.getInt("year"));
                    }
                    String date = month + day + year;
                    return date;
                } else if (item instanceof JSONArray) {
                    JSONArray list = (JSONArray) item;
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject entry = list.getJSONObject(i);
                        String month = "";
                        String day = "";
                        String year = "";
                        if (entry.has("month")) {
                            month = Integer.toString(entry.getInt("month")) + "/";
                        }
                        if (entry.has("day")) {
                            day = Integer.toString(entry.getInt("day")) + "/";
                        }
                        if (entry.has("year")) {
                            year = Integer.toString(entry.getInt("year"));
                        }
                        String date = month + day + year;
                        return date;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }
}
