package extraction.language;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinxuexiao on 8/10/17.
 */
public class Language {

    private List<String> language;

    public List<String> getLanguage() {  return language; }

    public Language(JSONObject xmlJSONObj) throws Exception {
        this.language = extractProgramming_lang(xmlJSONObj);
    }

    private List<String> extractProgramming_lang(JSONObject xmlJSONObj) {
        ArrayList<String> i = new ArrayList<String>();
        return i;
    }
}
