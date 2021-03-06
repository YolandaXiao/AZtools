package extraction.name;

import org.apache.commons.lang.StringUtils;
import webapp.Globs;

import java.io.*;
import java.util.*;

public class NameNLP {

    private ArrayList<String> words;
    private Vector info;
    private String cermine_title;
    private List<String> urls;
    private String final_name;

    public NameNLP(String title, List<String> cer_urls) {

        info = new Vector(0);
        words = new ArrayList(0);

        if (null != title) {
            words.addAll(Arrays.asList(title.split("\\s")));
        } else {
            final_name = "";
            return;
        }

        urls = cer_urls;
        cermine_title = title;

//        Calendar start_time = Calendar.getInstance();
        dealFirstWord();
//        Calendar end_time = Calendar.getInstance();
//        long time_taken = end_time.getTimeInMillis() - start_time.getTimeInMillis();
//        System.out.println("dealFirstWord() takes " + time_taken);

//        System.out.println("One: '" + cermine_title + "'");
//        for (int l = 0; l < info.size(); l++) {
//            System.out.println(info.get(l));
//        }

//        start_time = Calendar.getInstance();
        findRepoName();
//        end_time = Calendar.getInstance();
//        time_taken = end_time.getTimeInMillis() - start_time.getTimeInMillis();
//        System.out.println("findRepoName() takes " + time_taken);

//        System.out.println("Two: '" + cermine_title + "'");
//        for (int l = 0; l < info.size(); l++) {
//            System.out.println(info.get(l));
//        }

//        start_time = Calendar.getInstance();
        initializeInfo();
//        end_time = Calendar.getInstance();
//        time_taken = end_time.getTimeInMillis() - start_time.getTimeInMillis();
//        System.out.println("initializeInfo() takes " + time_taken);

//        System.out.println("Three: '" + cermine_title + "'");
//        for (int l = 0; l < info.size(); l++) {
//            System.out.println(info.get(l));
//        }

//        start_time = Calendar.getInstance();
        dealPunctuation();
//        end_time = Calendar.getInstance();
//        time_taken = end_time.getTimeInMillis() - start_time.getTimeInMillis();
//        System.out.println("dealPunctuation() takes " + time_taken);

//        System.out.println("Four: '" + cermine_title + "'");
//        for (int l = 0; l < info.size(); l++) {
//            System.out.println(info.get(l));
//        }

//        start_time = Calendar.getInstance();
        isDefined();
//        end_time = Calendar.getInstance();
//        time_taken = end_time.getTimeInMillis() - start_time.getTimeInMillis();
//        System.out.println("isDefined() takes " + time_taken);

//        System.out.println("Five: '" + cermine_title + "'");
//        for (int l = 0; l < info.size(); l++) {
//            System.out.println(info.get(l));
//        }

//        start_time = Calendar.getInstance();
        dealUniqueChar();
//        end_time = Calendar.getInstance();
//        time_taken = end_time.getTimeInMillis() - start_time.getTimeInMillis();
//        System.out.println("dealUniqueChar() takes " + time_taken);

//        System.out.println("Six: '" + cermine_title + "'");
//        for (int l = 0; l < info.size(); l++) {
//            System.out.println(info.get(l));
//        }

//        start_time = Calendar.getInstance();
        cleanup();
//        end_time = Calendar.getInstance();
//        time_taken = end_time.getTimeInMillis() - start_time.getTimeInMillis();
//        System.out.println("cleanup() takes " + time_taken);

//        System.out.println("Seven: '" + cermine_title + "'");
//        for (int l = 0; l < info.size(); l++) {
//            System.out.println(info.get(l));
//        }
    }

    public String getName() {
        return final_name;
    }

    private void dealFirstWord() {
        String firstWord = words.get(0);
        if (firstWord.length() >= 1 && Character.isLowerCase(firstWord.charAt(0))) {
            return;
        }

        // only first letter should be capitalized for this feature
        for (int i = 1; i < firstWord.length(); i++) {
            Character c = firstWord.charAt(i);
            if (Character.isUpperCase(c)) {
                return;
            }
        }

        // ensure all other words are lower case
        int i = 0;
        for (String word : words) {
            if (i == 0) {
                i = 1;
                continue;
            }
            if (!word.equals(word.toLowerCase())) {
                return;
            }
        }

        // add title to list
        Vector element = new Vector(0);
        element.addElement(cermine_title);
        element.addElement(13); // initial confidence
        info.add(element);
    }

