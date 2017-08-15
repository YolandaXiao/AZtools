package webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xpath.internal.operations.Mult;
import extraction.Attributes;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.json.JSONObject;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Controller
public class MainController extends Thread {

    @GetMapping("/")
    public String upload(Model model) throws IOException {
        return "upload";
    }

    public static Map<Integer, MultipartFile> map_files = new HashMap<>();
    private JSONObject data;
    private int number;

    @Autowired
    public MainController() {
        data = new JSONObject();
    }

    private void setNumber(Integer i) {
        number = i;
    }

    @PostMapping("/")
    public ResponseEntity<String> process(@RequestParam("file") List<MultipartFile> files,
                                                   RedirectAttributes redirectAttributes, Model model)
            throws Exception {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");

        JSONObject final_json_object = new JSONObject();
        JSONObject metadata = new JSONObject();

        metadata.put("number_of_pdfs", files.size());
        String metadata_string = metadata.toString();
        final_json_object.put("metadata", metadata_string);

        try {
            ArrayList<Map<String, String>> data_file = new ArrayList<>();
            for (int k = 0; k < files.size(); k++) {
                map_files.put(k, files.get(k));

                ApplicationContext context = new ClassPathXmlApplicationContext("Beans.xml");
                MainController t = (MainController) context.getBean("mainController");

                t.setNumber(k);
                t.start();

                String data_string = t.data.toString().replace("abstrakt", "abstract");
                final_json_object.put("data", data_string);
            }

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

    @Autowired
    @Override
    public void run() {
        try {
            MultipartFile file = map_files.get(number);
            String originalFilename = file.getOriginalFilename();
            originalFilename = originalFilename.split("\\.")[0] + ".pdf";
            ContentExtractor extractor = new ContentExtractor();

            InputStream inputStream = new BufferedInputStream(file.getInputStream());
            extractor.setPDF(inputStream);

            ObjectMapper mapper = new ObjectMapper();
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

            System.out.println("Completed CERMINE workflow for '" + originalFilename + "'");

            String nlm = new XMLOutputter().outputString(nlmContent);
            Attributes attr = new Attributes(nlm, originalFilename);
            attr.printFunding();

            String json_string = mapper.writeValueAsString(attr);
            data.put(originalFilename, json_string);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

