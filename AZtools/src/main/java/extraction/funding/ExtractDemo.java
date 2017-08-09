package extraction.funding;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import extraction.Properties;

public class ExtractDemo {   
    private static AbstractSequenceClassifier<CoreLabel> ner;

    public ExtractDemo() {
        InitNer();
    }

    public void InitNer() {
        if (ner == null) {
            ner = CRFClassifier.getClassifierNoExceptions(Properties.getSerializedClassifier());
        }
    }

    public String doNer(String sent) {
        return ner.classifyWithInlineXML(sent);
    }
}
