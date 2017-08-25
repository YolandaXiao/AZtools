package extraction.funding;

import com.fasterxml.jackson.databind.ObjectMapper;
import extraction.Properties;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.test.Test;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Funding {
    private List<FundingInfo> funding;

    public List<FundingInfo> getFunding() {  return funding; }

    public Funding(String nlm) throws Exception {
        this.funding = extractFunding(nlm);
    }

    //extract funding section from xlm
    private String extractMatch(String nlm, String name, String result){
        String pattern = "(?s)"+name+".*?</p>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(nlm);
        while (m.find( )) {
            result = m.group();
        }
        return result;
    }

    //extract funding section from xlm using the above helper function
    private String extractFundingSection(String nlm){
        String result = "None";
        result = extractMatch(nlm,"<title>ACKNOWLEDG", result);
        result = extractMatch(nlm,"<title>acknowledg", result);
        result = extractMatch(nlm,"<title>Acknowledg", result);
        result = extractMatch(nlm,"<p>ACKNOWLEDG", result);
        result = extractMatch(nlm,"<p>acknowledg", result);
        result = extractMatch(nlm,"<p>Acknowledg", result);
        result = extractMatch(nlm,"<title>Funding", result);
        result = extractMatch(nlm,"<title>FUNDING", result);
        result = extractMatch(nlm,"<title>funding", result);
        result = extractMatch(nlm,"<p>Funding:", result);
        result = extractMatch(nlm,"<p>funding:", result);
        result = extractMatch(nlm,"<p>FUNDING:", result);
        result = extractMatch(nlm,"Funding: ", result);
        result = extractMatch(nlm,"funding: ", result);
        result = extractMatch(nlm,"FUNDING: ", result);
        return result;
    }

    //helper function: get list of agency names
    private ArrayList<String> getAgencyDic() throws IOException {
        String agencyNamesFile = Properties.getAgencyNamesFileName();
        BufferedReader br = new BufferedReader(new FileReader(agencyNamesFile));
        ArrayList<String> agency_dic= new ArrayList<>();
        try {
            String line = br.readLine();

            while (line != null) {
                agency_dic.add(line);
                line = br.readLine();
            }
        } finally {
            br.close();
        }
        return agency_dic;
    }

    private List<FundingInfo> extractFunding(String nlm) throws Exception {
        ArrayList<FundingInfo> arrayList= new ArrayList<>();

        //check each character in json file
        InputStream is = new FileInputStream("src/main/java/extraction/funding/cached_tree_map.json");
        String jsonTxt = IOUtils.toString(is);
        jsonTxt = jsonTxt.toLowerCase();
//        System.out.println(jsonTxt);
        JSONObject result = new JSONObject(jsonTxt);

        //run NER on the entire paragraph again to get agencies without grant number
        String funding_section = extractFundingSection(nlm);
        if(funding_section=="None"){
            return arrayList;
        }
//        System.out.println(funding_section);
//        funding_section = funding_section.toLowerCase();
        String pattern2 = "([/\\[]*\\w+[-/]*\\w+[-/\\]]*)";
        Pattern r2 = Pattern.compile(pattern2);
        Matcher m2 = r2.matcher(funding_section);
        ArrayList<String> words = new ArrayList<>();
        while (m2.find( )) {
            String word = m2.group();
            words.add(word);
        }
        //iterate through each word
        for(int i=0;i<words.size();i++){
            String word = words.get(i);
            String word_lowercase = word.toLowerCase();
//            System.out.println("word1:"+word);
            int count = i;
            JSONObject result2 = result;
            //exclude extreme cases:
            //if the word is only one layer and it's not acronym
            if(result2.has(word_lowercase) && result2.getJSONObject(word_lowercase).has("$value") && word.toUpperCase()!=word){
                continue;
            }
            if(word_lowercase.length()==1){
                continue;
            }
            if(word_lowercase.equals("us")){
                continue;
            }
//            System.out.println("word2:"+word);
            //for each word, find possible agency name by going through words after it
            while(result2.has(word_lowercase) || result2.has("$value")){
                //if it has an output result value
                if(result2.has("$value")){
                    JSONArray arr = result2.getJSONArray("$value");
                    String value = (String) arr.get(0);
                    //check if the acronym has other meanings
                    if(arr.length()>0){
                        for(int j = 0; j < arr.length(); j++){
                            String name = (String) arr.get(j);
                            if(funding_section.toLowerCase().contains(name)){
                                value = name;
                                break;
                            }
                        }
                    }
                    System.out.println("value: "+value);
                    //create new FundingInfo object
                    FundingInfo fi = new FundingInfo();
                    fi.setAgency(value);
                    //check if the name already exists in the list
                    boolean flag = true;
                    for(int j = 0; j < arrayList.size(); j++) {
                        if(arrayList.get(j).getAgency()!=null ){
                            if(arrayList.get(j).getAgency().equals(fi.getAgency())){
                                flag = false;
                            }
                        }
                    }
                    if(flag){
                        arrayList.add(fi);
                    }
                    i = count-1;
                    break;
                }
                //keep iterating
                else{
//                    System.out.println("word: "+word_lowercase);
                    count++;
                    if(count<words.size()){
                        result2 = result2.getJSONObject(word_lowercase);
//                        System.out.println("hi: "+result2);
                        word = words.get(count);
                        word_lowercase = word.toLowerCase();
                    }
                    if(count==words.size()){
                        result2 = result2.getJSONObject(word_lowercase);
//                        System.out.println("hi: "+result2);
                    }
                }
            }
        }

        return arrayList;
    }
}
