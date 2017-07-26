package upload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.*;
import java.util.*;
import java.net.*;

public class Attributes {
    private final String title;
    private final String name;
    private final List<String> author;
    private final List<String> affiliation;
    private final String abstrakt;
    private final List<String> contact;
    private final String DOI;
    private final int date;
    private final List<String> URL;
    private final String funding;

    public Attributes(JSONObject xmlJSONObj, String orig_file_name) {
        this.title = extractTitle(xmlJSONObj);
        this.name = extractName(orig_file_name);
        this.author = extractAuthor(xmlJSONObj);
        this.affiliation = extractAffiliation(xmlJSONObj);
        this.abstrakt = extractAbstract(xmlJSONObj);
        this.contact = extractContact(xmlJSONObj);
        this.DOI = extractDOI(xmlJSONObj);
        this.date = extractDate(xmlJSONObj);
        this.URL = extractURL(xmlJSONObj);
        this.funding = extractFunding(xmlJSONObj);
    }

    public String getTitle(){
        return title;
    }

    public String getName() {
        return name;
    }

    public List<String> getAuthor(){
        return author;
    }

    public List<String> getAffiliation(){
        return affiliation;
    }

    public String getAbstrakt(){
        return abstrakt;
    }

    public List<String> getContact(){
        return contact;
    }

    public String getDOI(){
        return DOI;
    }

    public int getDate() {
        return date;
    }

    public List<String> getURL(){
        return URL;
    }

    public String getFunding() {
        return funding;
    }

    // ----------------------------------------------------------- //

