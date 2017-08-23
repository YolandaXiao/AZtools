package extraction.contact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Contact {

    private List<String> contact;

    public List<String> getContact() {  return contact; }

    public Contact(JSONObject xmlJSONObj) throws Exception {
        this.contact = extractContact(xmlJSONObj);
    }

    private List<String> extractContact(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONObject group = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group");
        if (group.has("contrib")) {
            Object item = null;
            try {
                item = group.get("contrib");
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            if (item instanceof JSONArray) {
                JSONArray contacts = (JSONArray) item;
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (item instanceof String) {
                String contact = (String) item;
                arraylist.add(contact);
            }
        }
        return arraylist;
    }
}
