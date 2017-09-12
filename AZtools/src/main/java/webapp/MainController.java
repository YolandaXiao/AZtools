package webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import extraction.Attributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class MainController {

    @GetMapping("/")
    public String upload(Model model) throws IOException {
        return "upload";
    }

    @PostMapping("/")
    public ResponseEntity<String> process(@RequestParam("file") List<MultipartFile> files,
                                          RedirectAttributes redirectAttributes, Model model) throws Exception {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");
        ArrayList<MultipartFile> f_files = new ArrayList();
        ArrayList<File> fnm_files = new ArrayList();
        ArrayList<String> f_filenames = new ArrayList();
        for (MultipartFile file : files) {
            f_files.add(file);
            f_filenames.add(file.getOriginalFilename());

            File convFile = new File(file.getOriginalFilename());
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();
            fnm_files.add(convFile);
        }
        String result = (new ProcessPDF(fnm_files, f_filenames)).getDataString();
        // .getDataString() for only PDF metadata
        // .getMetadataString() for processing stats
        // .getFinalString() for both

        System.out.println("Check response.");
        return new ResponseEntity(result, responseHeaders, HttpStatus.OK);
    }

    @PostMapping(value = "/pmc_id")
    public ResponseEntity<String> authenticateUser(@RequestParam("PMC_ID") String pmc_id,
                                                   Model model) throws Exception {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");

        //get xml
        String url_link = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pmc&format=xml&id=PMC" + pmc_id;
        String html = getHTML(url_link);

        //check if it's open access
        String pattern = "(?s)<body>.*</body>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(html);
        boolean flag = false;
        while (m.find( )) {
            flag = true;
        }
        if(!flag){
            String return_value = "Sorry! PubMed Central doesn't allow full text access for this ID!";
            return new ResponseEntity(return_value, responseHeaders, HttpStatus.OK);
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

        ObjectMapper mapper = new ObjectMapper();
        Attributes attr = new Attributes(html_withoutref, "tmp",1);
        String json_string = mapper.writeValueAsString(attr);

        return new ResponseEntity(json_string, responseHeaders, HttpStatus.OK);
    }

    // helper function: get HTML content
    private static String getHTML(String urlToRead) throws Exception {
        try{
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1500);
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
