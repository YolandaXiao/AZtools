package upload;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
                                   RedirectAttributes redirectAttributes, Model model) throws FileNotFoundException {

        //storageService.store(file);
        //redirectAttributes.addFlashAttribute("message",
        //        "Successfully uploaded " + file.getOriginalFilename() + "!");
    	
    	HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");
        
        ContentExtractor extractor = null;
        InputStream inputStream = null;
//        Element result = null;
        try {       
			extractor = new ContentExtractor();
			inputStream = new BufferedInputStream(file.getInputStream());
			extractor.setPDF(inputStream);

			//convert body and metadata section (without reference section) to xml
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

            //convert xml to json output
            String nlm = new XMLOutputter().outputString(nlmContent);
            JSONObject xmlJSONObj = XML.toJSONObject(nlm);
	        String jsonPrettyPrintString = xmlJSONObj.toString();

            //filter out excessive data
            ObjectMapper mapper = new ObjectMapper();
            Attributes attr = new Attributes(xmlJSONObj);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String jsonString = mapper.writeValueAsString(attr);
//            model.addAttribute("message", jsonString);

			return new ResponseEntity<String>(jsonString, responseHeaders, HttpStatus.OK);
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
