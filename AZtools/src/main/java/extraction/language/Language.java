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

        //consider edge case when bioconductor is involved
        String json_string = xmlJSONObj.toString();
        if(json_string.toLowerCase().contains("bioconductor")){
            lan.add("R");
            return lan;
        }

        //iterate through all links to get github link
        for (int i=0; i < url_links.size(); i++){
            //perform GET request to get the github link -> for github repo name search
            if(url_links.get(i).contains("github.com") || url_links.get(i).contains("sourceforge.net") || url_links.get(i).contains("bitbucket.org") || url_links.get(i).contains("bioconductor")){
                github_link = url_links.get(i);
                break;
            }
        }

        //if no github link found, iterate through all links to find github links
        if (github_link.equals("")){
            for(int i=0;i<url_links.size();i++){
                try{
                    String link = url_links.get(i);
                    if(link.contains("Contact")){
                        link = link.split("Contact")[0];
                    }
                    System.out.println("find github link: "+ link);
                    String result = getHTML(link);

                    //pattern1 for github
                    if(link.contains("github")) {
                        String pattern = "href=\"(?=[^\"]*github)([^\"]*)";
                        Pattern r = Pattern.compile(pattern);
                        Matcher m = r.matcher(result);
                        if (m.find()) {
                            System.out.println(m.group());
                            github_link = m.group().split("\"")[1];
                            break;
                        }
                    }
                    else if(link.contains("sourceforge")){
                        //pattern2 for sourceforge
                        String pattern2 = "href=\"(?=[^\"]*sourceforge.net/projects)([^\"]*)";
                        Pattern r2 = Pattern.compile(pattern2);
                        Matcher m2 = r2.matcher(result);
                        if (m2.find( )) {
                            System.out.println(m2.group());
                            github_link =  m2.group().split("\"")[1];
                            break;
                        }
                    }
                    else{
                        String pattern = "href=\"(?=[^\"]*github)([^\"]*)";
                        Pattern r = Pattern.compile(pattern);
                        Matcher m = r.matcher(result);
                        if (m.find()) {
                            System.out.println(m.group());
                            github_link = m.group().split("\"")[1];
                            break;
                        }
                        String pattern2 = "href=\"(?=[^\"]*sourceforge.net/projects)([^\"]*)";
                        Pattern r2 = Pattern.compile(pattern2);
                        Matcher m2 = r2.matcher(result);
                        if (m2.find( )) {
                            System.out.println(m2.group());
                            github_link =  m2.group().split("\"")[1];
                            break;
                        }
                    }
                }
                catch (Exception e) {
//                    System.out.println("Unable to GET " + url_links.get(i));
                }
            }
        }

        //if github link present, find programming language from github api
        if (github_link.contains("github.com")) {
            if(github_link.split("github.com").length>1){
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
                String access_link = "https://api.github.com/search/repositories?q="+name+"%20in:name&sort=stars&order=desc";
                System.out.println(access_link);
                JSONObject github_page = readJsonFromUrl(access_link);
                String new_page_info = "";
                if (github_page.getInt("total_count")!=0){
                    new_page_info = github_page.getJSONArray("items").getJSONObject(0).getString("languages_url");
                }
                else {
                    return lan;
                }

                //access git language info
                try{
                    JSONObject lang_info = readJsonFromUrl(new_page_info);
                    Iterator<String> keys = lang_info.keys();
                    String prev_key = (String)keys.next(); // First key in your json object
                    int max = lang_info.getInt(prev_key);
                    lan.add(prev_key);
                    System.out.println(prev_key+": "+max);
                    while (keys.hasNext()) {
                        String key = (String)keys.next(); // First key in your json object
                        int num = lang_info.getInt(key);
                        System.out.println(key+": "+num);
                        if (num > max){
                            lan.remove(prev_key);
                            lan.add(key);
                            max = num;
                            prev_key = key;
                        }
                    }
                }
                catch (Exception e){

                }
            }
        }
        //sourceforge has SSL handshake error
        //if github_link contains sourceforge
        else if(github_link.contains("sourceforge.net")){

            if(github_link.contains("Contact")){
                github_link = github_link.split("Contact")[0];
            }
            //use sourceforge api to access language info link
            String name = github_link.split("sourceforge.net/projects")[1];
            System.out.println(name);
            String[] arr = name.split("/");
            if(arr.length>=3){
                name = "/"+arr[1];
            }
            System.out.println(name);
            String access_link = "https://sourceforge.net/rest/p"+name;
            System.out.println(access_link);
            System.setProperty("https.protocols", "TLSv1");
            JSONObject github_page = readJsonFromUrl(access_link);
            System.out.println("github_page "+github_page);
            String key = github_page.getJSONObject("categories").getJSONArray("language").getJSONObject(0).getString("fullname");
            lan.add(key);
        }
        else if(github_link.contains("bioconductor")){
            lan.add("R");
        }
        else if (github_link.contains("bitbucket.org")) {
//            github_link = "https://bitbucket.org/booz-allen-sci-comp-team/cl-dash.git";
            if(github_link.split("bitbucket.org/").length>1){
                String name = github_link.split("bitbucket.org/")[1];
                String access_link = "https://api.bitbucket.org/2.0/repositories/"+name;
                System.out.println(access_link);
                JSONObject github_page = readJsonFromUrl(access_link);
                String key = github_page.getString("language");
                lan.add(key);
            }
        }

        Calendar findprogramminglan_end = Calendar.getInstance();
//        System.out.println("Time findprogramminglan: ");
//        System.out.println(findprogramminglan_end.getTimeInMillis() - findprogramminglan_start.getTimeInMillis());
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
        try{
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "google.com");

            System.out.println("Request URL ... " + url);
            boolean redirect = false;
            // normally, 3xx is redirect
            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }
            System.out.println("Response Code ... " + status);

            if (redirect) {

                // get redirect url from "location" header field
                String newUrl = conn.getHeaderField("Location");

                // get the cookie if need, for login
                String cookies = conn.getHeaderField("Set-Cookie");

                // open the new connnection again
                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                conn.setRequestProperty("Cookie", cookies);
                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.addRequestProperty("User-Agent", "Mozilla");
                conn.addRequestProperty("Referer", "google.com");

                System.out.println("Redirect to URL : " + newUrl);

            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer html = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                html.append(inputLine);
            }
            in.close();

            System.out.println("URL Content... \n" + html.toString());
            System.out.println("Done");

            return html.toString();
        }
        catch (Exception e){
            return "";
        }
    }

//    //helper function: get HTML content
//    private static String getHTML(String urlToRead) throws Exception {
//        try{
//            StringBuilder result = new StringBuilder();
//            URL url = new URL(urlToRead);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            conn.setConnectTimeout(3000); // timeout = 3 seconds
//            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            String line;
//            while ((line = rd.readLine()) != null) {
//                result.append(line);
//            }
//            rd.close();
//            return result.toString();
//        }
//        catch (Exception e){
//            return "";
//        }
//    }

}
