package upload;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.util.ArrayList;

/**
 * Created by yinxuexiao on 7/12/17.
 */
public class Attributes {
    private final String title;
    private final String author;

//    public Attributes(String title, String author) {
//        this.title = title;
//        this.author = author;
//    }

    public Attributes(String nlm) {
        this.title = extractTitle(nlm);
        this.author = extractAuthor(nlm);
    }

    public String getTitle(){
        return title;
    }

    public String getAuthor(){
        return author;
    }

    public String extractTitle(String nlm) {
        JSONObject xmlJSONObj = XML.toJSONObject(nlm);
        String title = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("title-group").getString("article-title");
        return title;
    }

    public String extractAuthor(String nlm) {
        JSONObject xmlJSONObj = XML.toJSONObject(nlm);
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONArray authors = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group").getJSONArray("contrib");
        for(int i=0;i<authors.length();i++)
        {
            String author = authors.getJSONObject(i).getString("string-name");
            arraylist.add(author);
        }
        String listString = String.join(", ", arraylist);
        return listString;
    }
}
