package webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import extraction.Attributes;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class ProcessPDF {

    private long cermine_time;
    private long refine_time;
    private long total_time;

    private JSONObject data;
    private JSONObject metadata;
    private JSONObject final_json_object;

    private String data_string;
    private String metadata_string;
    private String final_json_string;

    public ProcessPDF(ArrayList<File> files, ArrayList<String> filenames, boolean ignore) {
        ObjectMapper mapper = new ObjectMapper();

        data = new JSONObject();
        metadata = new JSONObject();
        final_json_object = new JSONObject();

        cermine_time = 0;
        refine_time = 0;
        total_time = 0;

        Calendar cermine_start, cermine_end;
        Calendar refining_start, refining_end;
        Calendar clock_start = Calendar.getInstance(), clock_end;

        try {
            for (int i = 0; i < files.size(); i++) {
                try {
                    cermine_start = Calendar.getInstance();
                    ContentExtractor extractor = new ContentExtractor();
                    InputStream inputStream = new FileInputStream(files.get(i));
                    extractor.setPDF(inputStream);
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
                    String nlm = new XMLOutputter().outputString(nlmContent);
                    cermine_end = Calendar.getInstance();
                    cermine_time += cermine_end.getTimeInMillis() - cermine_start.getTimeInMillis();

                    refining_start = Calendar.getInstance();
                    Attributes attr = new Attributes(nlm, filenames.get(i), 0);
                    refining_end = Calendar.getInstance();
                    refine_time += refining_end.getTimeInMillis() - refining_start.getTimeInMillis();

                    String json_string = mapper.writeValueAsString(attr);
                    System.out.println("---------------\n" + filenames.get(i) + ":\n" + json_string + "\n---------------");
                    data.put(filenames.get(i), json_string);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            clock_end = Calendar.getInstance();

            metadata.put("number_of_pdfs", files.size());
            total_time = clock_end.getTimeInMillis() - clock_start.getTimeInMillis();
            metadata.put("total time (ms)", total_time);
            metadata.put("total cermine_time (ms)", total_time);
            metadata.put("total refine_time (ms)", refine_time);
            metadata_string = metadata.toString();

            data_string = data.toString().replace("abstrakt", "abstract").replace("\\\"", "\"");
            System.out.println(data_string);

            final_json_object.put("metadata", metadata_string);
            final_json_object.put("data", data_string);

            String final_result = final_json_object.toString().replace("\\\"", "\"");
            final_json_string = final_result.replace("\\\\\"", "\"");
        }
        catch (TimeoutException e) {
            e.printStackTrace();
            JSONObject status = new JSONObject();
            status.put("status", "Internal error occurred, please try again later.");
            final_json_string = status.toString();
        }
    }

    public ProcessPDF(File file, String filename) {
        ObjectMapper mapper = new ObjectMapper();

        data = new JSONObject();
        metadata = new JSONObject();
        final_json_object = new JSONObject();

        cermine_time = 0;
        refine_time = 0;
        total_time = 0;

        Calendar cermine_start, cermine_end;
        Calendar refining_start, refining_end;
        Calendar clock_start = Calendar.getInstance(), clock_end;

        try {
            cermine_start = Calendar.getInstance();
            ContentExtractor extractor = new ContentExtractor();
            InputStream inputStream = new FileInputStream(file);
            extractor.setPDF(inputStream);
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
            String nlm = new XMLOutputter().outputString(nlmContent);
            cermine_end = Calendar.getInstance();
            cermine_time += cermine_end.getTimeInMillis() - cermine_start.getTimeInMillis();

            refining_start = Calendar.getInstance();
            Attributes attr = new Attributes(nlm, filename, 0);
            refining_end = Calendar.getInstance();
            refine_time += refining_end.getTimeInMillis() - refining_start.getTimeInMillis();

            String json_string = mapper.writeValueAsString(attr);
            data.put(filename, json_string);

            clock_end = Calendar.getInstance();

            metadata.put("number_of_pdfs", 1);
            total_time = clock_end.getTimeInMillis() - clock_start.getTimeInMillis();
            metadata.put("total time (ms)", total_time);
            metadata.put("total cermine_time (ms)", total_time);
            metadata.put("total refine_time (ms)", refine_time);
            metadata_string = metadata.toString();

            data_string = data.toString().replace("abstrakt", "abstract");

            final_json_object.put("metadata", metadata_string);
            final_json_object.put("data", data_string);

            String final_result = final_json_object.toString().replace("\\\"", "\"");
            final_json_string = final_result.replace("\\\\\"", "\"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ProcessPDF(ArrayList<MultipartFile> files, ArrayList<String> filenames) {
        ObjectMapper mapper = new ObjectMapper();

        data = new JSONObject();
        metadata = new JSONObject();
        final_json_object = new JSONObject();

        cermine_time = 0;
        refine_time = 0;
        total_time = 0;

        Calendar cermine_start, cermine_end;
        Calendar refining_start, refining_end;
        Calendar clock_start = Calendar.getInstance(), clock_end;

        try {
            for (int i = 0; i < files.size(); i++) {
                try {
                    cermine_start = Calendar.getInstance();
                    ContentExtractor extractor = new ContentExtractor();
                    extractor.setPDF((files.get(i)).getInputStream());
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
                    String nlm = new XMLOutputter().outputString(nlmContent);
                    cermine_end = Calendar.getInstance();
                    cermine_time += cermine_end.getTimeInMillis() - cermine_start.getTimeInMillis();

                    refining_start = Calendar.getInstance();
                    Attributes attr = new Attributes(nlm, filenames.get(i), 0);
                    refining_end = Calendar.getInstance();
                    refine_time += refining_end.getTimeInMillis() - refining_start.getTimeInMillis();

                    String json_string = mapper.writeValueAsString(attr);
                    System.out.println("---------------\n" + filenames.get(i) + ":\n" + json_string + "\n---------------");
                    data.put(filenames.get(i), json_string);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            clock_end = Calendar.getInstance();

            metadata.put("number_of_pdfs", files.size());
            total_time = clock_end.getTimeInMillis() - clock_start.getTimeInMillis();
            metadata.put("total time (ms)", total_time);
            metadata.put("total cermine_time (ms)", total_time);
            metadata.put("total refine_time (ms)", refine_time);
            metadata_string = metadata.toString();

            data_string = data.toString().replace("abstrakt", "abstract");
            System.out.println(data_string);

            final_json_object.put("metadata", metadata_string);
            final_json_object.put("data", data_string);

            String final_result = final_json_object.toString().replace("\\\"", "\"");
            final_json_string = final_result.replace("\\\\\"", "\"");
        }
        catch (TimeoutException e) {
            e.printStackTrace();
            JSONObject status = new JSONObject();
            status.put("status", "Internal error occurred, please try again later.");
            final_json_string = status.toString();
        }
    }

    public ProcessPDF(MultipartFile file, String filename) {
        ObjectMapper mapper = new ObjectMapper();

        data = new JSONObject();
        metadata = new JSONObject();
        final_json_object = new JSONObject();

        cermine_time = 0;
        refine_time = 0;
        total_time = 0;

        Calendar cermine_start, cermine_end;
        Calendar refining_start, refining_end;
        Calendar clock_start = Calendar.getInstance(), clock_end;

        try {
            cermine_start = Calendar.getInstance();
            ContentExtractor extractor = new ContentExtractor();
            extractor.setPDF(file.getInputStream());
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
            String nlm = new XMLOutputter().outputString(nlmContent);
            cermine_end = Calendar.getInstance();
            cermine_time += cermine_end.getTimeInMillis() - cermine_start.getTimeInMillis();

            refining_start = Calendar.getInstance();
            Attributes attr = new Attributes(nlm, filename, 0);
            refining_end = Calendar.getInstance();
            refine_time += refining_end.getTimeInMillis() - refining_start.getTimeInMillis();

            String json_string = mapper.writeValueAsString(attr);
            data.put(filename, json_string);

            clock_end = Calendar.getInstance();

            metadata.put("number_of_pdfs", 1);
            total_time = clock_end.getTimeInMillis() - clock_start.getTimeInMillis();
            metadata.put("total time (ms)", total_time);
            metadata.put("total cermine_time (ms)", total_time);
            metadata.put("total refine_time (ms)", refine_time);
            metadata_string = metadata.toString();

            data_string = data.toString().replace("abstrakt", "abstract");

            final_json_object.put("metadata", metadata_string);
            final_json_object.put("data", data_string);

            String final_result = final_json_object.toString().replace("\\\"", "\"");
            final_json_string = final_result.replace("\\\\\"", "\"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////

    public String getFinalString() {
        return final_json_string;
    }

    public String getDataString() {
        return data_string;
    }

    public String getMetadataString() {
        return metadata_string;
    }

    public JSONObject getData() {
        return data;
    }

    public JSONObject getMetaData() {
        return metadata;
    }

    public JSONObject getFinalJsonObject() {
        return final_json_object;
    }

    public long getCermineTime() {
        return cermine_time;
    }

    public long getRefineTime() {
        return refine_time;
    }

    public long getTotalTime() {
        return total_time;
    }

}
