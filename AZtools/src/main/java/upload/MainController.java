package upload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class MainController {

    @GetMapping("/")
    public String uploadOnePDF(Model model) throws IOException {
        return "uploadOnePDF";
    }

    @PostMapping("/")
    public ResponseEntity<String> processOnePDF(@RequestParam("file") MultipartFile file,
                                                   RedirectAttributes redirectAttributes, Model model)
                                throws Exception {

    	HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");

        ContentExtractor extractor = new ContentExtractor();
        InputStream inputStream = null;

        ObjectMapper mapper = new ObjectMapper();

        try {
            inputStream = new BufferedInputStream(file.getInputStream());
            extractor.setPDF(inputStream);

            String name = file.getOriginalFilename();
            name = name.split("\\.")[0];

            System.out.println("Applying CERMINE to \"" + name + ".pdf\"...");

            //convert pdf to xml
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

            System.out.println("! Completed CERMINE workflow.");

            //convert xml to json
            String nlm = new XMLOutputter().outputString(nlmContent);
            Attributes attr = new Attributes(nlm, name);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            String jsonString = mapper.writeValueAsString(attr);
            String result = jsonString.replace("abstrakt", "abstract");

			return new ResponseEntity<String>(result, responseHeaders, HttpStatus.OK);

		}
		catch (IOException | TimeoutException | AnalysisException e) {
			e.printStackTrace();
			return new ResponseEntity<String>("Exception!!", null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }

    @GetMapping("/batch")
    public String uploadManyPDFs(Model model) throws IOException {
        return "uploadManyPDFs";
    }

    @PostMapping("/batch")
    public ResponseEntity<String> processManyPDFs(@RequestParam("file") List<MultipartFile> files,
                                                   RedirectAttributes redirectAttributes, Model model)
            throws Exception {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        String final_result = "\n";

        try {

            for (int k = 0; k < files.size(); k++) {

                ContentExtractor extractor = new ContentExtractor();

                InputStream inputStream = new BufferedInputStream(((MultipartFile)files.get(k)).getInputStream());
                extractor.setPDF(inputStream);

                String name = ((MultipartFile)files.get(k)).getOriginalFilename();
                name = name.split("\\.")[0];

                System.out.println("Applying CERMINE to \"" + name + ".pdf\"...");

                //convert pdf to xml
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

                System.out.println("! Completed CERMINE workflow.");

                //convert xml to json
                String nlm = new XMLOutputter().outputString(nlmContent);
                Attributes attr = new Attributes(nlm, name);
                mapper.enable(SerializationFeature.INDENT_OUTPUT);

                String jsonString = mapper.writeValueAsString(attr);
                String result = jsonString.replace("abstrakt", "abstract");

                final_result += (k+1) + " " + result + "\n";
            }

            return new ResponseEntity<String>(final_result, responseHeaders, HttpStatus.OK);
        }
        catch (IOException | TimeoutException | AnalysisException e) {
            e.printStackTrace();
            return new ResponseEntity<String>("Exception!!", null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
