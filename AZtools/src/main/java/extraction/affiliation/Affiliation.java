package extraction.affiliation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Affiliation {

    private List<String> affiliation;

    public List<String> getAffiliation() {  return affiliation; }

    public Affiliation(JSONObject xmlJSONObj, int num) throws Exception {
        if(num==0){
            this.affiliation = extractAffiliation_fromCermineXML(xmlJSONObj);
        }
        else{
            this.affiliation = extractAffiliation_fromPMCXML(xmlJSONObj);
        }

    }

    private List<String> extractAffiliation_fromCermineXML(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist = new ArrayList<String>();
        JSONObject group = null;
        try {
            group = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (group.has("aff")) {
            Object item = null;
            try {
                item = group.get("aff");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (item instanceof JSONObject){
                JSONObject affiliations = (JSONObject) item;
                try {
                    String aff = affiliations.getString("institution");
                    String[] arr = aff.split(",");
                    String result = arr[arr.length-1];
                    if(!arraylist.contains(result)){
                        arraylist.add(result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (item instanceof JSONArray){
                JSONArray affiliations = (JSONArray) item;
                for(int i=0;i<affiliations.length();i++)
                {
                    try {
                        if(affiliations.getJSONObject(i).has("institution")) {
                            Object item2 = null;
                            try {
                                item2 = affiliations.getJSONObject(i).get("institution");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (item2 instanceof String){
                                String aff = affiliations.getJSONObject(i).getString("institution");
                                String[] arr = aff.split(",");
                                String result = arr[arr.length-1];
                                if(!arraylist.contains(result)){
                                    arraylist.add(result);
                                }
                            }
                            else if (item2 instanceof JSONArray){
                                JSONArray arr = affiliations.getJSONObject(i).getJSONArray("institution");
                                String result = (String) arr.get(arr.length()-1);
                                if(!arraylist.contains(result)){
                                    arraylist.add(result);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return arraylist;
    }

    private List<String> extractAffiliation_fromPMCXML(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist = new ArrayList<String>();
        JSONObject group = null;
        try {
            group = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (group.has("aff")) {
            JSONArray affiliations = group.getJSONObject("aff").getJSONArray("content");
            for(int i=0;i<affiliations.length();i++)
            {
                String result = affiliations.getString(i);
                if(!arraylist.contains(result)){
                    arraylist.add(result);
                }
            }
        }
        return arraylist;
    }
}
