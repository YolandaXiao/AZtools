package extraction.language;

import extraction.url.Url;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        for (int i=0; i < url_links.size(); i++){
            //perform GET request to get the github link -> for github repo name search
            if(url_links.get(i).contains("github.com") || url_links.get(i).contains("sourceforge.com")){
                github_link = url_links.get(i);
                break;
            }
        }

        //if no github link found, go to other links to find github links
        if (github_link.equals("")) {
            for (int i=0; i < url_links.size(); i++) {
                try {
                    if (url_links.get(i).endsWith(".pdf")) {
                        continue;
                    }
                    String result = getHTML(url_links.get(i));
                    String pattern = "href=\"(?=[^\"]*github)([^\"]*)";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(result);
                    if (m.find()) {
                        github_link = m.group().split("\"")[1];
                    }
                }
                catch (Exception e) {
//                    System.out.println("Unable to GET " + url_links.get(i));
                }
            }
        }

        //if github link present, find programming language from github api
        if (github_link.contains("github.com")) {
            //use Github api to access language info
            String name = github_link.split("github.com")[1];
            //clean the github name
            if(name.contains("Contact")){
                name = name.split("Contact")[0];
            }
            String[] arr = name.split("/");
            if(arr.length>=4){
                name = arr[1]+"/"+arr[2];
            }
            String access_link = "https://api.github.com/search/repositories?q=" + name + "%20in:name&sort=stars&order=desc";
//            System.out.println(access_link);

//            Calendar readJsonFromUrl_start = Calendar.getInstance();
            JSONObject github_page = readJsonFromUrl(access_link);
//            Calendar readJsonFromUrl_end = Calendar.getInstance();
//            System.out.println("Time readJsonFromUrl: ");
//            System.out.println(readJsonFromUrl_end.getTimeInMillis() - readJsonFromUrl_start.getTimeInMillis());

            String new_page_info = "";
            if (github_page.getInt("total_count") != 0) {
                new_page_info = github_page.getJSONArray("items").getJSONObject(0).getString("languages_url");
            }
            else {
                return lan;
            }

            //access git language info
            JSONObject lang_info = readJsonFromUrl(new_page_info);
            Iterator<String> keys = lang_info.keys();
            String prev_key = (String)keys.next(); // First key in your json object
            int max = lang_info.getInt(prev_key);
            lan.add(prev_key);
//            System.out.println(prev_key);
            while (keys.hasNext()) {
                String key = (String)keys.next(); // First key in your json object
                int num = lang_info.getInt(key);
                if (num > max){
                    lan.remove(prev_key);
                    lan.add(key);
                    max = num;
                }
                prev_key = key;
            }
        }

        //sourceforge has SSL handshake error
        //if github_link contains sourceforge
//        if(github_link.contains("sourceforge")){
//
//            //use sourceforge api to access language info link
//            String name = github_link.split("sourceforge.net/projects")[1];
//            System.out.println(name);
//            String access_link = "https://sourceforge.net/rest/p"+name;
//            System.out.println(access_link);
//            System.setProperty("https.protocols", "TLSv1");
//            JSONObject github_page = readJsonFromUrl(access_link);
//            System.out.println("github_page "+github_page);
//            String key = github_page.getJSONObject("categories").getJSONArray("language").getJSONObject(0).getString("fullname");
//
//            lan.add(key);
//        }
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
        Calendar readAll_start = Calendar.getInstance();

        InputStream is = new URL(url).openStream();
        JSONObject json = null;

        Calendar readAll_end = Calendar.getInstance();
//        System.out.println("Time readAll: ");
//        System.out.println(readAll_end.getTimeInMillis() - readAll_start.getTimeInMillis());
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
        Calendar start = Calendar.getInstance();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        Calendar end = Calendar.getInstance();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(500); // timeout = 1 seconds
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }
}