    private void findRepoName() {
        for (String url : urls) {
            int pos = url.indexOf("github.com");
            if (pos == -1) {
                continue;
            }
            int pos2 = url.indexOf("/", pos + 1);
            int pos3 = url.indexOf("/", pos2 + 1);

            int startPos = pos3 + 1;
            int endPos = url.indexOf("/", startPos + 1);
            if (endPos == -1) {
                endPos = url.length();
            }

            String repoName = url.substring(startPos, endPos);
            Vector element = new Vector(0);
            element.addElement(repoName);
            element.addElement(90); // confidence
            info.addElement(element);
        }
    }

    private void initializeInfo() {

        String stop_file_path = Globs.get_stop_path();
        ArrayList<String> stop_words = new ArrayList();

        // get stop words
        try {
            FileReader fileReader = new FileReader(stop_file_path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stop_words.add(line);
            }

            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + stop_file_path + "'");
        } catch (IOException ex) {
            System.out.println("Error reading file '" + stop_file_path + "'");
        }

        // create phrases (possible names)
        for (int i = 0; i < words.size(); i++) {
            for (int j = i; j < words.size(); j++) {

                ArrayList<String> phrase = new ArrayList<String>();
                Vector element = new Vector(0);

                // ignore phrases with stop words
                boolean hasStop = false;
                for (int k = i; k <= j; k++) {
                    if (stop_words.contains(words.get(k).toLowerCase())) {
                        hasStop = true;
                        //break;
                    }
                    phrase.add(words.get(k));
                }

                int init_confidence = 20;
                if (hasStop) {
                    init_confidence = 15;
                }

                String onePhrase = "";
                for (String word : phrase) {
                    onePhrase += word;
                    onePhrase += " ";
                }

                onePhrase = onePhrase.substring(0, onePhrase.length() - 1);

                // add phrase to possible list
                element.addElement(onePhrase);
                element.addElement(init_confidence); // initial confidence

                if (!info.contains(phrase)) {
                    info.addElement(element);
                }
            }
        }
    }

    private void dealPunctuation() {
        for (int l = 0; l < info.size(); l++) {

            ArrayList<String> phraseWords = new ArrayList();
            String phrase = (String)(((Vector)info.get(l)).get(0));
            phraseWords.addAll(Arrays.asList(phrase.split(" ")));

            for (int m = 0; m < phraseWords.size(); m++) {
                try {
                    String word = phraseWords.get(m);
                    //String word = phraseWords.get(phraseWords.size() - 1); // whether last word in phrase has colon

                    // get phrase before the : or -
//                    System.out.println("Word: " + word);
//                    System.out.println("Word length: " + word.length());
                    if ((!StringUtils.isBlank(word) && word.length() != 0) && (word.charAt(word.length() - 1) == ':' || word.charAt(word.length() - 1) == '-')) {

                        String new_word = word.substring(0, word.length() - 1);
                        ArrayList<String> new_phrase = new ArrayList();

                        for (int n = 0; n < m; n++) {
                            new_phrase.add(phraseWords.get(n));
                        }
                        new_phrase.add(new_word);
                        Vector element = new Vector(0);

                        String thePhrase = "";
                        for (String ind_word : new_phrase) {
                            thePhrase += ind_word + " ";
                        }
                        thePhrase = thePhrase.substring(0, thePhrase.length() - 1);

                        element.addElement(thePhrase);
                        element.addElement((int) ((Vector) info.get(l)).get(1) + 200);

                        boolean isNew = true;
                        for (int p = 0; p < info.size(); p++) {
                            if (((Vector) (info.get(p))).get(0).equals(thePhrase)) {
                                //System.out.println("Duplicate phrase detected: " + ((Vector)(info.get(p))).get(0) + "  ,  " + thePhrase);
                                isNew = false;
                                break;
                            }
                        }
                        if (isNew) {
                            info.addElement(element);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void isDefined() {

        boolean isMedical = false;
        boolean isEnglish = false;

        String line;

        String mesh_file_path = Globs.get_mesh_path();
        String en_file_path = Globs.get_en_path();

        ArrayList<String> mesh_list = new ArrayList();
        ArrayList<String> words_list = new ArrayList();

        try {
            FileReader fileReader = new FileReader(mesh_file_path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                mesh_list.add(line.toLowerCase());
            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + mesh_file_path + "'");
        } catch (IOException ex) {
            System.out.println("Error reading file '" + mesh_file_path + "'");
        }

        try {
            FileReader fileReader = new FileReader(en_file_path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                words_list.add(line.toLowerCase());
            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + en_file_path + "'");
        } catch (IOException ex) {
            System.out.println("Error reading file '" + en_file_path + "'");
        }

        for (int m = 0; m < info.size(); m++) {

            ArrayList<String> phraseWords = new ArrayList();
            String phrase = (String)(((Vector)info.get(m)).get(0));
            phraseWords.addAll(Arrays.asList(phrase.split(" ")));

            isMedical = mesh_list.contains(phrase.toLowerCase());
            isEnglish = words_list.contains(phrase.toLowerCase());
            int currentConfidence = (int) (((Vector) info.get(m)).get(1));

            if (isMedical || isEnglish) {
                ((Vector) info.get(m)).set(1, currentConfidence - 15);
            }
            else {
                ((Vector) info.get(m)).set(1, currentConfidence + 30);

                String aPhrase = ((String)((Vector) info.get(m)).get(0));
                String[] wordsInPhrase = aPhrase.split(" ");
                if (wordsInPhrase.length > 1) {
                    currentConfidence = (int) (((Vector) info.get(m)).get(1));
                    ((Vector) info.get(m)).set(1, currentConfidence - 10);
                }
            }
        }
    }

    private void dealUniqueChar() {

        for (int z = 0; z < info.size(); z++) {
            int numCapitalNumbers = 0;
            int numHyphens = 0;

            boolean firstLettersCapital = true;

            ArrayList<String> phraseWords = new ArrayList();
            String phrase = (String)(((Vector)info.get(z)).get(0));
            phraseWords.addAll(Arrays.asList(phrase.split(" ")));
            int numWords = phraseWords.size();

            for (String word : phraseWords) {
                try {
                    for (int y = 0; y < word.length(); y++) {
                        Character c = word.charAt(y);
                        if (Character.isUpperCase(c)) {
                            numCapitalNumbers += 1;
                        }
                        if (c.equals('-') || c.equals('/')) {
                            numHyphens += 1;
                        }
                    }
                    if (word.length() >= 1 && !Character.isUpperCase(word.charAt(0))) {
                        firstLettersCapital = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int curr_confidence = (int)((Vector)(info.get(z))).get(1);
            if (curr_confidence < 200) {
                //System.out.println("For phrase '" + phrase + "': " + numCapitalNumbers + "," + numHyphens );
                ((Vector) (info.get(z))).set(1, (numCapitalNumbers + numHyphens) * 10 + (int) ((Vector) (info.get(z))).get(1));
                ((Vector) (info.get(z))).set(1, numWords * 2 + (int) ((Vector) (info.get(z))).get(1));
                if (firstLettersCapital && numWords > 1) {
                    ((Vector) (info.get(z))).set(1, (int)((Vector)(info.get(z))).get(1) + 25);
                }
            }
        }
    }

    private void cleanup() {

        int maxConfidence = 0;
        int index = 0;

        for (int p = 0; p < info.size(); p++) {
            int confidence = (int)((Vector)(info.get(p))).get(1);
            if (confidence > maxConfidence && ((String)((Vector)info.get(p)).get(0)).matches(".*[a-zA-Z].*")) {
                maxConfidence = confidence;
                index = p;
            }
        }

        String fin_name = "";
        ArrayList<String> phraseWords = new ArrayList();
        String phrase = (String)(((Vector)info.get(index)).get(0));
        phraseWords.addAll(Arrays.asList(phrase.split(" ")));

        for (String word : phraseWords) {
            fin_name += word + " ";
        }
        fin_name = fin_name.substring(0, fin_name.length() - 1);

//         Output
//        System.out.println("Different possible phrases of the title: '" + cermine_title + "'");
//
//        for (int l = 0; l < info.size(); l++) {
//            System.out.println(info.get(l));
//        }

        final_name = fin_name;
    }

}
