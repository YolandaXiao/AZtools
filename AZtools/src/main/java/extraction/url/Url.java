package extraction.url;

import org.json.JSONObject;
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
        ArrayList<String> all_links= new ArrayList<>();
        ArrayList<String> good_links= new ArrayList<>();
        name = name.split(".pdf")[0];

        String line = xmlJSONObj.toString();
        String pattern = "(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        while (m.find( )) {
            String link = m.group();
            if(!link.contains("w3") && !link.contains("creativecommons")){
                System.out.println("all_links "+link);
                all_links.add(link);
            }
            String lowercase_link = link.toLowerCase();
            if (null != name && name.split(".pdf").length >= 1) {
                name = name.split(".pdf")[0];
            } else {
                continue;
            }
            if(lowercase_link.contains(name.toLowerCase()) && !good_links.contains(link)){
                System.out.println("good_links1 "+link);
                good_links.add(link);
            }
            if((link.contains("github") || link.contains("sourceforge") || link.contains("bioconductor") || link.contains("bitbucket")) && !good_links.contains(link)){
                System.out.println("good_links2 "+link);
                good_links.add(link);
            }
        }
        if(good_links.isEmpty() && all_links.size()>1)
        {
            for(int i=0;i<all_links.size();i++){
                if(!all_links.get(i).contains("w3") && !all_links.get(i).contains("niso") && !all_links.get(i).contains("creativecommons")){
                    good_links.add(all_links.get(i));
                    break;
                }
            }
        }

        return good_links;
    }
}
