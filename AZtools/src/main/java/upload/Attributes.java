package upload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Attributes {
    private final String title;
    private final List<String> author;
    private final List<String> affiliation;
    private final String abstrakt;
    private final List<String> contact;
    private final String DOI;
    private final int date;
    private final List<String> URL;
//    private final List<String> funding;
    private final List<String> license;

    public Attributes(JSONObject xmlJSONObj, String name) {
        this.title = extractTitle(xmlJSONObj);
        this.author = extractAuthor(xmlJSONObj);
        this.affiliation = extractAffiliation(xmlJSONObj);
        this.abstrakt = extractAbstract(xmlJSONObj);
        this.contact = extractContact(xmlJSONObj);
        this.DOI = extractDOI(xmlJSONObj);
        this.date = extractDate(xmlJSONObj);
        this.URL = extractURL(xmlJSONObj,name);
//        this.funding = extractFunding(xmlJSONObj);
        this.license = extractLicense(xmlJSONObj);
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

    public int getDate() {
        return date;
    }

    public List<String> getURL(){
        return URL;
    }

//    public List<String> getFunding() {
//        return funding;
//    }

    public List<String> getLicense() { return license; }

    public String extractTitle(JSONObject xmlJSONObj) {
        String title = null;
        try {
            title = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("title-group").getString("article-title");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return title;
    }

    public List<String> extractAuthor(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONArray authors = null;
        try {
            authors = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group").getJSONArray("contrib");
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        return arraylist;
    }

    public List<String> extractAffiliation(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONObject group = null;
        try {
            group = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(group.has("aff")){
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
                            String aff = affiliations.getJSONObject(i).getString("institution");
                            String[] arr = aff.split(",");
                            String result = arr[arr.length-1];
                            if(!arraylist.contains(result)){
                                arraylist.add(result);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return arraylist;
    }

    public String extractAbstract(JSONObject xmlJSONObj) {
        String abstrakt = null;
        try {
            abstrakt = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("abstract").getString("p");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return abstrakt;
    }

    public List<String> extractContact(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONArray contacts = null;
        try {
            contacts = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group").getJSONArray("contrib");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for(int i=0;i<contacts.length();i++)
        {
            try {
                if(contacts.getJSONObject(i).has("email")){
                    String contact = null;
                    try {
                        contact = contacts.getJSONObject(i).getString("email");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    arraylist.add(contact);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arraylist;
    }

    public String extractDOI(JSONObject xmlJSONObj) {
        JSONObject value = null;
        try {
            value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (value.has("article-id")) {
            String DOI = null;
            try {
                DOI = value.getJSONObject("article-id").getString("content");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return DOI;
        }
        return "None";
    }

    public int extractDate(JSONObject xmlJSONObj) {
        JSONObject value = null;
        try {
            value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (value.has("pub-date")) {
            int date = 0;
            try {
                date = value.getJSONObject("pub-date").getInt("year");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return date;
        }
        return 0;
    }

    public List<String> extractURL(JSONObject xmlJSONObj, String name) {
        ArrayList<String> all_links= new ArrayList<String>();
        ArrayList<String> good_links= new ArrayList<String>();
        String line = xmlJSONObj.toString();
        String pattern = "(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";
//        String pattern = "\\.\\s.*?http.*?(\\.(\\s|$|\"))";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        while (m.find( )) {
//            String[] arr = m.group().split("\\. ");
//            String result = arr[arr.length-1];
            String link = m.group().toLowerCase();
            all_links.add(link);
            if(link.contains(name.toLowerCase()) && !good_links.contains(link))
                good_links.add(link);
        }
        if(good_links.isEmpty())
            good_links.add(all_links.get(1));
        return good_links;
    }

    private String extractFundingSection(JSONObject xmlJSONObj) {
        ArrayList<String> section_title= new ArrayList<String>();
        String line = xmlJSONObj.toString().toLowerCase();
        String pattern1 = "\"title\":\"funding\"";
//        String pattern3 = ",\"funding:";
        Pattern r1 = Pattern.compile(pattern1);
        Matcher m1 = r1.matcher(line);
        while (m1.find( )) {
            section_title.add(m1.group());
        }
        String pattern2 = "\"title\":\"acknowledgement\"";
        Pattern r2 = Pattern.compile(pattern2);
        Matcher m2 = r2.matcher(line);
        while (m2.find( )) {
            section_title.add(m2.group());
        }
        String evaluation;
        boolean flag = (section_title.contains(pattern1));
        if(flag){
            evaluation = "funding";
        }
        else if(!flag && section_title.contains(pattern2)){
            evaluation = "acknowledgement";
        }
        else{
            evaluation = "None";
        }

        JSONArray funding_section = null;
        try {
            funding_section = xmlJSONObj.getJSONObject("article").getJSONObject("body").getJSONArray("sec");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for(int i=0;i<funding_section.length();i++)
        {
            String title = null;
            try {
                title = funding_section.getJSONObject(i).getString("title");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if(funding_section.getJSONObject(i).has("title") && title.toLowerCase().equals(evaluation)){
                    Object item = null;
                    try {
                        item = funding_section.getJSONObject(i).get("p");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (item instanceof String){
                        String funding_text = (String) item;
                        return funding_text;
                    }
                    else if (item instanceof JSONArray){
                        JSONArray funding_text = (JSONArray) item;
                        try {
                            return funding_text.getString(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "None";
    }

    public List<String> extractLicense(JSONObject xmlJSONObj) {
        ArrayList<String> arrayList= new ArrayList<String>();
        String funding_section = extractFundingSection(xmlJSONObj);
        String[] arr = funding_section.split("]");
        for(int i=0; i<arr.length-1; i++)
        {
            System.out.println(arr[i]);
            String result = arr[i].split("\\[")[1];
            arrayList.add(result);
        }

//        String pattern = "[^\\[].*[^\\]][.;]";
//        Pattern r = Pattern.compile(pattern);
//        Matcher m = r.matcher(funding_section);
//        while (m.find( )) {
//            System.out.println(m.group());
//            arrayList.add(m.group());
//        }

        return arrayList;
    }

}
