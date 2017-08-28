package webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import extraction.Attributes;
import javafx.concurrent.Task;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

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
                    Attributes attr = new Attributes(nlm, originalFilename,0);
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


    //for PMC ID input
    @PostMapping(value = "/pmc_id")
    public ResponseEntity<String> authenticateUser(@RequestParam("PMC_ID") String pmc_id, Model model) throws Exception {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");
        System.out.println("coming in controller    " +pmc_id);

        //get xml
        String url_link = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pmc&format=xml&id=PMC"+pmc_id;
        String html = getHTML(url_link);
        System.out.println(html);

        //check if it's open access
        String pattern = "(?s)<body>.*</body>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(html);
        boolean flag = false;
        while (m.find( )) {
            flag = true;
        }
        if(!flag){
            String return_value = "Sorry! PubMed Central doesn't allow full text access on this file!";
            return new ResponseEntity<>(return_value, responseHeaders, HttpStatus.OK);
        }

        //get rid of reference section
        String html_withoutref = html.split("<ref-list>")[0];
        html_withoutref += html.split("</ref-list>")[1];

        //get rid of outmost tag
        html_withoutref = html_withoutref.split("<pmc-articleset>")[1];
        html_withoutref = html_withoutref.split("</pmc-articleset>")[0];

        //remove <bold> and <italics> tag
        html_withoutref = html_withoutref.replaceAll("<\\/?bold>","");
        html_withoutref = html_withoutref.replaceAll("<\\/?italic>","");

        //xml to json
        JSONObject xmlJSONObj = XML.toJSONObject(html_withoutref);
        String return_value = xmlJSONObj.toString();

        //
        ObjectMapper mapper = new ObjectMapper();
        Attributes attr = new Attributes(html_withoutref, "tmp",1);
        String json_string = mapper.writeValueAsString(attr);

        return new ResponseEntity<>(json_string, responseHeaders, HttpStatus.OK);
    }

    //helper function: get HTML content
    private static String getHTML(String urlToRead) throws Exception {
        try{
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000); // timeout = 3 seconds
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            return result.toString();
        }
        catch (Exception e){
            return "";
        }
    }

}
