package pdl.backend;


import java.io.File;
import java.io.FileNotFoundException;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {
	
	public static void main(String[] args) throws FileNotFoundException {
		File fDir = new File("images");
		if(!(fDir.exists() && fDir.isDirectory()))
			throw new FileNotFoundException("Dossier 'images' inexistant");		
		SpringApplication.run(BackendApplication.class, args);
	}
	public static String getFileExtension(String name) {
		String fileName = new File(name).getName();
		int dotIndex = fileName.lastIndexOf('.');
		return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
	}

}
