package extraction.abstrakt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Abstract {

    private String abstrakt;

    public String getAbstrakt() {
        return abstrakt;
    }

    public Abstract(String nlm, int num) throws Exception {
        JSONObject xmlJSONObj = XML.toJSONObject(nlm);
        if(num==0){
            this.abstrakt = extractAbstract_fromCermineXML(xmlJSONObj);
        }
        else{
            this.abstrakt = extractAbstract_fromPMCXML(nlm);
        }
    }

    private String extractAbstract_fromCermineXML(JSONObject xmlJSONObj) {
        String abstrakt = "";
        try {
            abstrakt = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("abstract").getString("p");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return abstrakt;
    }

    private String extractAbstract_fromPMCXML(String nlm) {
        String abstrakt = "";

        String pattern = "<abstract>.*?<\\/abstract>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(nlm);
        if (m.find( )) {
            abstrakt = m.group();
        }
        abstrakt = abstrakt.replaceAll("<[^>]+>", "");
        abstrakt = abstrakt.replaceAll("\n", " ");
        abstrakt = abstrakt.replaceAll("\t", " ");
        abstrakt = abstrakt.trim().replaceAll(" +", " ");
        return abstrakt;
    }
}
