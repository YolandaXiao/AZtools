package extraction.author;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Author {

    private List<String> author;

    public List<String> getAuthor() {  return author; }

    public Author(JSONObject xmlJSONObj) throws Exception {
        this.author = extractAuthor(xmlJSONObj);
    }

    private List<String> extractAuthor(JSONObject xmlJSONObj) {
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
        return arraylist;
    }
}
