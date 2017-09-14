package extraction.author;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Author {

    private List<String> author;

    public List<String> getAuthor() {  return author; }

    public Author(JSONObject xmlJSONObj, int num) throws Exception {
        if (num == 0) {
            this.author = extractAuthor_fromCermineXML(xmlJSONObj);
        }
        else {
            this.author = extractAuthor_fromPMCXML(xmlJSONObj);
        }
    }

    private List<String> extractAuthor_fromCermineXML(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist = new ArrayList<String>();
        try {
            JSONObject article_meta = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
            if (article_meta.has("contrib-group")) {
                JSONObject group = article_meta.getJSONObject("contrib-group");
                if (group.has("contrib")) {
                    Object item = group.get("contrib");
                    if (item instanceof JSONArray) {
                        JSONArray authors = (JSONArray) item;
                        for (int i = 0; i < authors.length(); i++) {
                            String author = authors.getJSONObject(i).getString("string-name");
                            arraylist.add(author);
                        }
                    } else if (item instanceof String) {
                        String author = (String) item;
                        arraylist.add(author);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    private List<String> extractAuthor_fromPMCXML(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist = new ArrayList<String>();
        JSONObject article_meta = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        if(article_meta.has("contrib-group")){
            Object item2 = null;
            try {
                item2 = article_meta.get("contrib-group");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (item2 instanceof JSONArray) {
                JSONArray group = (JSONArray) item2;
                for(int j=0;j<group.length();j++){
                    JSONObject json = group.getJSONObject(j);
                    Iterator<String> keys = json.keys();

                    while (keys.hasNext()) {
                        String key = keys.next();
                        System.out.println("Key :" + key + "  Value :" + json.get(key));
                        if(key.equals("contrib")){
                            Object item = null;
                            try {
                                item = json.get(key);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            arraylist = getAuthor(item);
                            for (int i = 0; i < arraylist.size(); i++) {
                                arraylist.set(i, arraylist.get(i).trim());
                            }
                        }
                    }
                }
            }
            if(item2 instanceof JSONObject){
                JSONObject group = (JSONObject) item2;
                if (group.has("contrib")) {
                    Object item = null;
                    try {
                        item = group.get("contrib");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    arraylist = getAuthor(item);
                    for (int i = 0; i < arraylist.size(); i++) {
                        arraylist.set(i, arraylist.get(i).trim());
                    }
                }
            }
        }
        return arraylist;
    }

    private ArrayList<String> getAuthor(Object item){
        ArrayList<String> arraylist = new ArrayList<String>();
        if (item instanceof JSONArray) {
            JSONArray authors = (JSONArray) item;
            for (int i = 0; i < authors.length(); i++) {
                String author = null;
                try {
                    JSONObject contrib = authors.getJSONObject(i);
                    System.out.println("contrib: "+contrib);
                    if(contrib.has("name")){
                        JSONObject name = contrib.getJSONObject("name");
                        author = name.getString("given-names")+" "+name.getString("surname");
                        arraylist.add(author);
                        System.out.println("author: "+author);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (item instanceof String) {
            String author = (String) item;
            arraylist.add(author);
        }
        else if (item instanceof JSONObject) {
            JSONObject author = (JSONObject) item;
            if(author.has("name")){
                JSONObject name = author.getJSONObject("name");
                String au = name.getString("given-names")+" "+name.getString("surname");
                arraylist.add(au);
                System.out.println("author: "+author);
            }
        }
        return arraylist;
    }
}
