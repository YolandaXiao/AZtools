package upload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Attributes {
    private final String title;
    private final String name;
    private final List<String> author;
    private final List<String> affiliation;
    private final String abstrakt;
    private final List<String> contact;
    private final String DOI;
    private final int date;
    private final List<String> URL;
    private final List<funding_info> funding;
    private final List<String> programming_lang;

    // ------------------------------------------------------------- //

    class funding_info {

        public String agency;
        public String license;

        public String getAgency() {  return agency; }
        public String getLicense() { return license; }

        public funding_info(){}

        public void setAgency(String agency) { this.agency = agency; }
        public void setLicense(String license) { this.license = license; }
    }

    public Attributes(String nlm, String name) throws Exception {
        JSONObject xmlJSONObj = XML.toJSONObject(nlm);

        this.title = extractTitle(xmlJSONObj).trim();

        this.author = extractAuthor(xmlJSONObj);
        for (int i = 0; i < this.author.size(); i++) {
            this.author.set(i, this.author.get(i).trim());
        }

        this.affiliation = extractAffiliation(xmlJSONObj);
        for (int i = 0; i < this.affiliation.size(); i++) {
            this.affiliation.set(i, this.affiliation.get(i).trim());
        }

        this.abstrakt = extractAbstract(xmlJSONObj).trim();
        this.contact = extractContact(xmlJSONObj);
        for (int i = 0; i < this.contact.size(); i++) {
            this.contact.set(i, this.contact.get(i).trim());
        }

        this.DOI = extractDOI(xmlJSONObj).trim();
        this.date = extractDate(xmlJSONObj);

        this.URL = extractURL(xmlJSONObj);
        for (int i = 0; i < this.URL.size(); i++) {
            this.URL.set(i, this.URL.get(i).trim());
        }

        this.funding = extractFunding(nlm);
        this.programming_lang = extractProgramming_lang(xmlJSONObj);
        for (int i = 0; i < this.programming_lang.size(); i++) {
            this.programming_lang.set(i, this.programming_lang.get(i).trim());
        }

        NameNLP obj = new NameNLP(name, this.title, this.URL);
        this.name = obj.getName().trim();
    }

    // ------------------------------------------------------------ //

    public String getTitle(){
        return title;
    }

    public String getName() {
        return name;
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

    public List<String> getProgramming_lang(){
        return programming_lang;
    }

    // ----------------------------------------------------------- //

    private String extractTitle(JSONObject xmlJSONObj) {
        String title = null;
        try {
            title = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("title-group").getString("article-title");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return title;
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

    private List<String> extractAffiliation(JSONObject xmlJSONObj) {
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

    private String extractAbstract(JSONObject xmlJSONObj) {
        String abstrakt = null;
        try {
            abstrakt = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("abstract").getString("p");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return abstrakt;
    }

    private List<String> extractContact(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONArray contacts = null;
        try {
            contacts = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group").getJSONArray("contrib");
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    private String extractDOI(JSONObject xmlJSONObj) {
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

    private int extractDate(JSONObject xmlJSONObj) {
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

    public List<String> extractURL(JSONObject xmlJSONObj) {
        String name = getTitle();
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
            String link = m.group();
            System.out.println(link);
            all_links.add(link);
            if(link.contains(name.toLowerCase()) && !good_links.contains(link))
                good_links.add(link);
        }
        if(good_links.isEmpty() && all_links.size()>1)
            good_links.add(all_links.get(1));
        return good_links;
    }

    //extract funding section from xlm
    private String extractMatch(String nlm, String name, String result){
        String pattern = "(?s)"+name+".*?</p>";
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
        result = extractMatch(nlm,"<title>ACKNOWLEDG", result);
        result = extractMatch(nlm,"<title>acknowledg", result);
        result = extractMatch(nlm,"<title>Acknowledg", result);
        result = extractMatch(nlm,"<p>ACKNOWLEDG", result);
        result = extractMatch(nlm,"<p>acknowledg", result);
        result = extractMatch(nlm,"<p>Acknowledg", result);
        result = extractMatch(nlm,"<title>Funding", result);
        result = extractMatch(nlm,"<title>FUNDING", result);
        result = extractMatch(nlm,"<title>funding", result);
        result = extractMatch(nlm,"<p>Funding:", result);
        result = extractMatch(nlm,"<p>funding:", result);
        result = extractMatch(nlm,"<p>FUNDING:", result);
        return result;
    }

    //helper function: get list of agency names
    private ArrayList<String> getAgencyDic() throws IOException {
        String agencyNamesFile = Properties.getAgencyNamesFileName();
        BufferedReader br = new BufferedReader(new FileReader(agencyNamesFile));
        ArrayList<String> agency_dic= new ArrayList<String>();
        try {
            String line = br.readLine();

            while (line != null) {
                agency_dic.add(line);
                line = br.readLine();
            }
        } finally {
            br.close();
        }
        return agency_dic;
    }

    private List<funding_info> extractFunding(String nlm) throws Exception {
        ArrayList<funding_info> arrayList= new ArrayList<funding_info>();
        String funding_section = extractFundingSection(nlm);
        // System.out.println(funding_section);

        //get license
        String pattern = "([\\dA-Z\\/\\-\\s]{2,}[\\d\\/\\-\\s]{2,}[\\dA-Z\\/\\-]{2,})";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(funding_section);
        String agency = null;
        while (m.find( )) {
            //get license and sentence before that contains agency
            funding_info fi = new funding_info();
            String license = m.group();
            fi.setLicense(license);
            String[] arr = funding_section.split(license);
            String agency_sentence = arr[0];
            if (arr.length >= 2) {
                funding_section = arr[1];
            }
            else{
                funding_section = arr[0];
            }

            // get agency from stanford ner
            ExtractDemo extractDemo = new ExtractDemo();
            String funding = extractDemo.doNer(agency_sentence);
            //System.out.println(funding);
            String pattern2 = "(?s)<ORGANIZATION>.*?<\\/ORGANIZATION>";
            Pattern r2 = Pattern.compile(pattern2);
            Matcher m2 = r2.matcher(funding);
            while (m2.find( )) {
                String[] temp = m2.group().split(">");
                agency = temp[temp.length-1].split("<")[0];
            }
            fi.setAgency(agency);
            arrayList.add(fi);
        }

        //run NER on the entire paragraph again to get agencies without grant number
        funding_section = extractFundingSection(nlm);
        ExtractDemo extractDemo = new ExtractDemo();
        String funding = extractDemo.doNer(funding_section);
        String pattern2 = "(?s)<ORGANIZATION>.*?<\\/ORGANIZATION>";
        Pattern r2 = Pattern.compile(pattern2);
        Matcher m2 = r2.matcher(funding);
        while (m2.find( )) {
            funding_info f = new funding_info();
            String ag = m2.group().split(">")[1].split("<")[0];
            f.setAgency(ag);
            //check if the name already exists in the list
            boolean flag = true;
            for(int i = 0; i < arrayList.size(); i++) {
                if(arrayList.get(i).getAgency()!=null ){
                    if(arrayList.get(i).getAgency().equals(f.getAgency())){
                        flag = false;
                    }
                }
            }
            //check if the name exists in dictionary
            if(flag && getAgencyDic().contains(f.getAgency())){
                arrayList.add(f);
            }
        }

        return arrayList;
    }

    private List<String> extractProgramming_lang(JSONObject xmlJSONObj) {
        ArrayList<String> i = new ArrayList<String>();
        return i;
    }
}
