package extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by yinxuexiao on 8/17/17.
 */

public class LanguagesTest {

    static final private String abs_dir = "/Users/yinxuexiao/Documents/Computer_Science/AZtools/AZtools/src/test/resources/lang_files";
    private ContentExtractor extractor;
    SAXBuilder saxBuilder;

    @Before
    public void setUp() throws AnalysisException, IOException {
        extractor = new ContentExtractor();
        saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");
    }

    //programming language extraction from our method
    public List<String> getAZtoolResult(String test_pdf, String complete_path) throws Exception {

        // apply cermine
        ContentExtractor extractor = new ContentExtractor();
        FileInputStream fis = new FileInputStream(complete_path);
        InputStream inputStream = new BufferedInputStream(fis);
        extractor.setPDF(inputStream);

        Element nlmMetadata = extractor.getMetadataAsNLM();
        Element nlmFullText = extractor.getBodyAsNLM(null);
        Element nlmContent = new Element("article");

        for (Object ns : nlmFullText.getAdditionalNamespaces()) {
            if (ns instanceof Namespace) {
                nlmContent.addNamespaceDeclaration((Namespace) ns);
            }
        }

        Element meta = (Element) nlmMetadata.getChild("front").clone();
        nlmContent.addContent(meta);
        nlmContent.addContent(nlmFullText);

        String nlm = new XMLOutputter().outputString(nlmContent);
        System.out.println(nlm);
        String[] arr = test_pdf.split("/");
        String filename = arr[arr.length-1];
        System.out.println("file_name: "+filename);
        Attributes attr = new Attributes(nlm, filename,0);
        String doi = attr.getDOI();
        System.out.println("doi: "+doi);
        List<String> lang = attr.getProgramming_lang();
        for (int i = 0; i < lang.size(); i++) {
            lang.set(i, lang.get(i).trim());
        }
//        for (int i = 0; i < lang.size(); i++) {
//            System.out.println(lang.get(i).trim());
//            lang.set(i, lang.get(i).trim());
//        }
//        List<String> lang = new ArrayList<String>(attr.getProgramming_lang());
//        List<String> lang = new ArrayList<>();
//        lang.addAll(attr.getProgramming_lang());
//        System.out.println("language size: "+lang.size());
//        if(lang.size()>0){
//            System.out.println("language of file: "+lang.get(0));
//            return lang.get(0);
//        }
        return lang;
    }

//    @Test
    public Map<String,String> getLangMap() throws IOException, JSONException {
        Map<String,String> name2lang = new HashMap<>();
        String access_link = "http://dev.aztec.io:8983/solr/BD2K/select?q=(publicationDOI%3A*)AND(language%3A*)AND(codeRepoURL%3A*)&wt=json&indent=true";
        //System.out.println(access_link);
        JSONObject data = readJsonFromUrl(access_link);
        //System.out.println("data "+data);
        JSONArray docs = data.getJSONObject("response").getJSONArray("docs");
        for(int i=0; i<docs.length();i++){
            JSONObject doc = docs.getJSONObject(i);
            String publicationDOI_old = doc.getString("publicationDOI");
            String[] arr = publicationDOI_old.split("/");
            String publicationDOI = arr[arr.length-1];
            String lang = doc.getString("language");
            System.out.println(publicationDOI);
            System.out.println(lang);
            name2lang.put(publicationDOI,lang);
        }
        return name2lang;
    }

    @Test
    public void testLanguage() throws Exception {
        //create name to language map
//        Map<String,String> name2lang = getLangMap();

        //iterate through files in directory and apply AZtools
        File dir = new File(abs_dir);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if(child.getName().contains(".pdf")){
                    String complete_path = child.getPath();
                    System.out.println(complete_path);
                    String[] arr = complete_path.split("/");
                    String new_path = "../"+arr[arr.length-2]+"/"+arr[arr.length-1];
                    String file_name = arr[arr.length-1];
                    System.out.println(new_path);
                    List<String> lang = getAZtoolResult(new_path,complete_path);
                    System.out.println("file_name: "+file_name);
                    System.out.println("lang: "+lang);
//                    if(name2lang.get(file_name)==lang){
//                        System.out.println("Match!");
//                    }
//                    else{
//                        System.out.println("No Match!");
//                    }
                }
            }
        }
    }

    //helper function: read in lines
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    //helper function: give json url, return json string
    private static JSONObject readJsonFromUrl(String url) throws IOException {
        // String s = URLEncoder.encode(url, "UTF-8");
        // URL url = new URL(s);
        InputStream is = new URL(url).openStream();
        JSONObject json = null;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            json = new JSONObject(jsonText);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }
        return json;
    }



}
