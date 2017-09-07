package webapp;

import javax.mail.Address;
import java.io.File;
import java.util.*;

public class AppThread implements Runnable {

    private final int NUM_SECONDS_WAIT_TO_CHECK_EMAIL = 20;

    public AppThread() {
        try {
            List<String> commands = new ArrayList();
            commands.add("python");
            commands.add(Globs.get_svm_lib_path());
            new ProcessBuilder(commands).start();
            System.out.println("Started python process.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        ProcessEmail email = new ProcessEmail();

        // Keep checking inbox for new PDFs evert NUM_SECONDS_WAIT_TO_CHECK_EMAIL seconds
        // Loop gets interrupted by POST to /
        // Control returns back to here automatically

        while (true) {
            try {
                email.processInbox();
                Map<Address[], ArrayList<File>> to_process = email.get_to_process();
                ArrayList<Address[]> to_remove = new ArrayList();

                for (Map.Entry<Address[], ArrayList<File>> entry : to_process.entrySet()) {
                    Address[] address = entry.getKey();
                    ArrayList<File> file_list = entry.getValue();
                    ArrayList<String> f_filenames = new ArrayList();
                    for (File file : file_list) { f_filenames.add(file.getName()); }
                    String result = (new ProcessPDF(file_list, f_filenames)).getDataString();
                    // .getDataString() for only PDF metadata
                    // .getMetadataString() for processing stats
                    // .getFinalString() for both

                    // send response to sender
                    email.completeRequest(address, result);
                    to_remove.add(address);
                }

                for (Address[] ad : to_remove) {
                    to_process.remove(ad);
                }

                try {
                    System.out.println("Will check email again after " + NUM_SECONDS_WAIT_TO_CHECK_EMAIL + " seconds.");
                    Thread.sleep(1000 * NUM_SECONDS_WAIT_TO_CHECK_EMAIL);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
