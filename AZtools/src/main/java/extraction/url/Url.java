package extraction.url;

import extraction.Attributes;
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

    public Url(JSONObject xmlJSONObj) throws Exception {
        this.url = extractURL(xmlJSONObj);
    }

    private List<String> extractURL(JSONObject xmlJSONObj) {
        String name = Attributes.getTitle();
        ArrayList<String> all_links= new ArrayList<>();
        ArrayList<String> good_links= new ArrayList<>();

        String line = xmlJSONObj.toString();
        String pattern = "(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";
//        String pattern = "\\.\\s.*?http.*?(\\.(\\s|$|\"))";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        while (m.find( )) {
//            String[] arr = m.group().split("\\. ");
//            String result = arr[arr.length-1];
            String link = m.group();
            all_links.add(link);
            if(link.contains(name.toLowerCase()) && !good_links.contains(link))
                good_links.add(link);
        }
        if(good_links.isEmpty() && all_links.size()>1)
            good_links.add(all_links.get(1));
        return good_links;
    }
}
