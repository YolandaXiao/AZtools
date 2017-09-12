package extraction;

import extraction.abstrakt.Abstract;
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
import extraction.tags.Tags;
import extraction.title.Title;
import extraction.url.Url;
import org.json.JSONObject;
import org.json.XML;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Attributes implements Runnable {
    private final String title;
    private final String name;
    private final String abstrakt;
    private final String summary;
    private final List<String> author;
    private final List<String> affiliation;
    private final List<String> contact;
    private final String doi;
    private final String date;
    private final List<String> URL;
    private final List<String> tags;
    private List<String> funding;
    private List<String> languages;

    private final JSONObject xmlJSONObj;
    private final String m_nlm;
    private List<FundingInfo> funding_list;

    // ------------------------------------------------------------- //

    public Attributes(String nlm, String filename, int num) throws Exception {
        m_nlm = nlm;
        xmlJSONObj = XML.toJSONObject(nlm);
        funding = null;
        languages = null;

        run();

        Calendar title_start = Calendar.getInstance();
        Title t = new Title(xmlJSONObj);
        title = t.getTitle();
        Calendar title_end = Calendar.getInstance();
        Calendar author_start = Calendar.getInstance();
        Author au = new Author(xmlJSONObj,num);
        this.author = au.getAuthor();
        for (int i = 0; i < this.author.size(); i++) {
            this.author.set(i, this.author.get(i).trim());
        }
        Calendar author_end = Calendar.getInstance();
        Calendar aff_start = Calendar.getInstance();
        Affiliation aff = new Affiliation(xmlJSONObj,num);
        this.affiliation = aff.getAffiliation();
        for (int i = 0; i < this.affiliation.size(); i++) {
            this.affiliation.set(i, this.affiliation.get(i).trim());
        }
        Calendar aff_end = Calendar.getInstance();
        Calendar contact_start = Calendar.getInstance();
        Contact con = new Contact(nlm,num);
        this.contact = con.getContact();
        for (int i = 0; i < this.contact.size(); i++) {
            this.contact.set(i, this.contact.get(i).trim());
        }
        Calendar contact_end = Calendar.getInstance();
        Calendar doi_start = Calendar.getInstance();
        DOI d2 = new DOI(xmlJSONObj, num);
        this.doi = d2.getDoi();
        Calendar doi_end = Calendar.getInstance();
        Calendar date_start = Calendar.getInstance();
        Date d = new Date(xmlJSONObj,num);
        this.date = d.getDate();
        Calendar date_end = Calendar.getInstance();

        Calendar funding_start = Calendar.getInstance();
        Funding f = new Funding(nlm,num);
        this.funding_list = f.getFunding();
        Calendar funding_end = Calendar.getInstance();

        Calendar url_start = Calendar.getInstance();
        Url url_link = new Url(xmlJSONObj, filename);
        this.URL = url_link.getUrl();
        for (int i = 0; i < this.URL.size(); i++) {
            this.URL.set(i, this.URL.get(i).trim());
        }
        Calendar url_end = Calendar.getInstance();

        Calendar name_start = Calendar.getInstance();
        NameNLP obj = new NameNLP(title, URL);
        this.name = obj.getName().trim();
        Calendar name_end = Calendar.getInstance();

        Calendar abstract_start = Calendar.getInstance();
        Abstract a = new Abstract(xmlJSONObj,num);
        this.abstrakt = a.getAbstrakt().trim();
        Calendar abstract_end = Calendar.getInstance();

        Calendar summary_start = Calendar.getInstance();
        //summary must necessarily come after abstract
        Summary summ = new Summary(abstrakt, filename, name);
        this.summary = summ.getSummary();
        Calendar summary_end = Calendar.getInstance();

        Calendar lang_start = Calendar.getInstance();
        Language lan = new Language(xmlJSONObj, filename);
        this.languages = lan.getLanguage();
        for (int i = 0; i < this.languages.size(); i++) {
            this.languages.set(i, this.languages.get(i).trim());
        }
        Calendar lang_end = Calendar.getInstance();

        Calendar tags_start = Calendar.getInstance();
        Tags tag_var = new Tags(abstrakt);
        tags = tag_var.getTags();
        Calendar tags_end = Calendar.getInstance();

        // If < 10ms, comment it
//        System.out.println("Time title: ");
//        System.out.println(title_end.getTimeInMillis() - title_start.getTimeInMillis());
//        System.out.println("Time author: ");
//        System.out.println(author_end.getTimeInMillis() - author_start.getTimeInMillis());
//        System.out.println("Time affiliation: ");
//        System.out.println(aff_end.getTimeInMillis() - aff_start.getTimeInMillis());
//        System.out.println("Time contact: ");
//        System.out.println(contact_end.getTimeInMillis() - contact_start.getTimeInMillis());
//        System.out.println("Time doi: ");
//        System.out.println(doi_end.getTimeInMillis() - doi_start.getTimeInMillis());
//        System.out.println("Time date: ");
//        System.out.println(date_end.getTimeInMillis() - date_start.getTimeInMillis());
//        System.out.println("Time url: ");
//        System.out.println(url_end.getTimeInMillis() - url_start.getTimeInMillis());
//        System.out.println("Time name: ");
//        System.out.println(name_end.getTimeInMillis() - name_start.getTimeInMillis());
//        System.out.println("Time abstract: ");
//        System.out.println(abstract_end.getTimeInMillis() - abstract_start.getTimeInMillis());
//        System.out.println("Time summary: ");
//        System.out.println(summary_end.getTimeInMillis() - summary_start.getTimeInMillis());
    }

    // ------------------------------------------------------------ //

    public void run() {
        try {
            Calendar funding_start = Calendar.getInstance();
            Funding f = new Funding(m_nlm, 0);
            funding_list = f.getFunding();
            Calendar funding_end = Calendar.getInstance();
//            System.out.println("Time funding: ");
//            System.out.println(funding_end.getTimeInMillis() - funding_start.getTimeInMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTitle(){
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

    public String getDate() {
        return date;
    }

    public List<String> getURL(){
        return URL;
    }

    private List<FundingInfo> getFundingList() {
        return funding_list;
    }

    public List<String> getFundingStr() {
        List<String> fa = new ArrayList<>();
        for (FundingInfo fi : getFundingList()) {
            fa.add(fi.toString());
        }
        funding = fa;
        return funding;
    }

    public List<String> getLanguages(){
        return languages;
    }

    public List<String> getTags(){
        return tags;
    }

}
