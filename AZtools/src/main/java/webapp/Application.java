package webapp;

import extraction.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		try {
//			List<String> commands = new ArrayList<>();
//			commands.add("python");
//			String svm_lib_path = Properties.get_svm_lib_path();
//			commands.add(svm_lib_path);
//			ProcessBuilder pb = new ProcessBuilder(commands);
//			Process process = pb.start();
			SpringApplication.run(Application.class, args);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
