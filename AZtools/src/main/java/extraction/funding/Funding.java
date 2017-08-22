package extraction.funding;

import extraction.Properties;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yinxuexiao on 8/9/17.
 */
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

        //extract funding section
        String funding_section = extractFundingSection(nlm);

        //get agency dictionary
        ArrayList<String> agency_dic = getAgencyDic();


        Calendar ner1_start = Calendar.getInstance();

        //get license
        String pattern = "([\\dA-Z\\/\\-\\s]{2,}[\\d\\/\\-\\s]{2,}[\\dA-Z\\/\\-]{2,})";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(funding_section);
        String agency = null;
        while (m.find( )) {
            //get license and sentence before that contains agency
            FundingInfo fi = new FundingInfo();
            String license = m.group();
            fi.setLicense(license);
            String[] arr = funding_section.split(license);
            String agency_sentence = arr[0];
            if (arr.length >= 2) {
                funding_section = arr[1];
            }
            else{
                funding_section = arr[0];
            }

            // get agency from stanford ner
            ExtractDemo extractDemo = new ExtractDemo();
            String funding = extractDemo.doNer(agency_sentence);
            //System.out.println(funding);
            String pattern2 = "(?s)<ORGANIZATION>.*?<\\/ORGANIZATION>";
            Pattern r2 = Pattern.compile(pattern2);
            Matcher m2 = r2.matcher(funding);
            while (m2.find( )) {
                String[] temp = m2.group().split(">");
                agency = temp[temp.length-1].split("<")[0];
            }

            //consider special case such as "KRIBB Research Initiative Program; Technology Innovation Program of the Ministry of Trade, Industry and Energy"
            if(agency!=null && agency.contains(";")){
                String new_agency = agency.split(";")[1];
                fi.setAgency(new_agency);
                arrayList.add(fi);

                String new_agency1 = agency.split(";")[0];
                FundingInfo fi1 = new FundingInfo();
                fi1.setAgency(new_agency1);
                arrayList.add(fi1);
            }
            else{
                fi.setAgency(agency);
//            System.out.println(fi.getAgency());
//            System.out.println(fi.getLicense());
                arrayList.add(fi);
            }
        }

        Calendar ner1_end = Calendar.getInstance();
        System.out.println("Time ner1: ");
        System.out.println(ner1_end.getTimeInMillis() - ner1_start.getTimeInMillis());

        //run NER on the entire paragraph again to get agencies without grant number
        funding_section = extractFundingSection(nlm);
        ExtractDemo extractDemo = new ExtractDemo();
        String funding = extractDemo.doNer(funding_section);
        String pattern2 = "(?s)<ORGANIZATION>.*?<\\/ORGANIZATION>";
        Pattern r2 = Pattern.compile(pattern2);
        Matcher m2 = r2.matcher(funding);
        while (m2.find( )) {
            FundingInfo f = new FundingInfo();
            String ag = m2.group().split(">")[1].split("<")[0];
            f.setAgency(ag);
            //check if the name already exists in the list
            boolean flag = true;
            for(int i = 0; i < arrayList.size(); i++) {
                if(arrayList.get(i).getAgency()!=null ){
                    if(arrayList.get(i).getAgency().equals(f.getAgency())){
                        flag = false;
                    }
                }
            }
            //check if the name exists in dictionary

            if(flag && agency_dic.contains(f.getAgency())){
                arrayList.add(f);
            }
        }

        return arrayList;
    }
}
