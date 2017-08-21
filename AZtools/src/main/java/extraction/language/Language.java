package extraction.language;

import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import extraction.url.Url;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yinxuexiao on 8/10/17.
 */
public class Language {

    private List<String> language;

    public List<String> getLanguage() {  return language; }

    public Language(JSONObject xmlJSONObj, String name) throws Exception {
        this.language = extractProgramming_lang(xmlJSONObj, name);
    }

    private List<String> extractProgramming_lang(JSONObject xmlJSONObj, String pdf_name) throws Exception {
        ArrayList<String> lan = new ArrayList<>();
        Url url_link = new Url(xmlJSONObj, pdf_name);
        List<String> url_links = url_link.getUrl();
        String github_link = "";

        //iterate through all links to get github link
        for(int i=0;i<url_links.size();i++){
            //perform GET request to get the github link -> for github repo name search
            if(url_links.get(i).contains("github")){
                github_link = url_links.get(i);
                break;
            }
        }
        //if no github link found, go to other links to find github links
        if(github_link==""){
            for(int i=0;i<url_links.size();i++){
                //System.out.println(url_links.get(i));
                try{
                    String result = getHTML(url_links.get(i));
                    //System.out.println(result);
                    String pattern = "href=\"(?=[^\"]*github)([^\"]*)";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(result);
                    if (m.find( )) {
                        //System.out.println(m.group());
                        github_link =  m.group().split("\"")[1];
                        //System.out.println(github_link);
                    }
                }
                catch (Exception e) {}
            }
        }

        //if github link present, find programming language from github api
        if(github_link!=""){
            //use Github api to access language info link
            String name = github_link.split("github.com")[1];
            //System.out.println(name);
            String access_link = "https://api.github.com/search/repositories?q="+name+"%20in:name&sort=stars&order=desc";
            //System.out.println(access_link);
            JSONObject github_page = readJsonFromUrl(access_link);
            //System.out.println("github_page "+github_page);
            String new_page_info = github_page.getJSONArray("items").getJSONObject(0).getString("languages_url");
            //System.out.println("new_page_info "+new_page_info);

            //access git language info
            JSONObject lang_info = readJsonFromUrl(new_page_info);
            Iterator<String> keys = lang_info.keys();
            String prev_key = (String)keys.next(); // First key in your json object
            int max = lang_info.getInt(prev_key);
            lan.add(prev_key);
            while( keys.hasNext() ){
                String key = (String)keys.next(); // First key in your json object
                int num = lang_info.getInt(key);
//                System.out.println(key + ": " + num);

                if(num>max){
                    lan.remove(prev_key);
                    lan.add(key);
                }
                prev_key = key;
            }
        }
        return lan;
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

    //helper function: get HTML content
    private static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }
}