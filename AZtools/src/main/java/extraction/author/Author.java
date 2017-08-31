package extraction.author;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Author {

    private List<String> author;

    public List<String> getAuthor() {  return author; }

    public Author(JSONObject xmlJSONObj, int num) throws Exception {
        if(num==0){
            this.author = extractAuthor_fromCermineXML(xmlJSONObj);
        }
        else{
            this.author = extractAuthor_fromPMCXML(xmlJSONObj);
        }
    }

    private List<String> extractAuthor_fromCermineXML(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist = new ArrayList<String>();
        JSONObject article_meta = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        if(article_meta.has("contrib-group")){
            JSONObject group = article_meta.getJSONObject("contrib-group");
            if (group.has("contrib")) {
                Object item = null;
                try {
                    item = group.get("contrib");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (item instanceof JSONArray) {
                    JSONArray authors = (JSONArray) item;
                    for (int i = 0; i < authors.length(); i++) {
                        String author = null;
                        try {
                            author = authors.getJSONObject(i).getString("string-name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        arraylist.add(author);
                    }
                } else if (item instanceof String) {
                    String author = (String) item;
                    arraylist.add(author);
                }
            }
        }
        return arraylist;
    }

    private List<String> extractAuthor_fromPMCXML(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist = new ArrayList<String>();
        JSONObject group = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group");
        if (group.has("contrib")) {
            Object item = null;
            try {
                item = group.get("contrib");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (item instanceof JSONArray) {
                JSONArray authors = (JSONArray) item;
                for (int i = 0; i < authors.length(); i++) {
                    String author = null;
                    try {
                        JSONObject name = authors.getJSONObject(i).getJSONObject("name");
                        author = name.getString("given-names")+" "+name.getString("surname");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    arraylist.add(author);
                }
            } else if (item instanceof String) {
                String author = (String) item;
                arraylist.add(author);
            }
        }
        return arraylist;
    }
}
