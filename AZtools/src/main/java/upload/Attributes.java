package upload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.XML;

public class Attributes {
    private final String title;
    private final List<String> author;
    private final List<String> affiliation;
    private final String abstrakt;
    private final List<String> contact;
    private final String DOI;
    private final int date;
    private final List<String> URL;
    private final List<funding_info> funding;

    class funding_info {

        public ArrayList<String> agency;
        public String license;

        public ArrayList<String> getAgency() {  return agency; }
        public String getLicense() { return license; }

        public funding_info(){}

        public void setAgency(ArrayList<String> agency) { this.agency = agency; }
        public void setLicense(String license) { this.license = license; }

    }

    public Attributes(String nlm, String name) throws Exception {
        JSONObject xmlJSONObj = XML.toJSONObject(nlm);
        this.title = extractTitle(xmlJSONObj);
        this.author = extractAuthor(xmlJSONObj);
        this.affiliation = extractAffiliation(xmlJSONObj);
        this.abstrakt = extractAbstract(xmlJSONObj);
        this.contact = extractContact(xmlJSONObj);
        this.DOI = extractDOI(xmlJSONObj);
        this.date = extractDate(xmlJSONObj);
        this.URL = extractURL(xmlJSONObj,name);
        this.funding = extractFunding(nlm);
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

    public List<funding_info> getFunding() { return funding; }

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

    //extract funding section from xlm
    private String extractMatch(String nlm, String name, String result){
        String pattern = "(?s)<title>"+name+".*?</p>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(nlm);
        while (m.find( )) {
            result = m.group();
        }
        return result;
    }

    //extract funding section from xlm using the above helper function
    private String extractFundingSection(String nlm){
        String result = "None";
        result = extractMatch(nlm,"ACKNOWLEDG", result);
        result = extractMatch(nlm,"acknowledg", result);
        result = extractMatch(nlm,"Acknowledg", result);
        result = extractMatch(nlm,"Funding", result);
        result = extractMatch(nlm,"FUNDING", result);
        result = extractMatch(nlm,"funding", result);
        return result;
    }

    public List<funding_info> extractFunding(String nlm) throws Exception {
        ArrayList<funding_info> arrayList= new ArrayList<funding_info>();
        String funding_section = extractFundingSection(nlm);
        //just for license
        //String pattern = "[\\dA-Z\\/\\-\\s]{2,}[\\d\\/\\-\\s]{2,}[\\dA-Z\\/\\-]{2,}";

        //for license and agency
        String pattern = "(by|[^.])[A-Z]+[a-z\\s]+[^.]*?([\\dA-Z\\/\\-\\s]{2,}[\\d\\/\\-\\s,]{2,}[\\dA-Z\\/\\-]{2,})";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(funding_section);
        while (m.find( )) {
            //result is license + name
            String result = m.group();
            funding_info fi = new funding_info();

            //get agency from stanford ner
            ExtractDemo extractDemo = new ExtractDemo();
            String funding = extractDemo.doNer(result);
            String pattern2 = "<ORGANIZATION>.*?<\\/ORGANIZATION>";
            Pattern r2 = Pattern.compile(pattern2);
            Matcher m2 = r2.matcher(funding);
            ArrayList<String> arr= new ArrayList<String>();
            while (m2.find( )) {
                String agency = m2.group().split(">")[1].split("<")[0];
                arr.add(agency);
            }
            fi.setAgency(arr);

            //get license from regex
            String pattern1 = "[\\dA-Z\\/\\-\\s]{2,}[\\d\\/\\-\\s]{2,}[\\dA-Z\\/\\-]{2,}";
            Pattern r1 = Pattern.compile(pattern1);
            Matcher m1 = r1.matcher(result);
            while (m1.find( )) {
                fi.setLicense(m1.group());
            }
            arrayList.add(fi);
        }
        return arrayList;
    }

}
