package upload;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinxuexiao on 7/12/17.
 */
public class Attributes {
    private final String title;
    private final List<String> author;
    private final List<String> affiliation;
    private final String abstrakt;
    private final List<String> contact;
    private final String DOI;
    private final int date;

//    public Attributes(String title, String author) {
//        this.title = title;
//        this.author = author;
//    }

    public Attributes(JSONObject xmlJSONObj) {
        this.title = extractTitle(xmlJSONObj);
        this.author = extractAuthor(xmlJSONObj);
        this.affiliation = extractAffiliation(xmlJSONObj);
        this.abstrakt = extractAbstract(xmlJSONObj);
        this.contact = extractContact(xmlJSONObj);
        this.DOI = extractDOI(xmlJSONObj);
        this.date = extractDate(xmlJSONObj);
    }

    public String getTitle(){
        return title;
    }

    public List<String> getAuthor(){
        return author;
    }

    public List<String> getAffiliation(){
        return affiliation;
    }

    public String getAbstrakt(){
        return abstrakt;
    }

    public List<String> getContact(){
        return contact;
    }

    public String getDOI(){
        return DOI;
    }

    public int getDate(){
        return date;
    }

    public String extractTitle(JSONObject xmlJSONObj) {
        String title = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("title-group").getString("article-title");
        return title;
    }

    public List<String> extractAuthor(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONArray authors = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group").getJSONArray("contrib");
        for(int i=0;i<authors.length();i++)
        {
            String author = authors.getJSONObject(i).getString("string-name");
            arraylist.add(author);
        }
//        String listString = String.join(", ", arraylist);
        return arraylist;
    }

    public List<String> extractAffiliation(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONArray affiliations = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group").getJSONArray("aff");
        for(int i=0;i<affiliations.length();i++)
        {
            if(affiliations.getJSONObject(i).has("institution")) {
                String aff = affiliations.getJSONObject(i).getString("institution");
                arraylist.add(aff);
            }
        }
        return arraylist;
    }

    public String extractAbstract(JSONObject xmlJSONObj) {
        String abstrakt = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("abstract").getString("p");
        return abstrakt;
    }

    public List<String> extractContact(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONArray contacts = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group").getJSONArray("contrib");
        for(int i=0;i<contacts.length();i++)
        {
            if(contacts.getJSONObject(i).has("email")){
                String contact = contacts.getJSONObject(i).getString("email");
                arraylist.add(contact);
            }
        }
        return arraylist;
    }

    public String extractDOI(JSONObject xmlJSONObj) {
        String DOI = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("article-id").getString("content");
        return DOI;
    }

    public int extractDate(JSONObject xmlJSONObj) {
        int date = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("pub-date").getInt("year");
        return date;
    }
}
