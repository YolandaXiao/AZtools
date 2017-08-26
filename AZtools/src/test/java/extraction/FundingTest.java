package extraction;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class FundingTest {

    static final private String TEST_PDF = "../chimera.pdf";
    private ContentExtractor extractor;
    SAXBuilder saxBuilder;

    @Before
    public void setUp() throws AnalysisException, IOException {
        extractor = new ContentExtractor();
        saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");
    }

    @Test
    public void getNLMContentTest() throws Exception {
        InputStream testStream = FundingTest.class.getResourceAsStream(TEST_PDF);
        Element testContent;
        extractor.setPDF(testStream);
        testContent = extractor.getContentAsNLM();
        String nlm = new XMLOutputter().outputString(testContent);
        String[] arr = TEST_PDF.split("/");
        String filename = arr[arr.length-1];
        Attributes attr = new Attributes(nlm, filename,0);
        attr.printFunding();
    }







    @Test
    public void testAdd1Plus1()
    {
        int x  = 1 ; int y = 1;
        assertEquals(2, FundingTest.add(x,y));
    }

    private static int add(int x, int y) {
        return x+y;
    }
}