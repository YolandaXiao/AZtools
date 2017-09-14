package extraction.abstrakt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yinxuexiao on 8/25/17.
 */
public class Abstract {

    private String abstrakt;

    public String getAbstrakt() {  return abstrakt; }

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
//        try {
//            JSONObject section = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
//            if(section.has("abstract")){
//                Object item3 = null;
//                try {
//                    item3 = section.get("abstract");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                if (item3 instanceof JSONObject){
//                    JSONObject group = (JSONObject) item3;
//                    if(group.has("p")){
//                        Object item = null;
//                        try {
//                            item = group.get("p");
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        if (item instanceof String){
//                            abstrakt += (String) item;
//                        }
//                        else if (item instanceof JSONArray){
//                            JSONArray abstract_arr = (JSONArray) item;
//                            for(int i=0;i<abstract_arr.length();i++){
//                                Object item2 = null;
//                                try {
//                                    item2 = abstract_arr.get(i);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                if (item2 instanceof String){
//                                    abstrakt += (String) item2;
//                                }
//                            }
//                        }
//                    }
//                }
//                else if (item3 instanceof JSONArray){
//                    JSONArray group = (JSONArray) item3;
//                    for(int j=0;j<group.length();j++) {
//                        JSONObject json = group.getJSONObject(j);
//                        Iterator<String> keys = json.keys();
//
//                        while (keys.hasNext()) {
//                            String key = keys.next();
//                            System.out.println("Key :" + key + "  Value :" + json.get(key));
//                            if (key.equals("p")) {
//                                Object para = null;
//                                try {
//                                    para = json.get(key);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                if (para instanceof String) {
//                                    abstrakt += (String) para;
//                                } else if (para instanceof JSONArray) {
//                                    JSONArray abstract_arr = (JSONArray) para;
//                                    for (int i = 0; i < abstract_arr.length(); i++) {
//                                        Object item2 = null;
//                                        try {
//                                            item2 = abstract_arr.get(i);
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                        if (item2 instanceof String) {
//                                            abstrakt += (String) item2;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return abstrakt;
    }
}
