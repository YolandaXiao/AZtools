package extraction;

import org.assertj.core.api.SoftAssertions;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.json.JSONObject;
import org.json.XML;
//import org.junit.Before;
import org.junit.Test;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class FundingTest {

    static final private String TEST_PDF = "/Users/yinxuexiao/Documents/Computer_Science/AZtools/AZtools/src/test/resources/chimera.pdf";
//    private ContentExtractor extractor;
//    SAXBuilder saxBuilder;
//
//    @Before
//    public void setUp() throws AnalysisException, IOException {
//        extractor = new ContentExtractor();
//        saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");
//    }

    @Test
    public void getNLMContentTest() throws Exception {
//        InputStream testStream = FundingTest.class.getResourceAsStream(TEST_PDF);
//        Element testContent;
//        extractor.setPDF(testStream);
//        testContent = extractor.getContentAsNLM();
//        String nlm = new XMLOutputter().outputString(testContent);

        ContentExtractor extractor = new ContentExtractor();
        InputStream inputStream = new FileInputStream(TEST_PDF);
        extractor.setPDF(inputStream);
        Element nlmMetadata = extractor.getMetadataAsNLM();
        Element nlmFullText = extractor.getBodyAsNLM(null);
        Element nlmContent = new Element("article");
        for (Object ns : nlmFullText.getAdditionalNamespaces()) {
            if (ns instanceof Namespace) {
                nlmContent.addNamespaceDeclaration((Namespace) ns);
            }
        }
        Element meta = (Element) nlmMetadata.getChild("front").clone();
        nlmContent.addContent(meta);
        nlmContent.addContent(nlmFullText);
        String nlm = new XMLOutputter().outputString(nlmContent);


        JSONObject xmlJSONObj = XML.toJSONObject(nlm);
        String[] arr = TEST_PDF.split("/");
        String filename = arr[arr.length-1];
        Attributes attr = new Attributes(nlm, filename,0);
    }

    @Test
    public void testAdd1Plus1()
    {
        int x  = 1 ; int y = 1;
        assertEquals(2, FundingTest.add(x,y));
    }

    @Test
    public void testAll()
    {
        for(int i=0;i<4;i++){
            testAdd1Plus1();
        }
    }

    @Test
    public void testAssert(){
//        assertThat("They are not equal!","123",is("1234"));
        assertThat("123",is("123"));
//        assertThat("123",is("1234"));

    }

    private static int add(int x, int y) {
        return x+y;
    }
}