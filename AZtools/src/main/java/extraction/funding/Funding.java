package extraction.funding;

import webapp.Globs;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Funding {
    private List<FundingInfo> funding;

    public List<FundingInfo> getFunding() {  return funding; }

    public Funding(String nlm, int num) throws Exception {
        String funding_section = extractFundingSection(nlm);
        if (num == 0) {
            this.funding = extractFunding_fromCermineXML(nlm,funding_section);
        }
        else {
            this.funding = extractFunding_fromPMCXML(nlm);
        }

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
    private String extractFundingSection(String nlm) {
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
        String agencyNamesFile = Globs.getAgencyNamesFileName();
        BufferedReader br = new BufferedReader(new FileReader(agencyNamesFile));
        ArrayList<String> agency_dic = new ArrayList();
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

    private List<FundingInfo> extractFunding_fromCermineXML(String nlm, String funding_section) throws Exception {
        ArrayList<FundingInfo> arrayList= new ArrayList();
        InputStream is = new FileInputStream(Globs.get_cached_tree_map());
        String jsonTxt = IOUtils.toString(is);
        jsonTxt = jsonTxt.toLowerCase();
        JSONObject result = new JSONObject(jsonTxt);

        //if there's no funding_section extracted
        if(funding_section=="None"){
            return arrayList;
        }
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
            int count = i;
            JSONObject result2 = result;
            //exclude extreme cases: if the word is only one layer and it's not acronym
            if(result2.has(word_lowercase) && result2.getJSONObject(word_lowercase).has("$value") && word.toUpperCase()!=word){
                continue;
            }
            if(word_lowercase.length()==1){
                continue;
            }
            if(word_lowercase.equals("us")){
                continue;
            }
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
                    count++;
                    if(count<words.size() && count>=0){
                        result2 = result2.getJSONObject(word_lowercase);
                        word = words.get(count);
                        word_lowercase = word.toLowerCase();
                    }
                    if(count==words.size()){
                        result2 = result2.getJSONObject(word_lowercase);
                    }
                }
            }
        }
        return arrayList;
    }

    private List<FundingInfo> extractFunding_fromPMCXML(String nlm) throws Exception {
        ArrayList<FundingInfo> arrayList= new ArrayList<>();

        //get funding sources just by regex matching to pmc xmls
        String pattern = "(?s)<funding-source>.*?<\\/funding-source>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(nlm);
        while (m.find( )) {
            String agency = m.group().split("<funding-source>")[1];
            agency = agency.split("</funding-source>")[0];
            if(agency.contains("<institution>")){
                agency =  agency.split("<institution>")[1];
                agency =  agency.split("</institution>")[0];
            }
            FundingInfo fi = new FundingInfo();
            fi.setAgency(agency);
            arrayList.add(fi);
        }

        if(!arrayList.isEmpty()){
            return arrayList;
        }

        //get funding paragraph through another form of regex
        String pattern2 = "(?s)<funding-group>.*?<\\/funding-group>";
        Pattern r2 = Pattern.compile(pattern2);
        Matcher m2 = r2.matcher(nlm);
        if (m2.find( )) {
            String paragraph = m2.group();
            return extractFunding_fromCermineXML(nlm,paragraph);
        }

        //if there's no direct funding source info available, get paragraph and apply our method
        String pattern3 = "<ack>.*?<\\/ack>";
        Pattern r3 = Pattern.compile(pattern3);
        Matcher m3 = r3.matcher(nlm);
        if (m3.find( )) {
            String paragraph = m3.group();
            paragraph = paragraph.replaceAll("<[^>]+>", "");
            paragraph = paragraph.replaceAll("\n", " ");
            paragraph = paragraph.replaceAll("\t", " ");
            paragraph = paragraph.trim().replaceAll(" +", " ");
            return extractFunding_fromCermineXML(nlm,paragraph);
        }

        //apply our method on pmc xmls
        if(arrayList.isEmpty()){
            String funding_section = extractFundingSection(nlm);
            return extractFunding_fromCermineXML(nlm,funding_section);
        }
        return arrayList;
    }
}
