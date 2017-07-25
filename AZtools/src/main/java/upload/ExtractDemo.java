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

//    public static void main(String args[]) {
//        String str = "This work was supported by the Biotechnology and Biological Sciences Research Council [grant numbers BB/K020161/1, BB/ K019945/1, BB/K020129/1].";
//        ExtractDemo extractDemo = new ExtractDemo();
//        System.out.println(extractDemo.doNer(str));
//        System.out.println("Complete!");
//    }

//    public String test(String input) {
//
//        ExtractDemo extractDemo = new ExtractDemo();
////        System.out.println(input);
////        System.out.println(extractDemo.doNer(input));
////        System.out.println("Complete!");
//        return extractDemo.doNer(input);
//    }
  
}   