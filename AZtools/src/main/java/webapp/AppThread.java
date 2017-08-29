package webapp;

import extraction.Paths;

import javax.mail.Address;
import java.io.File;
import java.util.*;

public class AppThread implements Runnable {

    private final int NUM_SECONDS_WAIT_TO_CHECK_EMAIL = 10;

    public AppThread() {
        try {
            System.out.println("Starting python process...");
            List<String> commands = new ArrayList<>();
            String svm_lib_path = Paths.get_svm_lib_path();
            commands.add("python");
            commands.add(svm_lib_path);
            new ProcessBuilder(commands).start();
            System.out.println("Started.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        ProcessEmail email = new ProcessEmail();
        while (true) {
            try {
                email.process_inbox();
                Map<Address[], ArrayList<File>> to_process = email.get_to_process();
                ArrayList<Address[]> to_remove = new ArrayList<>();
                for (Map.Entry<Address[], ArrayList<File>> entry : to_process.entrySet()) {
                    Address[] address = entry.getKey();
                    ArrayList<File> file_list = entry.getValue();
                    email.completeRequest(address, (new ProcessPDF(file_list)).getDataString());
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
