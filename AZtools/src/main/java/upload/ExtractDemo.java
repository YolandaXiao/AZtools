package upload;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;   
import edu.stanford.nlp.ling.CoreLabel;

public class ExtractDemo {   
    private static AbstractSequenceClassifier<CoreLabel> ner;

    public ExtractDemo() {

        InitNer();
    }

    public void InitNer() {
        String serializedClassifier = "lib/stanford-ner-2017-06-09/classifiers/english.conll.4class.distsim.crf.ser"; // chinese.misc.distsim.crf.ser.gz
        if (ner == null) {
        ner = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
        }
    }

    public String doNer(String sent) {
        return ner.classifyWithInlineXML(sent);
    }
  
}   