package webapp;

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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

        ArrayList<File> f_files = new ArrayList<>();
        for (MultipartFile file : files) {
            f_files.add((File)file);
        }
        ProcessPDF p_pdfs = new ProcessPDF(f_files);
        return new ResponseEntity<>(p_pdfs.getMetadataString(), responseHeaders, HttpStatus.OK);
    }

    @GetMapping("/dev")
    public String upload_dev(Model model) throws IOException {
        return "upload_dev";
    }

    @PostMapping("/dev")
    public ResponseEntity<String> process_dev(@RequestParam("file") List<MultipartFile> files,
                                          RedirectAttributes redirectAttributes, Model model)
            throws Exception {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");

        ArrayList<File> f_files = new ArrayList<>();
        for (MultipartFile file : files) {
            f_files.add((File)file);
        }
        ProcessPDF p_pdfs = new ProcessPDF(f_files);
        return new ResponseEntity<>(p_pdfs.getFinalString(), responseHeaders, HttpStatus.OK);
    }

}
