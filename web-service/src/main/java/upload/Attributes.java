package upload;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final List<String> URL;
    private final String funding;

    public Attributes(JSONObject xmlJSONObj) {
        this.title = extractTitle(xmlJSONObj);
        this.author = extractAuthor(xmlJSONObj);
        this.affiliation = extractAffiliation(xmlJSONObj);
        this.abstrakt = extractAbstract(xmlJSONObj);
        this.contact = extractContact(xmlJSONObj);
        this.DOI = extractDOI(xmlJSONObj);
        this.date = extractDate(xmlJSONObj);
        this.URL = extractURL(xmlJSONObj);
        this.funding = extractFunding(xmlJSONObj);
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

    public int getDate(){ return date; }

    public List<String> getURL(){
        return URL;
    }

    public String getFunding(){ return funding; }

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
        return arraylist;
    }

    public List<String> extractAffiliation(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONObject group = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group");
        if(group.has("aff")){
            Object item = group.get("aff");
            if (item instanceof JSONObject){
                JSONObject affiliations = (JSONObject) item;
                arraylist.add(affiliations.getString("institution"));
            }
            else if (item instanceof JSONArray){
                JSONArray affiliations = (JSONArray) item;
                for(int i=0;i<affiliations.length();i++)
                {
                    if(affiliations.getJSONObject(i).has("institution")) {
                        String aff = affiliations.getJSONObject(i).getString("institution");
                        arraylist.add(aff);
                    }
                }
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
        JSONObject value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        if (value.has("article-id")) {
            String DOI = value.getJSONObject("article-id").getString("content");
            return DOI;
        }
        return "None";
    }

    public int extractDate(JSONObject xmlJSONObj) {
        JSONObject value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        if (value.has("pub-date")) {
            int date = value.getJSONObject("pub-date").getInt("year");
            return date;
        }
        return 0;
    }

    public List<String> extractURL(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        String line = xmlJSONObj.toString();
        String pattern = "(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        while (m.find( )) {
            System.out.println("Found value: " + m.group());
            arraylist.add(m.group());
        }
        return arraylist;
    }

    public String extractFunding(JSONObject xmlJSONObj) {
        JSONArray funding_section = xmlJSONObj.getJSONObject("article").getJSONObject("body").getJSONArray("sec");
        for(int i=0;i<funding_section.length();i++)
        {
            String title = funding_section.getJSONObject(i).getString("title");
            if((funding_section.getJSONObject(i).has("title") && title.toLowerCase().equals("funding")) || (funding_section.getJSONObject(i).has("title") && title.toLowerCase().equals("acknowledgements"))){
                Object item = funding_section.getJSONObject(i).get("p");
                if (item instanceof String){
                    String funding_text = (String) item;
                    return funding_text;
                }
                else if (item instanceof JSONArray){
                    JSONArray funding_text = (JSONArray) item;
                    return funding_text.getString(0);
                }
            }
        }
        return "None";
    }
}
