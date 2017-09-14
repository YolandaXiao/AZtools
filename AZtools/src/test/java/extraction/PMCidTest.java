package extraction;


/**
 * Created by yinxuexiao on 9/11/17.
 */

import extraction.funding.Funding;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PMCidTest {
    static final private String abs_dir = "/Users/yinxuexiao/Documents/Computer_Science/AZtools/AZtools/src/test/resources/github";
    static final private String output_file = abs_dir+"/result.txt";
    private ContentExtractor extractor;
    SAXBuilder saxBuilder;

    @Before
    public void setUp() throws AnalysisException, IOException {
        extractor = new ContentExtractor();
        saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");
    }

    //programming language extraction from our method
    public String getAZtoolResult(String test_pdf, String complete_path, PrintWriter writer) throws Exception {

        Helper helper = new Helper();
        Attributes attr = helper.getAttr(test_pdf,complete_path);

        //Name
        String name = attr.getName();
        System.out.println("name: "+name);

        //return links
        List<String> url = attr.getURL();
        for (int i = 0; i < url.size(); i++) {
            url.set(i, url.get(i).trim());
        }

        //return programming language
        List<String> lang = attr.getProgramming_lang();
        for (int i = 0; i < lang.size(); i++) {
            lang.set(i, lang.get(i).trim());
        }

        //write to file
        writer.write(name);
        writer.write(" | ");

        String url_line = "";
        for (int i = 0; i < url.size(); i++) {
            url_line += url.get(i);
        }
        writer.write(url_line + " | ");

        String programming_lang_line = "";
        for (int i = 0; i < lang.size(); i++) {
            programming_lang_line += lang.get(i);
        }
        writer.write(programming_lang_line + " | ");

        writer.write("\n");

        return name;
    }

    //get i to name map
    public Map<String,String> getMap(String fileName) throws IOException, JSONException {
        Map<String,String> map = new HashMap<>();

        String line = null;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            int i=1;
            while((line = bufferedReader.readLine()) != null) {
                map.put(Integer.toString(i),line);
                System.out.println(line);
                i++;
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        }
        catch(IOException ex) {System.out.println("Error reading file '" + fileName + "'");
        }

        return map;
    }

    @Test
    public void testName() throws Exception {

        Calendar start_time = Calendar.getInstance();

        //create name to language map
        Map<String,String> map = getMap(abs_dir+"/Name_data.txt");
        PrintWriter writer = new PrintWriter(new FileWriter(output_file), true);
//        Writer writer = new BufferedWriter(new OutputStreamWriter(
//                new FileOutputStream(output_file), "utf-8"));

        //keep track of numbers
        int match = 0;
        int non_match = 0;

        //iterate through files in directory and apply AZtools
        File dir = new File(abs_dir);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            int i = 1;
            for (File child : directoryListing) {
                if(child.getName().contains(".pdf")){
                    String complete_path = child.getPath();
                    String[] arr = complete_path.split("/");
                    String new_path = "../"+arr[arr.length-2]+"/"+arr[arr.length-1];
                    String file_name = arr[arr.length-1];
                    file_name=file_name.split(".pdf")[0];
                    writer.write(Integer.toString(i));
                    i++;
                    writer.write(" | ");
                    String test_name = getAZtoolResult(new_path,complete_path,writer);
                    System.out.println("file_name: "+file_name);
                    System.out.println("lang: "+test_name);
                    if(map.containsKey(file_name)){
                        String paper_name = map.get(file_name);
                        if(paper_name.equals(test_name) || paper_name.contains(test_name) || test_name.contains(paper_name)){
                            System.out.println("Match!");
                            match++;
                        }
                        else{
                            System.out.println("No Match!");
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
        double non_correct = 1-correct;
        System.out.println("correct: "+correct);
        System.out.println("non correct: "+non_correct);

        writer.write("correct: "+correct);
        writer.write("\n");
        writer.write("non correct: "+non_correct);
        writer.write("\n");

        Calendar end_time = Calendar.getInstance();
        System.out.println("Time: ");
        long time = end_time.getTimeInMillis() - start_time.getTimeInMillis();
        System.out.println(time);

        writer.write("Time: "+Long.toString(time)+" ms");
        writer.write("\n");

        writer.flush();
        writer.close();
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

