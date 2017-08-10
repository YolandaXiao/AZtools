package extraction.author;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinxuexiao on 8/10/17.
 */
public class Author {

    public List<String> author;

    public List<String> getAuthor() {  return author; }

    public Author(JSONObject xmlJSONObj) throws Exception {
        this.author = extractAuthor(xmlJSONObj);
    }

    private List<String> extractAuthor(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONArray authors = null;
        try {
            authors = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group").getJSONArray("contrib");
            for(int i=0;i<authors.length();i++)
            {
                String author = null;
                try {
                    author = authors.getJSONObject(i).getString("string-name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                arraylist.add(author);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }
}