    public String extractTitle(JSONObject xmlJSONObj) {
        String title = null;
        try {
            title = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("title-group").getString("article-title");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return title;
    }

    public String extractName(String orig_file_name) {

        System.out.println("Searching: \"" + orig_file_name + ".pdf\" for tool's name...");

        // String titleOfPaper, String abstractOfPaper

        Vector cv = new Vector(0);
        Vector metadata = new Vector(0);

        // Initialize confidence vector
        String cermine_title = getTitle();
        String[] words = cermine_title.split("\\s");

        String fileName = "C:/Ankur/Code/AZtools/AZtools/src/main/java/upload/stop.txt";

        String line = null;
        ArrayList<String> stop_words = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                stop_words.add(line);
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }

        for (int i = 0; i < words.length; i++) {
            for (int j = i; j < words.length; j++) {

                ArrayList<String> phrase = new ArrayList<String>();
                Vector element = new Vector(0);

                boolean hasStop = false;
                for (int k = i; k <= j; k++) {
                    if (stop_words.contains(words[k].toLowerCase())) {
                        hasStop = true;
                        break;
                    }
                    phrase.add(words[k]);
                }

                if (hasStop) {
                    continue;
                }

                int startPos = i;
                int numWords = phrase.size();
                int confidence = 5;
                boolean isDefinedInDict = false;

                element.addElement(phrase);
                element.addElement(startPos);
                element.addElement(numWords);

                element.addElement(confidence);
                element.addElement(isDefinedInDict);

                if (!cv.contains(phrase)) {
                    cv.addElement(element);
                    // need to modify
                }
            }
        }

        // Deal with colons
        for (int l = 0; l < cv.size(); l++) {
            ArrayList<String> phraseWords = new ArrayList((ArrayList)(((Vector)cv.get(l)).get(0)));

            for (int m = 0; m < phraseWords.size(); m++) {
                String word = phraseWords.get(m);
                //String word = phraseWords.get(phraseWords.size() - 1); // whether last word in phrase has colon

                if (word.charAt(word.length() - 1) == ':') {

                    String new_word = word.substring(0, word.length()-1);

                    ArrayList<String> new_phrase = new ArrayList<>();

                    for (int n = 0; n < m; n++) {
                        new_phrase.add (phraseWords.get(n));
                    }

                    new_phrase.add(new_word);

                    //new_phrase.remove(word);
                    //new_phrase.add(new_word);

                    //System.out.println("Colon detected");
                    //System.out.println(phraseWords);

                    //System.out.println(word);
                    //System.out.println(new_word);

                    Vector element = new Vector(0);

                    int startPos = (int)((Vector)cv.get(l)).get(1);
                    //int numWords = (int)((Vector)cv.get(l)).get(2);
                    int numWords = new_phrase.size();

                    int confidence = 50;
                    boolean isDefinedInDict = (boolean)((Vector)cv.get(l)).get(4);

                    element.addElement(new_phrase);
                    element.addElement(startPos);
                    element.addElement(numWords);

                    element.addElement(confidence);
                    element.addElement(isDefinedInDict);

                    boolean isNew = true;

                    for (int o = 0; o < cv.size(); o++) {
                        if (((Vector)cv.get(l)).get(0) == new_phrase) {
                            isNew = false;
                            break;
                        }
                    }

                    if (isNew) {
                        cv.addElement(element);
                        cv.remove((Vector)cv.get(l));
                        // need to modify
                    }
                }
            }

        }

        // Existence and Defined-ness
        for (int m = 0; m < cv.size(); m++) {
            ArrayList<String> phraseWords = new ArrayList((ArrayList) (((Vector)cv.get(m)).get(0)));
            String possibleName = "";

            for (String word: phraseWords) {
                possibleName += word + " ";
            }

            possibleName = possibleName.substring(0, possibleName.length()-1);
            //System.out.println(possibleName);

            String mesh_code = "";

            try {
                String mesh_api = "https://www.ncbi.nlm.nih.gov/mesh/?term=";

                URL url = new URL(mesh_api);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder result = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();

                mesh_code = result.toString();
            }
            catch (IOException ex) {
                System.out.println("Was not able to search through dictionary. Skipping phrase.");
            }

            String err_msg = "The following term was not found in MeSH";
            boolean isMedical = false;
            boolean isEnglish = false;

            if (!mesh_code.contains(err_msg)) {
                isMedical = true;
            }

            fileName = "C:/Ankur/Code/AZtools/AZtools/src/main/java/upload/en.txt";

            line = null;
            ArrayList<String> words_list = new ArrayList<>();

            try {
                FileReader fileReader = new FileReader(fileName);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                while((line = bufferedReader.readLine()) != null) {
                    words_list.add(line);
                }
                bufferedReader.close();
            }
            catch(FileNotFoundException ex) {
                System.out.println("Unable to open file '" + fileName + "'");
            }
            catch(IOException ex) {
                System.out.println("Error reading file '" + fileName + "'");
            }

            if (words_list.contains(phraseWords)) {
                isEnglish = true;
            }

            /*

            Process p = null;
            ProcessBuilder pb = null;

            int return_val = -1;
            String s = null;

            try {
                //p = Runtime.getRuntime().exec("/upload/words.exe");
                pb = new ProcessBuilder(
                        "C:/Ankur/Code/AZtools/AZtools/src/main/java/upload/words.exe", possibleName);
                p = pb.start();

                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(p.getInputStream()));

                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(p.getErrorStream()));

                System.out.println("standard output of the command:\n");
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                }

                System.out.println("standard error of the command (if any):\n");
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                }

            } catch (IOException ex) {
                System.out.println("Error while executing words.py!");
                ex.printStackTrace();
            }


            try {
                return_val = p.waitFor();
            }
            catch (InterruptedException ex) {
                System.out.println("Error while waiting for words.py!");
                System.out.println(return_val);
                ex.printStackTrace();
            }

            */


            if (isMedical || isEnglish) {
                // word found
                ((Vector)cv.get(m)).set(3,(int)(((Vector)cv.get(m)).get(3)) - 25);
                ((Vector)cv.get(m)).set(4, true);
            }
            else {
                // word not found
                ((Vector)cv.get(m)).set(3,(int)(((Vector)cv.get(m)).get(3)) + 30);
                ((Vector)cv.get(m)).set(4, false);

                if (((ArrayList<String>)(((Vector)cv.get(m)).get(0))).size() > 1) {
                    ((Vector)cv.get(m)).set(3,(int)(((Vector)cv.get(m)).get(3)) - 10);
                }
            }
            /*
            else {
                System.out.println("Bad exit status - words.py!");
            }
            */
        }

        for (int z = 0; z < cv.size(); z++) {
            int numCaptialsNumbers = 0;
            int numHyphens = 0;

            boolean firstLettersCapital = true;
            int numWords = 0;

            for (String word : (ArrayList<String>)((Vector)(cv.get(z))).get(0)) {
                for (int y = 0; y < word.length(); y++) {
                    if (Character.isUpperCase(word.charAt(y))) {
                        numCaptialsNumbers += 1;
                    }
                }
                numWords += 1;
                if (!Character.isUpperCase(word.charAt(0))) {
                    firstLettersCapital = false;
                }
            }

            ((Vector)(cv.get(z))).set(3, (int)((Vector)(cv.get(z))).get(3) + numCaptialsNumbers * 7);

            if (firstLettersCapital && numWords > 1) {
                ((Vector)(cv.get(z))).set(3, (int)((Vector)(cv.get(z))).get(3) + 25);
            }
        }


        ///// cleanup

        int maxConfidence = 0;
        int index = 0;

        for (int p = 0; p < cv.size(); p++) {
            int confidence = (int)((Vector)(cv.get(p))).get(3);
            if (confidence > maxConfidence) {
                maxConfidence = confidence;
                index = p;
            }
        }

        String final_name = "";
        for (String word : (ArrayList<String>)((Vector)(cv.get(index))).get(0)) {
            final_name += word + " ";
        }
        final_name = final_name.substring(0, final_name.length() - 1);

        // Output
        //System.out.println("Different possible phrases of the title: \n\"" + cermine_title + "\"");

        //for (int l = 0; l < cv.size(); l++) {
        //    System.out.println(cv.get(l));
        //}

        //System.out.println("Best possible name for title: \n\"" + cermine_title + "\"");
        System.out.println("! Found name: \"" + final_name + "\"");

        return final_name;
    }

