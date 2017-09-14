package webapp;

import extraction.Attributes;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.json.JSONObject;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

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

    private String last_filename;

    private Map<String, Attributes> metadata_info = new HashMap<>();

    public ProcessPDF(ArrayList<File> files, ArrayList<String> filenames) {
        data = new JSONObject();
        metadata = new JSONObject();
        final_json_object = new JSONObject();

        cermine_time = 0;
        aztools_time = 0;
        total_time = 0;

        Calendar cermine_start, cermine_end;
        Calendar aztools_start, aztools_end;
        Calendar clock_start = Calendar.getInstance(), clock_end;

        int spacesToIndentEachLevel = 4;

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
                    metadata_info.put(filenames.get(i), attr);
                    aztools_end = Calendar.getInstance();
                    aztools_time += aztools_end.getTimeInMillis() - aztools_start.getTimeInMillis();
                    data.put(filenames.get(i), attr.getFinalJSONObject());

                    String pubDOI = attr.getDOI();
                    List<String> pubDOIs = new ArrayList<>();
                    if (!pubDOI.equals("")) {
                        pubDOIs.add(pubDOI);
                    }

                    URL url = new URL("http://10.44.115.202:8983/solr/BD2K/select?q=*%3A*&wt=json&indent=true");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {  content.append(inputLine);  }
                    in.close();
                    String json_response_str = content.toString();
                    JSONObject json_reponse = new JSONObject(json_response_str);
                    JSONObject json_obj = (JSONObject) json_reponse.get("response");
                    int num_tools = (int)json_obj.get("numFound");

                    SolrClient client = new HttpSolrClient.Builder("http://10.44.115.202:8983/solr/BD2K/").build();
                    SolrInputDocument doc = new SolrInputDocument();
                    doc.addField("id", num_tools + 1);
                    doc.addField("name", attr.getName());
                    doc.addField("source", "AZtools");
                    doc.addField("fundingAgencies", attr.getFundingStr());
                    doc.addField("institutions", attr.getAffiliation());
                    doc.addField("authors", attr.getAuthor());
                    doc.addField("description", attr.getAbstrakt());
                    doc.addField("summary", attr.getSummary());
                    doc.addField("tags", attr.getTags());
                    doc.addField("language", attr.getLanguages());
                    doc.addField("publicationDOI", pubDOIs);
                    Calendar now = Calendar.getInstance();
                    String data_str = now.YEAR + "-" + now.MONTH + "-" + now.DAY_OF_MONTH + "T" + now.HOUR + ":";
                    data_str += now.MINUTE + ":" + now.SECOND + "." + now.MILLISECOND + "Z";
                    doc.addField("dateUpdated", data_str);
                    doc.addField("publicationTitle", attr.getTitle());
                    doc.addField("publicationDate", attr.getDate());
                    doc.addField("linkUrls", attr.getURL());
                    client.add(doc);
                    client.commit();
                    last_filename = filenames.get(i);
                    System.out.println("Committed metadata for '" + last_filename + "' to Solr DB.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            clock_end = Calendar.getInstance();
            total_time = clock_end.getTimeInMillis() - clock_start.getTimeInMillis();

            metadata.put("num_pdfs", files.size());
            metadata.put("total_time", total_time);
            metadata.put("cermine_time", total_time);
            metadata.put("aztools_time", aztools_time);
            metadata_string = metadata.toString(spacesToIndentEachLevel);

            data_string = data.toString(spacesToIndentEachLevel);

            final_json_object.put("metadata", metadata);
            final_json_object.put("data", data);
            final_json_string = final_json_object.toString(spacesToIndentEachLevel).replace("\\\"", "\"");
        }
        catch (TimeoutException e) {
            e.printStackTrace();
            cermine_time = 0;
            aztools_time = 0;
            total_time = 0;

            metadata.put("num_pdfs", files.size());
            metadata.put("status", "failure");
            metadata_string = metadata.toString(spacesToIndentEachLevel);
            data_string = "";

            final_json_object.put("metadata", metadata);
            final_json_object.put("data", data);
            final_json_string = final_json_object.toString(spacesToIndentEachLevel);
            final_json_string = final_json_string.replace("\\\"", "\"");
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

    public Map<String, Attributes> getMetadata_info() {
        return metadata_info;
    }

    public String getFileName() {
        return last_filename;
    }

}
