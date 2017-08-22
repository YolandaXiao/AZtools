package extraction.summary;

import extraction.Properties;
import extraction.SystemCommandExecutor;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Summary {

    private String summary;

    public Summary(String paper_abstract, String filename, String tool_name) {

        try {
            Calendar start = Calendar.getInstance();
            String abs_summ_dir = Properties.get_abs_summ_dir();
            String wr_txt_file_name = abs_summ_dir + filename + "_abstract.txt";
            String rd_txt_file_name = abs_summ_dir + filename + "_summary.txt";
            Calendar mid1 = Calendar.getInstance();

            File file = new File(wr_txt_file_name);
            FileWriter writer = new FileWriter(file);
            writer.write(paper_abstract);
            writer.close();
            Calendar mid2 = Calendar.getInstance();

            List<String> commands = new ArrayList<>();
            commands.add("python");
            String svm_lib_path = Properties.get_svm_lib_path();
            commands.add(svm_lib_path);
//            commands.add(paper_abstract);
            commands.add(filename);
            commands.add(tool_name);
            Calendar mid3 = Calendar.getInstance();

            Calendar mid4 = Calendar.getInstance();
            ProcessBuilder pb = new ProcessBuilder(commands);
            Process process = pb.start();
            int rv = process.waitFor();
            Calendar mid5 = Calendar.getInstance();
            if (rv != 0) {
                InputStream errorStream = process.getErrorStream();
                System.out.println("Exit value: " + rv);
                System.out.println("error stream: " + errorStream);
                summary = "Error while finding summary";
            }

//            SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
//            Calendar mid4 = Calendar.getInstance();
//            int result = commandExecutor.executeCommand();
//            Calendar mid5 = Calendar.getInstance();

//            StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();
//            StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();

//            if (result != 0) {
//                System.out.println("Exit status: " + result);
//                System.out.println("STDOUT:");
//                System.out.println(stdout);
//                System.out.println("STDERR:");
//                System.out.println(stderr);
//            } else {
//                System.out.println("Exit status: " + result);
//                System.out.println("STDOUT:");
//                System.out.println(stdout);
//                System.out.println("STDERR:");
//                System.out.println(stderr);
//                summary = stdout.toString();
//            }

            Calendar mid6 = Calendar.getInstance();

            FileReader fr = new FileReader(rd_txt_file_name);
            BufferedReader br = new BufferedReader(fr);
            String line;
            if ((line = br.readLine()) != null) {
                summary = line;
            }
            else {
                summary = paper_abstract;
            }
//            Calendar mid7 = Calendar.getInstance();
//            System.out.println("start to mid1: ");
//            System.out.println(mid1.getTimeInMillis()-start.getTimeInMillis());
//            System.out.println("mid1 to mid2: ");
//            System.out.println(mid2.getTimeInMillis()-mid1.getTimeInMillis());
//            System.out.println("mid2 to mid3: ");
//            System.out.println(mid3.getTimeInMillis()-mid2.getTimeInMillis());
            System.out.println("mid3 to mid4: ");
            System.out.println(mid4.getTimeInMillis()-mid3.getTimeInMillis());
//            System.out.println("mid4 to mid5: ");
//            System.out.println(mid5.getTimeInMillis()-mid4.getTimeInMillis());
//            System.out.println("mid5 to mid6: ");
//            System.out.println(mid6.getTimeInMillis()-mid5.getTimeInMillis());
//            System.out.println("mid6 to mid7: ");
//            System.out.println(mid7.getTimeInMillis()-mid6.getTimeInMillis());
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
