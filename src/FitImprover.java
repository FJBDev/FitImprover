import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class FitImprover {
	 public static void main(String []args) {

		 if(args.length == 0)
		 {
			 System.out.println("No file supplied. Aborting");
			 return;
		 }

		 File fitCSVTool = new File(FitImprover.class.getResource("FitCSVTool.jar").getFile());
		 if(!fitCSVTool.exists())
		 {
			 System.out.println("FitCSVTool.jar could not be found. Aborting");
			 return;
		 }

		 File fileToConvert = new File(FitImprover.class.getResource(args[0]).getFile());
		 ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", fitCSVTool.getAbsolutePath(),fileToConvert.getAbsolutePath());
		 try {
			Process process = processBuilder.start();

			File commands = new File("C:\\Users\\frederic\\git\\FitImprover\\src\\commmands");
			File dirOut = new File("C:\\Users\\frederic\\git\\FitImprover\\src\\dirout");
			File dirErr = new File("C:\\Users\\frederic\\git\\FitImprover\\src\\direrr");

		    InputStream in = process.getInputStream();
		    InputStream err = process.getErrorStream();

		    byte b[]=new byte[in.available()];
		    in.read(b,0,b.length);
		    System.out.println(new String(b));

		    byte c[]=new byte[err.available()];
		    err.read(c,0,c.length);
		    System.out.println(new String(c));
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	   }
}
