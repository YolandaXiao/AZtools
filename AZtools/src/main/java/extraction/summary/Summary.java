package extraction.summary;

import extraction.Properties;
import extraction.SystemCommandExecutor;

//import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Summary {

    private String summary;

    public Summary(String paper_abstract, String filename, String tool_name) {

        try {
//            String abs_summ_dir = Properties.get_abs_summ_dir();
//            String wr_txt_file_name = abs_summ_dir + filename + "_abstract.txt";
//            String rd_txt_file_name = abs_summ_dir + filename + "_summary.txt";
//
//            File file = new File(wr_txt_file_name);
//            FileWriter writer = new FileWriter(file);
//            writer.write(paper_abstract);
//            writer.close();

            List<String> commands = new ArrayList<>();
            commands.add("python");
            String svm_lib_path = Properties.get_svm_lib_path();
            commands.add(svm_lib_path);
            commands.add(paper_abstract);
//            commands.add(filename);
            commands.add(tool_name);

            SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
            int result = commandExecutor.executeCommand();
            StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();

            if (result != 0) {
                StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

                System.out.println("Exit status: " + result);
                System.out.println("STDOUT:");
                System.out.println(stdout);
                System.out.println("STDERR:");
                System.out.println(stderr);
            } else {
                summary = stdout.toString();
            }

//            FileReader fr = new FileReader(rd_txt_file_name);
//            BufferedReader br = new BufferedReader(fr);
//            String line;
//            if ((line = br.readLine()) != null) {
//                summary = line;
//            }
//            else {
//                summary = paper_abstract;
//            }
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
