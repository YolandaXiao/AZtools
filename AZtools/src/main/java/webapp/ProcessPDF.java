package webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import extraction.Attributes;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.json.JSONObject;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class ProcessPDF {

    private long cermine_time;
    private long aztools_time;
    private long total_time;

    private JSONObject data;
    private JSONObject metadata;
    private JSONObject final_json_object;

    private String data_string;
    private String metadata_string;
    private String final_json_string;

    public ProcessPDF(ArrayList<File> files, ArrayList<String> filenames) {
        ObjectMapper mapper = new ObjectMapper();

        data = new JSONObject();
        metadata = new JSONObject();
        final_json_object = new JSONObject();

        cermine_time = 0;
        aztools_time = 0;
        total_time = 0;

        Calendar cermine_start, cermine_end;
        Calendar aztools_start, aztools_end;
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

                    aztools_start = Calendar.getInstance();
                    Attributes attr = new Attributes(nlm, filenames.get(i), 0);
                    aztools_end = Calendar.getInstance();
                    aztools_time += aztools_end.getTimeInMillis() - aztools_start.getTimeInMillis();
                    data.put(filenames.get(i), mapper.writeValueAsString(attr));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            clock_end = Calendar.getInstance();
            total_time = clock_end.getTimeInMillis() - clock_start.getTimeInMillis();

            metadata.put("num_pdfs", files.size());
            metadata.put("total time", total_time);
            metadata.put("cermine_time", total_time);
            metadata.put("aztools_time", aztools_time);
            metadata_string = metadata.toString();

            data_string = data.toString();
            final_json_object.put("metadata", metadata_string);
            final_json_object.put("data", data_string);
            final_json_string = final_json_object.toString();

            metadata_string = metadata_string.replace("\\\"", "\"");
            metadata_string = metadata_string.replace("\\\\\"", "\"");
            data_string = data_string.replace("\\\"", "\"");
            data_string = data_string.replace("\\\\\"", "\"");
            final_json_string = final_json_string.replace("\\\"", "\"");
            final_json_string = final_json_string.replace("\\\\\"", "\"");
        }
        catch (TimeoutException e) {
            e.printStackTrace();
            JSONObject status = new JSONObject();
            status.put("status", "Internal error occurred, please try again later.");
            final_json_string = status.toString().replace("\\\"", "\"").replace("\\\\\"", "\"");
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    public String getFinalString() {
        return final_json_string;
    }

    public String getDataString() {
        return data_string;
    }

    public String getMetadataString() {
        return metadata_string;
    }

    ///////////////////////////////////////////////////////////////////////////

    public JSONObject getData() {
        return data;
    }

    public JSONObject getMetaData() {
        return metadata;
    }

    public JSONObject getFinalJsonObject() {
        return final_json_object;
    }

    ///////////////////////////////////////////////////////////////////////////

    public long getCermineTime() {
        return cermine_time;
    }

    public long getRefineTime() {
        return aztools_time;
    }

    public long getTotalTime() {
        return total_time;
    }

}
