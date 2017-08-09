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

import java.util.Calendar;
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
    public String upload(Model model) throws IOException {
        return "upload";
    }

    @PostMapping("/")
    public ResponseEntity<String> process(@RequestParam("file") List<MultipartFile> files,
                                                   RedirectAttributes redirectAttributes, Model model)
            throws Exception {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        JSONObject final_json_object = new JSONObject();

        JSONObject data = new JSONObject();
        JSONObject metadata = new JSONObject();

        Calendar clock_start = Calendar.getInstance();
        Calendar cermine_start = null;
        Calendar cermine_end= null;

        Calendar refining_start= null;
        Calendar refining_end= null;
        Calendar clock_end = null;

        try {
            for (int k = 0; k < files.size(); k++) {

                // need to check for pdf only
                String originalFilename = ((MultipartFile)files.get(k)).getOriginalFilename();
                originalFilename = originalFilename.split("\\.")[0] + ".pdf";
                System.out.println("Applying CERMINE to '" + originalFilename + "'...");

                // apply cermine
                cermine_start = Calendar.getInstance();
                ContentExtractor extractor = new ContentExtractor();

                InputStream inputStream = new BufferedInputStream(((MultipartFile)files.get(k)).getInputStream());
                extractor.setPDF(inputStream);

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

                cermine_end = Calendar.getInstance();
                System.out.println("Completed CERMINE workflow for '" + originalFilename + "'");

                String nlm = new XMLOutputter().outputString(nlmContent);
                refining_start = Calendar.getInstance();
                Attributes attr = new Attributes(nlm, originalFilename);

                refining_end = Calendar.getInstance();
                String json_string = mapper.writeValueAsString(attr);

                data.put(originalFilename, json_string);
            }

            clock_end = Calendar.getInstance();

            metadata.put("number_of_pdfs", files.size());
            metadata.put("total_time(ms)", (clock_end.getTimeInMillis() - clock_start.getTimeInMillis()));

            metadata.put("cermine_time(ms)", (cermine_end.getTimeInMillis() - cermine_start.getTimeInMillis()));
            metadata.put("refining_time(ms)", (refining_end.getTimeInMillis() - refining_start.getTimeInMillis()));

            String metadata_string = metadata.toString();
            String data_string = data.toString().replace("abstrakt", "abstract");

            final_json_object.put("metadata", metadata_string);
            final_json_object.put("data", data_string);

            String final_result = final_json_object.toString().replace("\\\"", "\"");
            final_result = final_result.replace("\\\\\"", "\"");

            return new ResponseEntity<String>(final_result, responseHeaders, HttpStatus.OK);
        }
        catch (IOException | TimeoutException | AnalysisException e) {
            e.printStackTrace();

            JSONObject status = new JSONObject();
            status.put("number_of_pdfs", files.size());
            status.put("status", "Internal error occurred, please try again later.");

            String status_string = status.toString();
            return new ResponseEntity<String>(status_string, null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