    public List<String> extractAuthor(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONArray authors = null;
        try {
            authors = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group").getJSONArray("contrib");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for(int i=0;i<authors.length();i++)
        {
            String author = null;
            try {
                author = authors.getJSONObject(i).getString("string-name");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            arraylist.add(author);
        }
        return arraylist;
    }

    public List<String> extractAffiliation(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONObject group = null;
        try {
            group = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(group.has("aff")){
            Object item = null;
            try {
                item = group.get("aff");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (item instanceof JSONObject){
                JSONObject affiliations = (JSONObject) item;
                try {
                    String aff = affiliations.getString("institution");
                    String[] arr = aff.split(",");
                    String result = arr[arr.length-1];
                    if(!arraylist.contains(result)){
                        arraylist.add(result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (item instanceof JSONArray){
                JSONArray affiliations = (JSONArray) item;
                for(int i=0;i<affiliations.length();i++)
                {
                    try {
                        if(affiliations.getJSONObject(i).has("institution")) {
                            String aff = affiliations.getJSONObject(i).getString("institution");
                            String[] arr = aff.split(",");
                            String result = arr[arr.length-1];
                            if(!arraylist.contains(result)){
                                arraylist.add(result);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return arraylist;
    }

    public String extractAbstract(JSONObject xmlJSONObj) {
        String abstrakt = null;
        try {
            abstrakt = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("abstract").getString("p");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return abstrakt;
    }

    public List<String> extractContact(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist= new ArrayList<String>();
        JSONArray contacts = null;
        try {
            contacts = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("contrib-group").getJSONArray("contrib");
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        return arraylist;
    }

    public String extractDOI(JSONObject xmlJSONObj) {
        JSONObject value = null;
        try {
            value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (value.has("article-id")) {
            String DOI = null;
            try {
                DOI = value.getJSONObject("article-id").getString("content");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return DOI;
        }
        return "None";
    }

    public int extractDate(JSONObject xmlJSONObj) {
        JSONObject value = null;
        try {
            value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (value.has("pub-date")) {
            int date = 0;
            try {
                date = value.getJSONObject("pub-date").getInt("year");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return date;
        }
        return 0;
    }

    public List<String> extractURL(JSONObject xmlJSONObj) {
        String name = getTitle();
        ArrayList<String> all_links= new ArrayList<String>();
        ArrayList<String> good_links= new ArrayList<String>();
        String line = xmlJSONObj.toString();
        String pattern = "(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";
//        String pattern = "\\.\\s.*?http.*?(\\.(\\s|$|\"))";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        while (m.find( )) {
//            String[] arr = m.group().split("\\. ");
//            String result = arr[arr.length-1];
            String link = m.group().toLowerCase();
            all_links.add(link);
            if(link.contains(name.toLowerCase()) && !good_links.contains(link))
                good_links.add(link);
        }
        if(good_links.isEmpty())
            good_links.add(all_links.get(1));
        return good_links;
    }

    public String extractFunding(JSONObject xmlJSONObj) {
        JSONArray funding_section = null;
        try {
            funding_section = xmlJSONObj.getJSONObject("article").getJSONObject("body").getJSONArray("sec");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for(int i=0;i<funding_section.length();i++)
        {
            String title = null;
            try {
                title = funding_section.getJSONObject(i).getString("title");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if((funding_section.getJSONObject(i).has("title") && title.toLowerCase().equals("funding")) || (funding_section.getJSONObject(i).has("title") && title.toLowerCase().equals("acknowledgements"))){
                    Object item = null;
                    try {
                        item = funding_section.getJSONObject(i).get("p");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (item instanceof String){
                        String funding_text = (String) item;
                        return funding_text;
                    }
                    else if (item instanceof JSONArray){
                        JSONArray funding_text = (JSONArray) item;
                        try {
                            return funding_text.getString(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "None";
    }

}
