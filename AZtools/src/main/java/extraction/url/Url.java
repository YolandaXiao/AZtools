package extraction.url;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Url {

    private List<String> url;

    public List<String> getUrl() {  return url; }

    public Url(JSONObject xmlJSONObj, String name) throws Exception {
        this.url = extractURL(xmlJSONObj, name);
    }

    private List<String> extractURL(JSONObject xmlJSONObj, String name) {
        ArrayList<String> all_links= new ArrayList();
        ArrayList<String> good_links= new ArrayList();
        try {
            name = name.split(".pdf")[0];

        //check URLs without http
        String line = xmlJSONObj.toString();
        line = line.replaceAll("\\\\","");
        //String pattern = "(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";
        String pattern = "[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&\\/=]*)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        while (m.find( )) {
            String link = m.group();
            if(!link.contains("w3") && !link.contains("creativecommons") && !link.contains("@") && !link.contains("niso")
                    && !link.contains(".xlsx") && !link.contains(".html") && !link.contains(".pdf") && !link.contains(".gz")
                    && !link.contains(".jpg") && !link.contains(".avi")){
                if(!all_links.contains(link)){
                    all_links.add(link);
                    System.out.println("all_links "+link);
                }
            }
            String lowercase_link = link.toLowerCase();
            if (null != name && name.split(".pdf").length >= 1) {
                name = name.split(".pdf")[0];
            } else {
                continue;
            }
            //only when name is accurate
//            if(lowercase_link.contains(name.toLowerCase()) && !good_links.contains(link)){
//                System.out.println("good_links1 "+link);
//                good_links.add(link);
//            }
            if((link.contains("github") || link.contains("sourceforge") || link.contains("bioconductor") || link.contains("bitbucket")) && !good_links.contains(link)){
                if(link.contains("Contact")){
                    String[] arr = link.split("Contact");
                    if(arr.length>1)
                        link = link.split("Contact")[0];
                }
                System.out.println("good_links2 "+link);
                good_links.add(link);
                break;
            }
        }

            //add one link to good_links if none found
            if (good_links.isEmpty() && all_links.size() > 0) {
                good_links.add(all_links.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return good_links;
    }

    public String getStatus(List<String> good_links) throws Exception {
        //check if good_links are valid
        for(int i=0;i<good_links.size();i++){
            if(checkLink(good_links.get(i))==false){
                return "Github link no longer valid";
            }
        }
        return "Success";
    }

    //helper function: get HTML content
    private boolean checkLink(String urlToRead) throws Exception {
        try{
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1500);
            int code = conn.getResponseCode() ;
            if(code==404){
                return false;
            }
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

}
