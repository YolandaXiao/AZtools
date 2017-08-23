package webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import extraction.Attributes;
import javafx.concurrent.Task;
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
        Calendar cermine_start = null, cermine_mid1 = null, cermine_mid2 = null, cermine_mid3 = null, cermine_mid4 = null;
        Calendar cermine_end = null, cermine_mid5 = null, cermine_mid6 = null, cermine_mid7 = null, cermine_mid8 = null, cermine_mid9 = null;

        Calendar refining_start= null;
        Calendar refining_end= null;
        Calendar clock_end = null;

        long total_time = 0;
        long refine_total = 0;

        try {
            for (int k = 0; k < files.size(); k++) {
                try {
                    // need to check for pdf only
                    String originalFilename = ((MultipartFile) files.get(k)).getOriginalFilename();
                    originalFilename = originalFilename.split("\\.")[0] + ".pdf";
                    System.out.println("Applying CERMINE to '" + originalFilename + "'...");

                    // apply cermine
                    cermine_start = Calendar.getInstance();
                    ContentExtractor extractor = new ContentExtractor();

                    cermine_mid1 = Calendar.getInstance();
                    InputStream inputStream = new BufferedInputStream(((MultipartFile) files.get(k)).getInputStream());
                    extractor.setPDF(inputStream);

                    cermine_mid2 = Calendar.getInstance();
                    //convert pdf to xml
                    Element nlmMetadata = extractor.getMetadataAsNLM();
                    cermine_mid3 = Calendar.getInstance();

                    Element nlmFullText = extractor.getBodyAsNLM(null);
                    // funding, links
                    cermine_mid4 = Calendar.getInstance();
                    Element nlmContent = new Element("article");
                    cermine_mid5 = Calendar.getInstance();

                    for (Object ns : nlmFullText.getAdditionalNamespaces()) {
                        if (ns instanceof Namespace) {
                            nlmContent.addNamespaceDeclaration((Namespace) ns);
                        }
                    }

                    cermine_mid6 = Calendar.getInstance();
                    Element meta = (Element) nlmMetadata.getChild("front").clone();
                    cermine_mid7 = Calendar.getInstance();
                    nlmContent.addContent(meta);
                    cermine_mid8 = Calendar.getInstance();
                    nlmContent.addContent(nlmFullText);
                    cermine_mid9 = Calendar.getInstance();

                    cermine_end = Calendar.getInstance();
                    System.out.println("Completed CERMINE workflow for '" + originalFilename + "'");

                    String nlm = new XMLOutputter().outputString(nlmContent);

                    refining_start = Calendar.getInstance();
                    Attributes attr = new Attributes(nlm, originalFilename);
                    refining_end = Calendar.getInstance();

                    String json_string = mapper.writeValueAsString(attr);

                    data.put(originalFilename, json_string);
                    total_time += cermine_end.getTimeInMillis() - cermine_start.getTimeInMillis();
                    refine_total += refining_end.getTimeInMillis() - refining_start.getTimeInMillis();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            clock_end = Calendar.getInstance();

            metadata.put("number_of_pdfs", files.size());
            metadata.put("total time (ms)", (clock_end.getTimeInMillis() - clock_start.getTimeInMillis()));
            metadata.put("total cermine_time (ms)", total_time);
            metadata.put("total refining_time (ms)", refine_total);

            String metadata_string = metadata.toString();
            String data_string = data.toString().replace("abstrakt", "abstract");

            final_json_object.put("metadata", metadata_string);
            final_json_object.put("data", data_string);

            String final_result = final_json_object.toString().replace("\\\"", "\"");
            final_result = final_result.replace("\\\\\"", "\"");

            return new ResponseEntity<>(final_result, responseHeaders, HttpStatus.OK);
        }
        catch (TimeoutException e) {
            e.printStackTrace();

            JSONObject status = new JSONObject();
            status.put("number_of_pdfs", files.size());
            status.put("status", "Internal error occurred, please try again later.");

            String status_string = status.toString();
            return new ResponseEntity<>(status_string, null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
