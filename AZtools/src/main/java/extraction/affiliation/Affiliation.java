package extraction.affiliation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
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
        JSONObject article_meta = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        if(article_meta.has("contrib-group")) {
            JSONObject group = article_meta.getJSONObject("contrib-group");
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
        }
        return arraylist;
    }

    private List<String> extractAffiliation_fromPMCXML(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist = new ArrayList<String>();
        JSONObject article_meta = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        if(article_meta.has("aff")){
            Object item2 = null;
            try {
                item2 = article_meta.get("aff");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            arraylist = getAff(item2);
            for (int i = 0; i < arraylist.size(); i++) {
                arraylist.set(i, arraylist.get(i).trim());
            }
        }
        if(article_meta.has("contrib-group")){
            Object item3 = null;
            try {
                item3 = article_meta.get("contrib-group");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (item3 instanceof JSONArray) {
                System.out.println("AFF is JSONARRAY");
                JSONArray group = (JSONArray) item3;
                for(int j=0;j<group.length();j++) {
                    JSONObject json = group.getJSONObject(j);
                    Iterator<String> keys = json.keys();

                    while (keys.hasNext()) {
                        String key = keys.next();
                        System.out.println("Key :" + key + "  Value :" + json.get(key));
                        if (key.equals("aff")) {
                            Object item2 = null;
                            try {
                                item2 = json.get(key);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            arraylist = getAff(item2);
                            for (int i = 0; i < arraylist.size(); i++) {
                                arraylist.set(i, arraylist.get(i).trim());
                            }
                        }
                    }
                }
            }
            else if (item3 instanceof JSONObject) {
                JSONObject group = (JSONObject) item3;
                if (!group.has("aff")) {
                    group = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
                }
                if(group.has("aff")){
                    System.out.println("group has aff");
                    Object item2 = null;
                    try {
                        item2 = group.get("aff");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    arraylist = getAff(item2);
                    for (int i = 0; i < arraylist.size(); i++) {
                        arraylist.set(i, arraylist.get(i).trim());
                    }
                }
            }

        }

        return arraylist;
    }

    //helper function
    private ArrayList<String> getAff(Object item2){
        ArrayList<String> arraylist = new ArrayList<String>();
        if (item2 instanceof JSONObject){
            JSONObject aff = (JSONObject) item2;
            Object item = null;
            try {
                item = aff.get("content");
                System.out.println("item :(.content)" + item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (item instanceof String){
                arraylist.add((String) item);
            }
            else if (item instanceof JSONArray){
                JSONArray affiliations = (JSONArray) item;
                for(int i=0;i<affiliations.length();i++)
                {
                    String result = affiliations.getString(i);
                    System.out.println("result: "+result);
                    if(!arraylist.contains(result)){
                        arraylist.add(result);
                    }
                }
            }
        }
        else if (item2 instanceof JSONArray){
            JSONArray affiliations = (JSONArray) item2;
            for(int i=0;i<affiliations.length();i++)
            {
                JSONObject result_object = affiliations.getJSONObject(i);
                String result = "";
                if(result_object.has("institution")){
                    Object item = null;
                    try {
                        item = result_object.get("institution");
                        System.out.println("item :(.institution)" + item);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (item instanceof String){
                        result = (String) item;
                    }
                    else if (item instanceof JSONArray){
                        JSONArray arr = (JSONArray) item;
                        for(int k=0;k<arr.length();k++){
                            Object content = arr.get(k);
                            if (content instanceof String){
                                result+=(String) content;
                            }
                            else if (content instanceof JSONObject){
                                JSONObject value = (JSONObject) content;
                                if(value.has("content")){
                                    result+=value.getString("content");
                                }
                            }
                        }
                    }
                }
                else if(result_object.has("content")){
                    Object item = null;
                    try {
                        item = result_object.get("content");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (item instanceof String){
                        result = (String) item;
                    }
                    else if (item instanceof JSONArray){
                        JSONArray arr = (JSONArray) item;
                        for(int k=0;k<arr.length();k++){
                            result+=arr.get(k);
                        }
                    }
                }
                else if(result_object.has("addr-line")){
                    Object item = null;
                    try {
                        item = result_object.get("addr-line");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (item instanceof String){
                        result = (String) item;
                    }
                    else if (item instanceof JSONArray){
                        JSONArray arr = (JSONArray) item;
                        for(int k=0;k<arr.length();k++){
                            result+=arr.get(k);
                        }
                    }
                }
                if(!arraylist.contains(result)){
                    arraylist.add(result);
                }
            }
        }
        return arraylist;
    }
}
