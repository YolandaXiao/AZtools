package upload;

/**
 * Created by yinxuexiao on 7/20/17.
 */

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Funding {
    private static String funding_agencies;

    public static String getFunding_agencies(){
        return funding_agencies;
    };


    public static void main(String args) throws InvalidFormatException,
            IOException {

        Logger log = LoggerFactory.getLogger(Funding.class);

        // Load the model file downloaded from OpenNLP
        // http://opennlp.sourceforge.net/models-1.5/en-ner-person.bin
        TokenNameFinderModel model = new TokenNameFinderModel(new File(
                "input/en-ner-organization.bin"));

        // Create a NameFinder using the model
        NameFinderME finder = new NameFinderME(model);

        Tokenizer tokenizer = SimpleTokenizer.INSTANCE;

        // Split the sentence into tokens
        String[] tokens = tokenizer.tokenize(args);

        // Find the names in the tokens and return Span objects
        Span[] nameSpans = finder.find(tokens);

        // Print the names extracted from the tokens using the Span data
//        log.info(Arrays.toString(Span.spansToStrings(nameSpans, tokens)));

        String result = Arrays.toString(Span.spansToStrings(nameSpans, tokens));
        funding_agencies = result;
    }
}