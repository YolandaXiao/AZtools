package extraction;

import extraction.affiliation.Affiliation;
import extraction.author.Author;
import extraction.contact.Contact;
import extraction.date.Date;
import extraction.doi.DOI;
import extraction.funding.Funding;
import extraction.funding.FundingInfo;
import extraction.language.Language;
import extraction.name.NameNLP;
import extraction.summary.Summary;
import extraction.title.Title;
import extraction.url.Url;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.util.List;

public class Attributes {
    private static String title;
//    private final String title;
    private final String name;
    private final String summary;
    private final List<String> author;
    private final List<String> affiliation;
    private final String abstrakt;
    private final List<String> contact;
    private final String doi;
    private final int date;
    private final List<String> URL;
    private final List<FundingInfo> funding;
    private final List<String> programming_lang;

    // ------------------------------------------------------------- //

    public Attributes(String nlm, String filename) throws Exception {
        JSONObject xmlJSONObj = XML.toJSONObject(nlm);

        Title t = new Title(xmlJSONObj);
        title = t.getTitle();

        Author au = new Author(xmlJSONObj);
        this.author = au.getAuthor();
        for (int i = 0; i < this.author.size(); i++) {
            this.author.set(i, this.author.get(i).trim());
        }

        Affiliation aff = new Affiliation(xmlJSONObj);
        this.affiliation = aff.getAffiliation();
        for (int i = 0; i < this.affiliation.size(); i++) {
            this.affiliation.set(i, this.affiliation.get(i).trim());
        }

        Contact con = new Contact(xmlJSONObj);
        this.contact = con.getContact();
        for (int i = 0; i < this.contact.size(); i++) {
            this.contact.set(i, this.contact.get(i).trim());
        }

        DOI d2 = new DOI(xmlJSONObj);
        this.doi = d2.getDoi();

        Date d = new Date(xmlJSONObj);
        this.date = d.getDate();

        Funding f = new Funding(nlm);
        this.funding = f.getFunding();

        Url url_link = new Url(xmlJSONObj, filename);
        this.URL = url_link.getUrl();
        for (int i = 0; i < this.URL.size(); i++) {
            this.URL.set(i, this.URL.get(i).trim());
        }

        NameNLP obj = new NameNLP(filename, title);//, this.URL);
        this.name = obj.getName().trim();

        this.abstrakt = extractAbstract(xmlJSONObj).trim();
        //summary must necessarily come after abstract
        System.out.println("Finding summary of tool...");
        Summary summ = new Summary(abstrakt, filename, name);
        this.summary = summ.getSummary();
        System.out.println("Done with summary");

        Language lan = new Language(xmlJSONObj, filename);
        this.programming_lang = lan.getLanguage();
        for (int i = 0; i < this.programming_lang.size(); i++) {
            this.programming_lang.set(i, this.programming_lang.get(i).trim());
        }
    }

    // ------------------------------------------------------------ //

    public static String getTitle(){
        return title;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
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
        return doi;
    }

    public int getDate() {
        return date;
    }

    public List<String> getURL(){
        return URL;
    }

    public List<FundingInfo> getFunding() { return funding; }

    public List<String> getProgramming_lang(){
        return programming_lang;
    }

    // ----------------------------------------------------------- //

    private String extractAbstract(JSONObject xmlJSONObj) {
        String abstrakt = null;
        try {
            abstrakt = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("abstract").getString("p");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return abstrakt;
    }

    public void printFunding() {
        for(int i=0; i<funding.size(); i++){
            System.out.println(funding.get(i).agency);
            System.out.println(funding.get(i).license);
        }
    }
}
