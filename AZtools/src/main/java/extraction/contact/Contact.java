package extraction.contact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinxuexiao on 8/10/17.
 */
public class Contact {

    private List<String> contact;

    public List<String> getContact() {  return contact; }

    public Contact(JSONObject xmlJSONObj) throws Exception {
        this.contact = extractContact(xmlJSONObj);
    }

    private List<String> extractContact(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONArray contacts = null;
        try {
            contacts = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group").getJSONArray("contrib");
            for(int i=0;i<contacts.length();i++)
            {
                try {
                    if(contacts.getJSONObject(i).has("email")){
                        String contact = null;
                        try {
                            contact = contacts.getJSONObject(i).getString("email");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        arraylist.add(contact);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }
}
