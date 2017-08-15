package webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import extraction.Attributes;
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
public class MainController extends Thread {

    @GetMapping("/")
    public String upload(Model model) throws IOException {
        return "upload";
    }

    private ContentExtractor ex;
    private String file_name;
    private JSONObject data;

    public MainController(ContentExtractor extractor, String filename) {
        ex = extractor;
        file_name = filename;
        data = new JSONObject();
    }

    @Override
    public void run() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Element nlmMetadata = ex.getMetadataAsNLM();
            Element nlmFullText = ex.getBodyAsNLM(null);
            Element nlmContent = new Element("article");

            for (Object ns : nlmFullText.getAdditionalNamespaces()) {
                if (ns instanceof Namespace) {
                    nlmContent.addNamespaceDeclaration((Namespace) ns);
                }
            }

            Element meta = (Element) nlmMetadata.getChild("front").clone();
            nlmContent.addContent(meta);
            nlmContent.addContent(nlmFullText);

            System.out.println("Completed CERMINE workflow for '" + file_name + "'");

            String nlm = new XMLOutputter().outputString(nlmContent);
            Attributes attr = new Attributes(nlm, file_name);
            attr.printFunding();

            String json_string = mapper.writeValueAsString(attr);

            data.put(file_name, json_string);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/")
    public ResponseEntity<String> process(@RequestParam("file") List<MultipartFile> files,
                                                   RedirectAttributes redirectAttributes, Model model)
            throws Exception {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");

        JSONObject final_json_object = new JSONObject();
        JSONObject metadata = new JSONObject();

//        Calendar clock_start = Calendar.getInstance();
//        Calendar cermine_start = null;
//        Calendar cermine_end= null;
//
//        Calendar refining_start= null;
//        Calendar refining_end= null;
//        Calendar clock_end = null;

        metadata.put("number_of_pdfs", files.size());
        String metadata_string = metadata.toString();
        final_json_object.put("metadata", metadata_string);

        try {
            for (int k = 0; k < files.size(); k++) {
                String originalFilename = (files.get(k)).getOriginalFilename();
                originalFilename = originalFilename.split("\\.")[0] + ".pdf";
                ContentExtractor extractor = new ContentExtractor();
                InputStream inputStream = new BufferedInputStream((files.get(k)).getInputStream());
                extractor.setPDF(inputStream);
                MainController t = new MainController(extractor, originalFilename);
                t.start();
                String data_string = t.data.toString().replace("abstrakt", "abstract");
                final_json_object.put("data", data_string);
            }

            String final_result = final_json_object.toString().replace("\\\"", "\"");
            final_result = final_result.replace("\\\\\"", "\"");
            return new ResponseEntity<>(final_result, responseHeaders, HttpStatus.OK);
        }
        catch (IOException | TimeoutException | AnalysisException e) {
            e.printStackTrace();

            JSONObject status = new JSONObject();
            status.put("number_of_pdfs", files.size());
            status.put("status", "Internal error occurred, please try again later.");
            String status_string = status.toString();
            return new ResponseEntity<>(status_string, null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

