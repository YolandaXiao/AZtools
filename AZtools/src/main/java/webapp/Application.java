package webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		AppThread ap = new AppThread();
		SpringApplication.run(Application.class, args);
		ap.run();
	}
}

