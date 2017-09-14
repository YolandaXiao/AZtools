package extraction.contact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Contact {

    private List<String> contact;

    public List<String> getContact() {
        return contact;
    }

    public Contact(String nlm,int num) throws Exception {
        JSONObject xmlJSONObj = XML.toJSONObject(nlm);
        if( num == 0) {
            this.contact = extractContact_fromCermineXML(xmlJSONObj);
        }
        else{
            this.contact = extractContact_fromPMCXML(nlm);
        }

    }

    private List<String> extractContact_fromCermineXML(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist = new ArrayList<String>();
        try {
            JSONObject article_meta = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
            if (article_meta.has("contrib-group")) {
                JSONObject group = article_meta.getJSONObject("contrib-group");
                if (group.has("contrib")) {
                    Object item = group.get("contrib");
                    if (item instanceof JSONArray) {
                        JSONArray contacts = (JSONArray) item;
                        for (int i = 0; i < contacts.length(); i++) {
                            if (contacts.getJSONObject(i).has("email")) {
                                String contact = contacts.getJSONObject(i).getString("email");
                                arraylist.add(contact);
                            }
                        }
                    } else if (item instanceof String) {
                        String contact = (String) item;
                        arraylist.add(contact);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    private List<String> extractContact_fromPMCXML(String nlm) {
        ArrayList<String> arraylist = new ArrayList<String>();
        try {
            String pattern = "(?s)<email.*?<\\/email>";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(nlm);
            while (m.find()) {
                String contact = m.group().split("<email.*?>")[1];
                contact = contact.split("</email>")[0];
                arraylist.add(contact);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return arraylist;
    }
}
