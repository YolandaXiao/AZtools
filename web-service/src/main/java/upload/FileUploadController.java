package upload;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;
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

import com.google.common.net.MediaType;

import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;
//import upload.storage.StorageFileNotFoundException;
//import upload.storage.StorageService;

@Controller
public class FileUploadController {

	/*
    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }
	*/
    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        /*
          model.addAttribute("files", storageService
         
                .loadAll()
                .map(path ->
                        MvcUriComponentsBuilder
                                .fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
                                .build().toString())
                .collect(Collectors.toList()));
		*/
        return "uploadForm";
    }

    /*
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
                .body(file);
    }
    */

    @PostMapping("/")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) throws FileNotFoundException {

        //storageService.store(file);
        //redirectAttributes.addFlashAttribute("message",
        //        "Successfully uploaded " + file.getOriginalFilename() + "!");
    	
    	HttpHeaders responseHeaders = new HttpHeaders();
        //responseHeaders.setContentType(MediaType.APPLICATION_XML);
        responseHeaders.add("Content-Type", "application/xml; charset=UTF-8");
        
        ContentExtractor extractor = null;
        InputStream inputStream = null;
        Element result = null;
        try {       
			extractor = new ContentExtractor();
			inputStream = new BufferedInputStream(file.getInputStream());
			//inputStream = new FileInputStream("path/to/pdf/file");
			extractor.setPDF(inputStream);
			result = extractor.getContentAsNLM();
			String nlm = new XMLOutputter().outputString(result);
			return new ResponseEntity<String>(nlm, responseHeaders, HttpStatus.OK);
		} catch (IOException | TimeoutException | AnalysisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<String>("Exception!!", null, HttpStatus.INTERNAL_SERVER_ERROR);
		}	

    }

    //@ExceptionHandler(StorageFileNotFoundException.class)
    //public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
    //    return ResponseEntity.notFound().build();
    //}

}
