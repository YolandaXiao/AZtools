package extraction.summary;

import webapp.Globs;
import java.io.*;

public class Summary {

    private String summary;

    public Summary(String paper_abstract, String filename, String tool_name) {

        try {
            String abs_summ_dir = Globs.get_abs_summ_dir();
            String wr_txt_file_name = abs_summ_dir + filename + "_abstract.txt";
            String rd_txt_file_name = abs_summ_dir + filename + "_summary.txt";

            File file = new File(wr_txt_file_name);
            FileWriter writer = new FileWriter(file);
            if (tool_name == null || tool_name.equals("")) {
                tool_name = "no tool name available";
            }
            writer.write(paper_abstract + "\n" + tool_name);
            writer.close();

            FileReader fr;
            while (true) {
                try {
                    fr = new FileReader(rd_txt_file_name);
                    break;
                } catch (Exception e) {
                    // Wait for python script for find abstract file
                    continue;
                }
            }
            fr = new FileReader(rd_txt_file_name);
            BufferedReader br = new BufferedReader(fr);
            String line;
            if ((line = br.readLine()) != null) {
                summary = line;
            } else {
                summary = paper_abstract;
            }
        }
        catch (Exception e){
            System.out.println("Failed to get summary!");
            e.printStackTrace();
        }
    }

    public String getSummary() {
        return summary;
    }
}
