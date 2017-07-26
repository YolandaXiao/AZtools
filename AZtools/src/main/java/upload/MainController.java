package upload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
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
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class MainController {

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        return "uploadForm";
    }

    @PostMapping("/")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes, Model model) throws FileNotFoundException {

    	HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");

        ContentExtractor extractor = null;
        InputStream inputStream = null;

        try {
			extractor = new ContentExtractor();
			inputStream = new BufferedInputStream(file.getInputStream());
			extractor.setPDF(inputStream);

			String name = file.getOriginalFilename();
			name = name.split("\\.")[0];

			System.out.println("\nApplying CERMINE to \"" + name + ".pdf\"...");

			//convert pdf to xml
            Element nlmMetadata = extractor.getMetadataAsNLM();
            Element nlmFullText = extractor.getBodyAsNLM(null);
            Element nlmContent = new Element("article");
            for (Object ns : nlmFullText.getAdditionalNamespaces()) {
                if (ns instanceof Namespace) {
                    nlmContent.addNamespaceDeclaration((Namespace)ns);
                }
            }
            Element meta = (Element) nlmMetadata.getChild("front").clone();
            nlmContent.addContent(meta);
            nlmContent.addContent(nlmFullText);

            System.out.println("! Completed CERMINE workflow.\n");

            //convert xml to json
            String nlm = new XMLOutputter().outputString(nlmContent);
            JSONObject xmlJSONObj = null;
			try {
				xmlJSONObj = XML.toJSONObject(nlm);
			} catch (JSONException e) {
				e.printStackTrace();
			}

            ObjectMapper mapper = new ObjectMapper();
            Attributes attr = new Attributes(xmlJSONObj, name);

            //xmlJSONObj.put("name", Attributes.findName(name, attr.getTitle()));
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

}
