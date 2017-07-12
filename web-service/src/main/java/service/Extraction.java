//package service;
//
//import org.jdom.Element;
//import org.jdom.JDOMException;
//import org.jdom.output.Format;
//import org.jdom.output.XMLOutputter;
//import org.xml.sax.SAXException;
//import pl.edu.icm.cermine.ContentExtractor;
//import pl.edu.icm.cermine.exception.AnalysisException;
//
//import java.io.IOException;
//import java.io.InputStream;
//import static com.sun.org.apache.bcel.internal.util.SecuritySupport.getResourceAsStream;
//
///**
// * Created by yinxuexiao on 7/11/17.
// */
//
//
//public class Extraction {
////    static final private String TEST_PDF = "/pl/edu/icm/cermine/ECDL069.pdf";
////
////    ContentExtractor extractor = new ContentExtractor();
////    InputStream inputStream = new FileInputStream(TEST_PDF);
////    extractor.setPDF(inputStream);
////    Element result = extractor.getContentAsNLM();
//
////    static final private String TEST_PDF = "/pl/edu/icm/cermine/ECDL069.pdf";
//    static final private String TEST_PDF = "src/main/resources/ECDL069.pdf";
//    private ContentExtractor extractor;
//
//    public void setUp() throws AnalysisException, IOException {
//        extractor = new ContentExtractor();
//    }
//
//    public void getNLMContentTest() throws AnalysisException, JDOMException, IOException, SAXException {
//
//        InputStream testStream = getResourceAsStream(TEST_PDF);
//        Element testContent;
//        try {
//            extractor.setPDF(testStream);
//            testContent = extractor.getContentAsNLM();
//        } finally {
//            testStream.close();
//        }
//
//        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
//        String line = outputter.outputString(testContent);
//        System.out.println(line);
//    }
//
//    public void main() throws JDOMException, SAXException, AnalysisException, IOException {
//        try {
//            getNLMContentTest();
//        } catch (AnalysisException e) {
//            e.printStackTrace();
//        } catch (JDOMException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        }
//    }
//}
