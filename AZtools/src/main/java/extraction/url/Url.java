package extraction.url;

import extraction.Attributes;
import extraction.name.NameNLP;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yinxuexiao on 8/10/17.
 */
public class Url {

    private List<String> url;

    public List<String> getUrl() {  return url; }

    public Url(JSONObject xmlJSONObj, String name) throws Exception {
        this.url = extractURL(xmlJSONObj, name);
    }

    private List<String> extractURL(JSONObject xmlJSONObj, String name) {
        ArrayList<String> all_links= new ArrayList<>();
        ArrayList<String> good_links= new ArrayList<>();

        String line = xmlJSONObj.toString();
        String pattern = "(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        while (m.find( )) {
            String link = m.group();
//            System.out.println("all_links "+link);
            all_links.add(link);
            String lowercase_link = link.toLowerCase();
            if(lowercase_link.contains(name.toLowerCase()) && !good_links.contains(link)){
//                System.out.println("good_links "+link);
                good_links.add(link);
            }
        }
        if(good_links.isEmpty() && all_links.size()>1)
            good_links.add(all_links.get(1));
        return good_links;
    }
}
