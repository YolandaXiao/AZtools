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

        Helper helper = new Helper();
        Attributes attr = helper.getAttr(test_pdf,complete_path);

        String doi = attr.getDOI();
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

    public Map<String,String> getLangMap() throws IOException, JSONException {
        Map<String,String> name2lang = new HashMap<>();
        String access_link = "http://dev.aztec.io:8983/solr/BD2K/select?q=(publicationDOI%3A*)AND(language%3A*)AND(codeRepoURL%3A*)&rows=100&wt=json&indent=true";
        //System.out.println(access_link);
        JSONObject data = readJsonFromUrl(access_link);
        //System.out.println("data "+data);
        JSONArray docs = data.getJSONObject("response").getJSONArray("docs");
        for(int i=0; i<docs.length();i++){
            JSONObject doc = docs.getJSONObject(i);
            String publicationDOI_old = doc.getString("publicationDOI");
            String[] arr = publicationDOI_old.split("/");
            String publicationDOI = arr[arr.length-1];
            String[] arr2 = publicationDOI.split("\"]");
            if(arr2.length>0){
                publicationDOI = arr2[0];
            }
            else{
                publicationDOI = "";
            }
            String lang = doc.getString("language");
            lang = lang.split("\"]")[0];
            lang = lang.split("\\[\"")[1];
//            System.out.println(publicationDOI);
//            System.out.println(lang);
            name2lang.put(publicationDOI,lang);
        }
        return name2lang;
    }

    @Test
    public void testLanguage() throws Exception {
        //create name to language map
        Map<String,String> name2lang = getLangMap();

        //keep track of numbers
        int match = 0;
        int non_match = 0;

        //iterate through files in directory and apply AZtools
        File dir = new File(abs_dir);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if(child.getName().contains(".pdf")){
                    String complete_path = child.getPath();
//                    System.out.println(complete_path);
                    String[] arr = complete_path.split("/");
                    String new_path = "../"+arr[arr.length-2]+"/"+arr[arr.length-1];
                    String file_name = arr[arr.length-1];
                    file_name=file_name.split(".pdf")[0];
//                    System.out.println(new_path);
                    List<String> lang = getAZtoolResult(new_path,complete_path);
//                    System.out.println("file_name: "+file_name);
//                    System.out.println("lang: "+lang);
                    if(!lang.isEmpty() && name2lang.containsKey(file_name)){
                        assertEquals(name2lang.get(file_name), lang.get(0));
                        if(name2lang.get(file_name).equals(lang.get(0))){
//                            System.out.println("Match!");
                            match++;
                        }
                        else{
//                            System.out.println("No Match!");
                            non_match++;
                        }
                    }
                    else{
                        System.out.println("No Match!");
                        non_match++;
                    }
                }
            }
        }
        double sum = match + non_match;
        double correct = match/sum;
//        double non_correct = 1-correct;
//        System.out.println("correct: "+correct);
//        System.out.println("non correct: "+non_correct);
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
