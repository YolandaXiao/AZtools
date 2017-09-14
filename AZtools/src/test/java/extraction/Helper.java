package extraction;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

import java.io.*;

public class Helper {

    public Helper() throws Exception {}

    //read in data from txt
    public String readintxt(String fileName) throws IOException {
//        // This will reference one line at a time
//        String line = null;
//
//        try {
//            // FileReader reads text files in the default encoding.
//            FileReader fileReader =
//                    new FileReader(fileName);
//
//            // Always wrap FileReader in BufferedReader.
//            BufferedReader bufferedReader =
//                    new BufferedReader(fileReader);
//
//            while((line = bufferedReader.readLine()) != null) {
//                System.out.println(line);
//            }
//
//            // Always close files.
//            bufferedReader.close();
//        }
//        catch(FileNotFoundException ex) {
//            System.out.println(
//                    "Unable to open file '" +
//                            fileName + "'");
//        }
//        catch(IOException ex) {
//            System.out.println(
//                    "Error reading file '"
//                            + fileName + "'");
//            // Or we could just do this:
//            // ex.printStackTrace();
//        }
//    }
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String everything = "";
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
        } finally {
            br.close();
        }
        return everything;
    }

    //run cermine and AZtools to get all attributes
    public Attributes getAttr(String test_pdf, String complete_path) throws Exception {
        // apply cermine
        ContentExtractor extractor = new ContentExtractor();
        FileInputStream fis = new FileInputStream(complete_path);
        InputStream inputStream = new BufferedInputStream(fis);
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
        String[] arr = test_pdf.split("/");
        String filename = arr[arr.length-1];
        Attributes attr = new Attributes(nlm, filename,0);
        return attr;
    }
}
