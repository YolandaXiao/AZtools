package extraction.affiliation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Affiliation {

    private List<String> affiliation;

    public List<String> getAffiliation() {
        return affiliation;
    }

    public Affiliation(JSONObject xmlJSONObj, int num) throws Exception {
        if( num == 0){
            this.affiliation = extractAffiliation_fromCermineXML(xmlJSONObj);
        }
        else{
            this.affiliation = extractAffiliation_fromPMCXML(xmlJSONObj);
        }

    }

    private List<String> extractAffiliation_fromCermineXML(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist = new ArrayList<String>();
        try {
            JSONObject article_meta = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");

            if (article_meta.has("contrib-group")) {
                JSONObject group = article_meta.getJSONObject("contrib-group");
                if (group.has("aff")) {
                    Object item = group.get("aff");
                    if (item instanceof JSONObject) {
                        JSONObject affiliations = (JSONObject) item;
                        String aff = affiliations.getString("institution");
                        String[] arr = aff.split(",");
                        String result = arr[arr.length - 1];
                        if (!arraylist.contains(result)) {
                            arraylist.add(result);
                        }
                    } else if (item instanceof JSONArray) {
                        JSONArray affiliations = (JSONArray) item;

                        for (int i = 0; i < affiliations.length(); i++) {
                            if (affiliations.getJSONObject(i).has("institution")) {
                                Object item2 = affiliations.getJSONObject(i).get("institution");

                                if (item2 instanceof String) {
                                    String aff = affiliations.getJSONObject(i).getString("institution");
                                    String[] arr = aff.split(",");
                                    String result = arr[arr.length - 1];
                                    if (!arraylist.contains(result)) {
                                        arraylist.add(result);
                                    }
                                } else if (item2 instanceof JSONArray) {
                                    JSONArray arr = affiliations.getJSONObject(i).getJSONArray("institution");
                                    String result = (String) arr.get(arr.length() - 1);
                                    if (!arraylist.contains(result)) {
                                        arraylist.add(result);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            return new ArrayList();
        }
        return arraylist;
    }

    private List<String> extractAffiliation_fromPMCXML(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist = new ArrayList<String>();
        try {
            JSONObject group = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group");
            if (!group.has("aff")) {
                group = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
            }
            if (group.has("aff")) {
                Object item2 = group.get("aff");

                if (item2 instanceof JSONObject) {
                    JSONObject aff = (JSONObject) item2;
                    Object item = aff.get("content");

                    if (item instanceof String) {
                        arraylist.add((String) item);
                    } else if (item instanceof JSONArray) {
                        JSONArray affiliations = (JSONArray) item;
                        for (int i = 0; i < affiliations.length(); i++) {
                            String result = affiliations.getString(i);
                            if (!arraylist.contains(result)) {
                                arraylist.add(result);
                            }
                        }
                    }
                } else if (item2 instanceof JSONArray) {
                    JSONArray affiliations = (JSONArray) item2;
                    for (int i = 0; i < affiliations.length(); i++) {
                        JSONObject result_object = affiliations.getJSONObject(i);
                        String result = "";
                        if (result_object.has("institution")) {
                            Object item = result_object.get("institution");
                            if (item instanceof String) {
                                result = (String) item;
                            } else if (item instanceof JSONArray) {
                                JSONArray arr = (JSONArray) item;
                                for (int j = 0; j < arr.length(); j++) {
                                    result += arr.get(j);
                                }
                            }
                        } else if (result_object.has("content")) {
                            Object item = result_object.get("content");
                            if (item instanceof String) {
                                result = (String) item;
                            } else if (item instanceof JSONArray) {
                                JSONArray arr = (JSONArray) item;
                                for (int j = 0; j < arr.length(); j++) {
                                    result += arr.get(j);
                                }
                            }
                        }
                        if (!arraylist.contains(result)) {
                            arraylist.add(result);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arraylist;
    }
}
